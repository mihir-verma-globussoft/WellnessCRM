package com.crm.enhance_wellness.feature.health.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
                        exactAllowed = exactAllowed,
                    )
                }
                sequence += 1
                triggerAt += intervalMs
            }
        }
        if (BuildConfig.DEBUG && drugs.isNotEmpty() && reminder.endAt > System.currentTimeMillis()) {
            scheduleAlarm(
                triggerAt = System.currentTimeMillis() + DEBUG_TEST_REMINDER_DELAY_MS,
                pendingIntent = pendingIntentFor(
                    prescriptionId = reminder.prescriptionId,
                    drugIndex = DEBUG_TEST_DRUG_INDEX,
                    sequence = DEBUG_TEST_SEQUENCE,
                    drug = drugs.first(),
                    endAt = reminder.endAt,
                ),
                exactAllowed = exactAllowed,
            )
        }

        return exactAllowed
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
    }

    private fun scheduleAlarm(
        triggerAt: Long,
        pendingIntent: PendingIntent,
        exactAllowed: Boolean,
    ) {
        when {
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
        }
        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(prescriptionId, drugIndex, sequence),
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
    }
}
