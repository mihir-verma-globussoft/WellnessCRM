package com.crm.enhance_wellness.feature.health.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicationReminderRescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: PrescriptionReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in SUPPORTED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            reminderRepository.rescheduleActiveReminders()
            pendingResult.finish()
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
