package com.crm.enhance_wellness.feature.health.reminder

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.util.DateUtil
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionReminderDao
import com.crm.enhance_wellness.feature.health.data.local.entity.PrescriptionReminderEntity
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class ReminderToggleResult(
    val enabled: Boolean,
    val exactAlarmPermissionRequired: Boolean = false,
    val message: String,
)

data class RequestMedsResult(
    val success: Boolean,
    val message: String,
)

@Singleton
class PrescriptionReminderRepository @Inject constructor(
    private val dao: PrescriptionReminderDao,
    private val scheduler: MedicationReminderScheduler,
    private val apiService: WellnessApiService,
) {
    fun enabledReminderIds(): Flow<Set<Int>> =
        dao.getAllAsFlow().map { reminders ->
            val now = System.currentTimeMillis()
            reminders.filter { it.endAt > now }.map { it.prescriptionId }.toSet()
        }

    suspend fun enable(prescription: Prescription): ReminderToggleResult {
        val drugs = prescription.drugs.mapNotNull { it.toScheduledMedication() }
        if (drugs.isEmpty()) {
            return ReminderToggleResult(
                enabled = false,
                message = "This prescription does not include frequency and duration details.",
            )
        }

        val now = System.currentTimeMillis()
        val createdAt = DateUtil.isoToEpochMs(prescription.createdAt).takeIf { it > 0L } ?: now
        val startAt = maxOf(createdAt, now)
        val endAt = startAt + drugs.maxOf { it.durationDays } * HOURS_IN_DAY_MS
        val reminder = PrescriptionReminderEntity(
            prescriptionId = prescription.id,
            enabledAt = now,
            startAt = startAt,
            endAt = endAt,
            prescriptionLabel = prescription.serviceName ?: prescription.doctorName,
            drugsJson = drugs.toReminderJson(),
        )

        dao.upsert(reminder)
        val exactAllowed = scheduler.schedule(reminder)
        return ReminderToggleResult(
            enabled = true,
            exactAlarmPermissionRequired = !exactAllowed,
            message = if (exactAllowed) {
                "Medication and refill reminders enabled."
            } else {
                "Reminders enabled with approximate timing. Allow exact alarms for best accuracy."
            },
        )
    }

    suspend fun disable(prescriptionId: Int): ReminderToggleResult {
        val reminder = dao.getByPrescriptionId(prescriptionId)
        if (reminder != null) scheduler.cancel(reminder)
        dao.deleteByPrescriptionId(prescriptionId)
        return ReminderToggleResult(enabled = false, message = "Medication reminders disabled.")
    }

    suspend fun cleanupExpired() {
        val now = System.currentTimeMillis()
        dao.getAll()
            .filter { it.endAt <= now }
            .forEach { reminder ->
                scheduler.cancel(reminder)
                dao.delete(reminder)
            }
    }

    suspend fun rescheduleActiveReminders() {
        cleanupExpired()
        dao.getAll()
            .filter { it.endAt > System.currentTimeMillis() }
            .forEach { scheduler.schedule(it) }
    }

    // "Request Meds" — pulls each prescription's backend-computed `validUntil` from
    // GET my-prescriptions and uses it as the depletion alarm's trigger date instead of the
    // locally-estimated course end. Enables a reminder for any matching prescription that
    // doesn't already have one, since tapping this button is itself an explicit opt-in.
    suspend fun requestMeds(prescriptions: List<Prescription>): RequestMedsResult {
        val response = try {
            apiService.getMyPrescriptions()
        } catch (e: Exception) {
            return RequestMedsResult(false, "Couldn't reach the clinic. Check your connection and try again.")
        }
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return RequestMedsResult(false, "Couldn't fetch medicine data from the clinic.")
        }

        val validUntilByPrescriptionId = body.prescriptions.associate { dto ->
            dto.id to DateUtil.isoToEpochMs(dto.validUntil).takeIf { it > 0L }
        }

        val now = System.currentTimeMillis()
        var updatedCount = 0
        prescriptions.forEach { prescription ->
            val validUntilAt = validUntilByPrescriptionId[prescription.id] ?: return@forEach
            val existing = dao.getByPrescriptionId(prescription.id)
            val reminder = if (existing != null) {
                existing.copy(validUntilAt = validUntilAt)
            } else {
                val drugs = prescription.drugs.mapNotNull { it.toScheduledMedication() }
                if (drugs.isEmpty()) return@forEach
                val createdAt = DateUtil.isoToEpochMs(prescription.createdAt).takeIf { it > 0L } ?: now
                val startAt = maxOf(createdAt, now)
                PrescriptionReminderEntity(
                    prescriptionId = prescription.id,
                    enabledAt = now,
                    startAt = startAt,
                    endAt = startAt + drugs.maxOf { it.durationDays } * HOURS_IN_DAY_MS,
                    prescriptionLabel = prescription.serviceName ?: prescription.doctorName,
                    drugsJson = drugs.toReminderJson(),
                    validUntilAt = validUntilAt,
                )
            }
            dao.upsert(reminder)
            scheduler.schedule(reminder)
            updatedCount++
        }

        return RequestMedsResult(
            success = updatedCount > 0,
            message = if (updatedCount > 0) {
                "Medicine reminders updated for $updatedCount prescription${if (updatedCount != 1) "s" else ""}."
            } else {
                "No medicine finish dates found from the clinic yet."
            },
        )
    }

    private companion object {
        const val HOURS_IN_DAY_MS = 24L * 60L * 60L * 1000L
    }
}
