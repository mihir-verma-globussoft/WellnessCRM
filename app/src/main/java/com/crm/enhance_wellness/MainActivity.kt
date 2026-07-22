package com.crm.enhance_wellness

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crm.enhance_wellness.core.navigation.WellnessNavGraph
import com.crm.enhance_wellness.core.theme.WellnessTheme
import com.crm.enhance_wellness.core.websocket.WellnessSocketManager
import com.crm.enhance_wellness.feature.health.reminder.MedicationReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var socketManager: WellnessSocketManager

    private val mainVm: MainViewModel by viewModels()

    private var notificationIntent by mutableStateOf<Intent?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationIntent = intent
        createNotificationChannels()
        requestPostNotificationsPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by mainVm.isDarkTheme.collectAsStateWithLifecycle()
            val clinicName by mainVm.clinicName.collectAsStateWithLifecycle()
            val unreadCount by mainVm.unreadNotificationCount.collectAsStateWithLifecycle()

            WellnessTheme(darkTheme = isDarkTheme) {
                WellnessNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    notificationIntent = notificationIntent,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = mainVm::toggleDarkTheme,
                    clinicName = clinicName,
                    unreadNotificationCount = unreadCount,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationIntent = intent
    }

    private fun requestPostNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    MedicationReminderReceiver.CHANNEL_ID_MEDICATION_REMINDERS,
                    MedicationReminderReceiver.CHANNEL_NAME_MEDICATION_REMINDERS,
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Medication dosage reminder notifications"
                    enableVibration(true)
                },
                NotificationChannel(
                    "wellness_reminders",
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Appointment reminder notifications"
                    enableVibration(true)
                },
                NotificationChannel(
                    "wellness_health",
                    "Health Updates",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Prescription and health update notifications"
                },
                NotificationChannel(
                    "wellness_wallet",
                    "Wallet & Payments",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Wallet credit and payment notifications"
                },
                NotificationChannel(
                    "wellness_offers",
                    "Offers & Surveys",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Offers, promotions, and survey notifications"
                },
            )
        )
    }
}
