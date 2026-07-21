package com.crm.enhance_wellness.feature.health.reminder

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

@Singleton
class PrescriptionReminderRepository @Inject constructor(
    private val dao: PrescriptionReminderDao,
    private val scheduler: MedicationReminderScheduler,
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
                "Medication reminders enabled."
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

    private companion object {
        const val HOURS_IN_DAY_MS = 24L * 60L * 60L * 1000L
    }
}
