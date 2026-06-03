package com.globussoft.wellness.patient.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

// Full implementation in Phase 9
@AndroidEntryPoint
class WellnessFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO Phase 9: register token via FcmHelper when user is logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO Phase 9: persist to Room + show system notification on correct channel
    }
}
