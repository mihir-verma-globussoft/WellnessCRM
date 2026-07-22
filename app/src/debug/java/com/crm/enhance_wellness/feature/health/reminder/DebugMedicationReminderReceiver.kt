package com.crm.enhance_wellness.feature.health.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.crm.enhance_wellness.R

class DebugMedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val prescriptionId = intent.getIntExtra(EXTRA_PRESCRIPTION_ID, DEFAULT_PRESCRIPTION_ID)
        val drugName = intent.getStringExtra(EXTRA_DRUG_NAME).orEmpty().ifBlank { DEFAULT_DRUG_NAME }
        val dosage = intent.getStringExtra(EXTRA_DOSAGE).orEmpty().ifBlank { DEFAULT_DOSAGE }
        val shortMessage = "Take $drugName ($dosage unit/s)."
        val detailedMessage = "It's time to take your recommended dosage for $drugName " +
            "($dosage unit/s). Tap to view prescription #$prescriptionId in PDF view."
        MedicationReminderReceiver.ensureMedicationReminderChannel(context)

        val notification = NotificationCompat.Builder(
            context,
            MedicationReminderReceiver.CHANNEL_ID_MEDICATION_REMINDERS,
        )
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Time for your medication")
            .setContentText(shortMessage)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(detailedMessage)
            )
            .setContentIntent(createPrescriptionPdfIntent(context, prescriptionId))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            "debug-medication-$prescriptionId-$drugName".hashCode(),
            notification,
        )
    }

    private fun createPrescriptionPdfIntent(context: Context, prescriptionId: Int): PendingIntent {
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
            "debug-prescription-pdf-$prescriptionId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val EXTRA_PRESCRIPTION_ID = "extra_prescription_id"
        const val EXTRA_DRUG_NAME = "extra_drug_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val DEFAULT_PRESCRIPTION_ID = 281
        const val DEFAULT_DRUG_NAME = "Fucidin Cream"
        const val DEFAULT_DOSAGE = "Apply thin layer"
    }
}
