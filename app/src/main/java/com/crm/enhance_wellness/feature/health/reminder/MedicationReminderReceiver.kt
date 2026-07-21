package com.crm.enhance_wellness.feature.health.reminder

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.crm.enhance_wellness.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicationReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: PrescriptionReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val prescriptionId = intent.getIntExtra(EXTRA_PRESCRIPTION_ID, -1)
        val endAt = intent.getLongExtra(EXTRA_END_AT, 0L)
        if (prescriptionId == -1) return

        if (endAt > 0L && System.currentTimeMillis() > endAt) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                reminderRepository.disable(prescriptionId)
                pendingResult.finish()
            }
            return
        }

        showNotification(context, intent, prescriptionId)
    }

    private fun showNotification(context: Context, intent: Intent, prescriptionId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val drugName = intent.getStringExtra(EXTRA_DRUG_NAME).orEmpty().ifBlank { "your medication" }
        val dosage = intent.getStringExtra(EXTRA_DOSAGE)?.ifBlank { null } ?: "recommended"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MEDICATION_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for your medication")
            .setContentText("It's time to take your recommended dosage for $drugName ($dosage unit/s).")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "It's time to take your recommended dosage for $drugName ($dosage unit/s)."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            "medication-$prescriptionId-$drugName".hashCode(),
            notification,
        )
    }

    companion object {
        const val CHANNEL_ID_MEDICATION_REMINDERS = "wellness_medication_reminders"
        const val CHANNEL_NAME_MEDICATION_REMINDERS = "Medication Reminders"

        const val EXTRA_PRESCRIPTION_ID = "extra_prescription_id"
        const val EXTRA_DRUG_NAME = "extra_drug_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_END_AT = "extra_end_at"
    }
}
