package com.globussoft.wellness.patient

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.globussoft.wellness.patient.core.navigation.WellnessNavGraph
import com.globussoft.wellness.patient.core.theme.WellnessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Updated when a notification intent arrives while the app is already running.
    // Observed by WellnessNavGraph to trigger handleDeepLink on the NavController.
    private var notificationIntent by mutableStateOf<Intent?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — no action needed here */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        requestPostNotificationsPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            WellnessTheme {
                WellnessNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    notificationIntent = notificationIntent,
                )
            }
        }
    }

    // Called when the app is already running and a notification tap brings it to foreground.
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
