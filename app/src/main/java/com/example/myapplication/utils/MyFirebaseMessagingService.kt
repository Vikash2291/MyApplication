package com.example.myapplication.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.presentation.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "fcm_notifications"
        const val NOTIFICATION_ID_BASE = 2000
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("FCM Message received: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("FCM Data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Timber.d("FCM Notification: ${notification.title} - ${notification.body}")
            sendNotification(notification.title ?: "", notification.body ?: "", remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("FCM Token refreshed: $token")
        sendRegistrationToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Handle custom data messages (e.g., sync triggers, config updates)
        data["type"]?.let { type ->
            when (type) {
                "SYNC" -> triggerBackgroundSync()
                "CONFIG_UPDATE" -> refreshRemoteConfig()
                "AD_REFRESH" -> refreshAds()
                else -> Timber.w("Unknown FCM data type: $type")
            }
        }
    }

    private fun triggerBackgroundSync() {
        val intent = Intent(this, SyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun refreshRemoteConfig() {
        // Firebase Remote Config fetch
        // FirebaseRemoteConfig.getInstance().fetchAndActivate()
    }

    private fun refreshAds() {
        // Trigger ad reload
        // AdsEntryPoint.fromApplication(applicationContext).adsManager().initialize()
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "App notifications"
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        manager.notify(NOTIFICATION_ID_BASE + System.currentTimeMillis().toInt(), notification)
    }

    private fun sendRegistrationToServer(token: String) {
        // Send FCM token to your backend server
        // API call to register token for push notifications
        Timber.d("Sending FCM token to server: $token")
    }
}