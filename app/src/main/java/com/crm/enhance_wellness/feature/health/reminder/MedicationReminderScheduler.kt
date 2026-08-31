package com.crm.enhance_wellness.feature.health.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.crm.enhance_wellness.BuildConfig
import com.crm.enhance_wellness.feature.health.data.local.entity.PrescriptionReminderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    fun schedule(reminder: PrescriptionReminderEntity): Boolean {
        val exactAllowed = canScheduleExactAlarms()
        cancel(reminder)

        return try {
            val drugs = parseReminderDrugs(reminder.drugsJson)
            drugs.forEachIndexed { drugIndex, drug ->
                val intervalMs = HOURS_IN_DAY_MS / drug.frequencyPerDay
                val drugEndAt = reminder.startAt + drug.durationDays * HOURS_IN_DAY_MS
                var triggerAt = reminder.startAt
                var sequence = 0

                while (triggerAt < drugEndAt && triggerAt < reminder.endAt) {
                    if (triggerAt > System.currentTimeMillis()) {
                        scheduleAlarm(
                            triggerAt = triggerAt,
                            pendingIntent = pendingIntentFor(
                                prescriptionId = reminder.prescriptionId,
                                drugIndex = drugIndex,
                                sequence = sequence,
                                drug = drug,
                                endAt = reminder.endAt,
                            ),
                            prescriptionId = reminder.prescriptionId,
                            exactAllowed = exactAllowed,
                        )
                    }
                    sequence += 1
                    triggerAt += intervalMs
                }
                scheduleDepletionAlarm(reminder, drugIndex, drug, exactAllowed)
            }
            if (BuildConfig.DEBUG && drugs.isNotEmpty() && reminder.endAt > System.currentTimeMillis()) {
                val debugDrug = drugs.first()
                scheduleAlarm(
                    triggerAt = System.currentTimeMillis() + DEBUG_TEST_REMINDER_DELAY_MS,
                    pendingIntent = pendingIntentFor(
                        prescriptionId = reminder.prescriptionId,
                        drugIndex = DEBUG_TEST_DRUG_INDEX,
                        sequence = DEBUG_TEST_SEQUENCE,
                        drug = debugDrug,
                        endAt = reminder.endAt,
                    ),
                    prescriptionId = reminder.prescriptionId,
                    exactAllowed = exactAllowed,
                )
                scheduleAlarm(
                    triggerAt = System.currentTimeMillis() + DEBUG_TEST_DEPLETION_DELAY_MS,
                    pendingIntent = depletionPendingIntentFor(
                        reminder.prescriptionId,
                        DEBUG_TEST_DEPLETION_DRUG_INDEX,
                        debugDrug,
                        reminder.validUntilAt ?: reminder.endAt,
                    ),
                    prescriptionId = reminder.prescriptionId,
                    exactAllowed = exactAllowed,
                )
            }

            exactAllowed
        } catch (e: SecurityException) {
            false
        }
    }

    fun cancel(reminder: PrescriptionReminderEntity) {
        parseReminderDrugs(reminder.drugsJson).forEachIndexed { drugIndex, drug ->
            val intervalMs = HOURS_IN_DAY_MS / drug.frequencyPerDay
            val drugEndAt = reminder.startAt + drug.durationDays * HOURS_IN_DAY_MS
            var triggerAt = reminder.startAt
            var sequence = 0
            while (triggerAt < drugEndAt && triggerAt < reminder.endAt) {
                alarmManager.cancel(
                    pendingIntentFor(
                        prescriptionId = reminder.prescriptionId,
                        drugIndex = drugIndex,
                        sequence = sequence,
                        drug = drug,
                        endAt = reminder.endAt,
                    )
                )
                sequence += 1
                triggerAt += intervalMs
            }
            alarmManager.cancel(depletionPendingIntentFor(reminder.prescriptionId, drugIndex))
        }
        alarmManager.cancel(
            pendingIntentFor(
                prescriptionId = reminder.prescriptionId,
                drugIndex = DEBUG_TEST_DRUG_INDEX,
                sequence = DEBUG_TEST_SEQUENCE,
                drug = ScheduledMedication(
                    name = "debug",
                    dosage = null,
                    frequencyPerDay = 1,
                    durationDays = 1,
                ),
                endAt = reminder.endAt,
            )
        )
        alarmManager.cancel(depletionPendingIntentFor(reminder.prescriptionId, DEBUG_TEST_DEPLETION_DRUG_INDEX))
    }

    private fun scheduleAlarm(
        triggerAt: Long,
        pendingIntent: PendingIntent,
        prescriptionId: Int,
        exactAllowed: Boolean,
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAllowed ->
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(
                        triggerAt,
                        prescriptionPdfPendingIntent(prescriptionId),
                    ),
                    pendingIntent,
                )
            exactAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            exactAllowed ->
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            else ->
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    // Fires once, DEPLETION_REMINDER_LEAD_DAYS before the medicine is expected to run out, so
    // the patient has time to book a follow-up before they're actually out. Prefers the
    // backend's own `validUntil` (set via "Request Meds" — see PrescriptionReminderRepository)
    // over the locally-estimated course end date (`endAt`, from the drug's `duration` alone).
    private fun scheduleDepletionAlarm(
        reminder: PrescriptionReminderEntity,
        drugIndex: Int,
        drug: ScheduledMedication,
        exactAllowed: Boolean,
    ) {
        val depletionAt = reminder.validUntilAt ?: reminder.endAt
        val triggerAt = depletionAt - DEPLETION_REMINDER_LEAD_DAYS * HOURS_IN_DAY_MS
        if (triggerAt <= System.currentTimeMillis()) return

        scheduleAlarm(
            triggerAt = triggerAt,
            pendingIntent = depletionPendingIntentFor(reminder.prescriptionId, drugIndex, drug, depletionAt),
            prescriptionId = reminder.prescriptionId,
            exactAllowed = exactAllowed,
        )
    }

    private fun depletionPendingIntentFor(
        prescriptionId: Int,
        drugIndex: Int,
        drug: ScheduledMedication? = null,
        depletionAt: Long = 0L,
    ): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_PRESCRIPTION_ID, prescriptionId)
            putExtra(MedicationReminderReceiver.EXTRA_DRUG_NAME, drug?.name)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, drug?.dosage)
            putExtra(MedicationReminderReceiver.EXTRA_DEPLETION_AT, depletionAt)
            putExtra(MedicationReminderReceiver.EXTRA_REMINDER_TYPE, MedicationReminderReceiver.TYPE_DEPLETION)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(prescriptionId, drugIndex, DEPLETION_SEQUENCE),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun pendingIntentFor(
        prescriptionId: Int,
        drugIndex: Int,
        sequence: Int,
        drug: ScheduledMedication,
        endAt: Long,
    ): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_PRESCRIPTION_ID, prescriptionId)
            putExtra(MedicationReminderReceiver.EXTRA_DRUG_NAME, drug.name)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, drug.dosage)
            putExtra(MedicationReminderReceiver.EXTRA_END_AT, endAt)
            putExtra(MedicationReminderReceiver.EXTRA_REMINDER_TYPE, MedicationReminderReceiver.TYPE_DOSE)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(prescriptionId, drugIndex, sequence),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun prescriptionPdfPendingIntent(prescriptionId: Int): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("globuscrm://screen/prescription_pdf?id=$prescriptionId"),
        ).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            "alarm-clock-prescription-pdf-$prescriptionId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun requestCodeFor(prescriptionId: Int, drugIndex: Int, sequence: Int): Int =
        prescriptionId * 100_000 + drugIndex * 1_000 + sequence

    private companion object {
        const val HOURS_IN_DAY_MS = 24L * 60L * 60L * 1000L
        const val DEBUG_TEST_REMINDER_DELAY_MS = 60L * 1000L
        const val DEBUG_TEST_DRUG_INDEX = 999
        const val DEBUG_TEST_SEQUENCE = 999
        const val DEPLETION_REMINDER_LEAD_DAYS = 3
        const val DEPLETION_SEQUENCE = 998
        const val DEBUG_TEST_DEPLETION_DELAY_MS = 90L * 1000L
        const val DEBUG_TEST_DEPLETION_DRUG_INDEX = 998
    }
}
