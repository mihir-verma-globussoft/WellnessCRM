package com.crm.enhance_wellness.core.websocket

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.crm.enhance_wellness.BuildConfig
import com.crm.enhance_wellness.MainActivity
import com.crm.enhance_wellness.R
import com.crm.enhance_wellness.core.network.TokenManager
import com.crm.enhance_wellness.feature.notifications.domain.model.Notification
import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationPreferencesRepository
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WellnessSocketManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository,
    private val preferencesRepository: NotificationPreferencesRepository,
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null

    init {
        scope.launch {
            tokenManager.tokenFlow()
                .distinctUntilChanged()
                .collect { token ->
                    if (!token.isNullOrBlank()) connect(token)
                    else if (socket?.connected() == true) disconnect()
                }
        }
    }

    private fun connect(token: String) {
        if (socket?.connected() == true) return

        // Empty SOCKET_URL means "no gateway configured" — skip rather than hammer a host
        // that will never speak Socket.IO. Previously this derived the URL from BASE_URL
        // and retried forever against an endpoint that serves the web app's HTML, so the
        // socket could never connect and nothing said so.
        val serverUrl = BuildConfig.SOCKET_URL
        if (serverUrl.isBlank()) {
            Log.i(TAG, "Socket disabled: no SOCKET_URL configured. Notifications sync over REST.")
            return
        }

        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setPath(BuildConfig.SOCKET_PATH)
                .setReconnection(true)
                .setReconnectionAttempts(MAX_RECONNECT_ATTEMPTS)
                .setReconnectionDelay(RECONNECT_DELAY_MS)
                .build()
            socket = IO.socket(serverUrl, options)
            socket?.on("notification_new") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                scope.launch { handleNotification(data) }
            }
            socket?.on(Socket.EVENT_CONNECT) {
                Log.i(TAG, "Socket connected to $serverUrl")
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                // Surfaced rather than swallowed: a permanently unreachable gateway used to
                // look identical to "no notifications have arrived yet".
                Log.w(TAG, "Socket connect failed for $serverUrl: ${args.firstOrNull()}")
            }
            socket?.connect()
        } catch (e: Exception) {
            Log.w(TAG, "Socket setup failed; falling back to REST sync: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "WellnessSocket"

        // Bounded, unlike the previous Int.MAX_VALUE: an unreachable gateway otherwise
        // retries for the life of the process, holding a wakelock-ish loop for nothing.
        const val MAX_RECONNECT_ATTEMPTS = 10
        const val RECONNECT_DELAY_MS = 3000L
    }

    fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
    }

    private suspend fun handleNotification(data: JSONObject) {
        val id = data.optInt("id", 0).toString()
        val notification = Notification(
            id = id,
            type = data.optString("type", "info"),
            title = data.optString("title", ""),
            body = data.optString("message", ""),
            screen = linkToScreen(data.optString("link", null)),
            entityId = data.optInt("entityId", -1).takeIf { it != -1 }?.toString(),
            isRead = false,
            receivedAt = parseDate(data.optString("createdAt", null)),
        )
        val entityType = data.optString("entityType", null)
        val prefs = preferencesRepository.get()
        val category = categoryForEntityType(entityType)

        // The in-app bell is the inbox itself, so it gates persistence; push gates the
        // system tray notification. A muted category silences both.
        if (!prefs.isCategoryEnabled(category)) return
        if (prefs.isChannelEnabled(NotificationPreferences.CHANNEL_IN_APP)) {
            notificationRepository.insert(notification)
        }
        if (!prefs.isChannelEnabled(NotificationPreferences.CHANNEL_PUSH)) return

        // Quiet hours suppress the tray alert only — the message is already in the inbox.
        val now = java.util.Calendar.getInstance()
        val minuteOfDay = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
            now.get(java.util.Calendar.MINUTE)
        if (prefs.isQuietAt(minuteOfDay)) return

        showSystemNotification(
            notification = notification,
            entityType = entityType,
            priority = data.optString("priority", "normal"),
        )
    }

    private fun categoryForEntityType(entityType: String?): String = when (entityType) {
        "Appointment" -> NotificationPreferences.CATEGORY_APPOINTMENTS
        "Prescription" -> NotificationPreferences.CATEGORY_PRESCRIPTIONS
        "Wallet", "Payment", "Transaction" -> NotificationPreferences.CATEGORY_PAYMENTS
        "Membership" -> NotificationPreferences.CATEGORY_MEMBERSHIPS
        "GiftCard" -> NotificationPreferences.CATEGORY_GIFT_CARDS
        // Unknown types fall under appointments, the category patients are least likely
        // to have muted — better a stray notification than a silently dropped one.
        else -> NotificationPreferences.CATEGORY_APPOINTMENTS
    }

    @SuppressLint("MissingPermission")
    private fun showSystemNotification(notification: Notification, entityType: String?, priority: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, channelForType(entityType, priority))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        NotificationManagerCompat.from(context).notify(notification.id.hashCode(), builder.build())
    }

    private fun linkToScreen(link: String?): String? = when {
        link == null -> null
        link.startsWith("/appointments") -> "appointments"
        link.startsWith("/prescriptions") -> "prescriptions"
        link.startsWith("/wallet") -> "wallet"
        link.startsWith("/memberships") -> "memberships"
        link.startsWith("/book") -> "book"
        else -> null
    }

    private fun channelForType(entityType: String?, priority: String?): String = when {
        entityType == "Appointment" -> "wellness_reminders"
        entityType == "Prescription" -> "wellness_health"
        entityType == "Wallet" || entityType == "Payment" -> "wellness_wallet"
        priority == "high" || priority == "urgent" -> "wellness_reminders"
        else -> "wellness_health"
    }

    private fun parseDate(dateStr: String?): Long = runCatching {
        if (!dateStr.isNullOrBlank()) Instant.parse(dateStr).toEpochMilli()
        else System.currentTimeMillis()
    }.getOrDefault(System.currentTimeMillis())
}
