package com.example.myapplication.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

class SyncService : Service() {

    companion object {
        const val CHANNEL_ID = "sync_channel"
        const val NOTIFICATION_ID = 1001
    }

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    @Inject
    lateinit var adsManager: com.example.myapplication.data.ads.AdsManager

    override fun onCreate() {
        super.onCreate()
        // Hilt injection for Service
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SyncEntryPoint::class.java)
        entryPoint.inject(this)
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY
        
        isRunning = true
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        coroutineScope.launch {
            performSync()
            stopSelf()
        }
        
        return START_STICKY
    }

    private suspend fun performSync() {
        try {
            Log.d("SyncService", "Starting background sync")
            
            // 1. Sync user data with backend
            // syncUserData()
            
            // 2. Refresh ads if needed
            // adsManager.loadRewardedAd("rewarded")
            // adsManager.loadInterstitialAd("interstitial")
            
            // 3. Backup preferences
            // backupPreferences()
            
            // 4. Cleanup old logs/cache
            // cleanupCache()
            
            Log.d("SyncService", "Sync completed successfully")
        } catch (e: Exception) {
            Log.e("SyncService", "Sync failed", e)
        } finally {
            isRunning = false
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Periodic data synchronization"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Syncing Data")
            .setContentText("Background synchronization in progress")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        isRunning = false
    }
}

interface SyncEntryPoint {
    fun inject(service: SyncService)
}