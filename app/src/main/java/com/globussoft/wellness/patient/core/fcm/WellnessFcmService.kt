package com.globussoft.wellness.patient.core.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.globussoft.wellness.patient.MainActivity
import com.globussoft.wellness.patient.R
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.notifications.domain.model.Notification
import com.globussoft.wellness.patient.feature.notifications.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class WellnessFcmService : FirebaseMessagingService() {

    @Inject lateinit var fcmHelper: FcmHelper
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var encryptedPrefs: EncryptedPrefsManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (encryptedPrefs.getUserId() == -1) return
        serviceScope.launch { fcmHelper.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val type = data["type"] ?: return
        val title = data["title"] ?: message.notification?.title ?: return
        val body = data["body"] ?: message.notification?.body ?: return
        val screen = data["screen"]
        val entityId = data["entityId"]

        val notificationId = UUID.randomUUID().toString()
        val notification = Notification(
            id = notificationId,
            type = type,
            title = title,
            body = body,
            screen = screen,
            entityId = entityId,
            isRead = false,
            receivedAt = System.currentTimeMillis(),
        )

        serviceScope.launch {
            notificationRepository.insert(notification)
            showSystemNotification(notificationId, type, title, body, screen, entityId)
        }
    }

    private fun showSystemNotification(
        notificationId: String,
        type: String,
        title: String,
        body: String,
        screen: String?,
        entityId: String?,
    ) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val channelId = channelForType(type)
        val pendingIntent = buildPendingIntent(notificationId, screen, entityId)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this)
            .notify(notificationId.hashCode(), notification)
    }

    private fun channelForType(type: String): String = when (type) {
        "APPOINTMENT_REMINDER_24H",
        "APPOINTMENT_REMINDER_1H",
        "BOOKING_CONFIRMED",
        "BOOKING_CANCELLED",
        -> "wellness_reminders"

        "WALLET_CREDITED" -> "wellness_wallet"
        "NPS_SURVEY", "NO_SHOW_REENGAGEMENT" -> "wellness_offers"

        else -> "wellness_health" // PRESCRIPTION_READY, MEMBERSHIP_EXPIRY, unknown
    }

    private fun buildPendingIntent(
        notificationId: String,
        screen: String?,
        entityId: String?,
    ): PendingIntent {
        val deepLinkUri = if (!screen.isNullOrBlank() && screen.startsWith("http")) {
            Uri.parse(screen)
        } else {
            val path = screen ?: "dashboard"
            val base = "wellnesspatient://screen/$path"
            Uri.parse(if (entityId != null) "$base?id=$entityId" else base)
        }

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri, this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        return PendingIntent.getActivity(
            this,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
