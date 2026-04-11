package com.publiceye.app.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messaging (FCM) messages and token refresh.
 *
 * Phase 2+ will add notification display logic for:
 *  - Status updates on reported issues
 *  - Upvote milestone notifications
 *  - Nearby issue alerts
 */
class PublicEyeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        // TODO Phase 2: Send token to Firestore for this user
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")
        // TODO Phase 2: Build and show notification
    }

    companion object {
        private const val TAG = "PublicEyeFCM"
    }
}
