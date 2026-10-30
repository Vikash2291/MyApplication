package com.example.myapplication.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.core.common.Constants
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.presentation.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onNewToken(token: String) {
        // Send the token to your backend here if you target individual devices.
        Timber.d("FCM token refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // onMessageReceived runs on a background thread.
        val enabled = runBlocking { preferencesRepository.settings.first().notificationsEnabled }
        if (!enabled) return

        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(title, body, message.data["deeplink"])
    }

    private fun showNotification(title: String, body: String, deeplink: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deeplink?.let { putExtra(MainActivity.EXTRA_DEEPLINK, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }
}
