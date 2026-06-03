package com.globussoft.wellness.patient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.globussoft.wellness.patient.core.navigation.WellnessNavGraph
import com.globussoft.wellness.patient.core.theme.WellnessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        enableEdgeToEdge()
        setContent {
            WellnessTheme {
                WellnessNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
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
