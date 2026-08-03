package com.example.myapplication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.data.ads.AdsManager
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "Received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-initialize ads after boot
                initializeAds(context)
                
                // Reschedule any pending work
                rescheduleWork(context)
                
                // Re-register FCM token
                refreshFcmToken(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // App was updated
                onAppUpdated(context)
            }
        }
    }

    private fun initializeAds(context: Context) {
        try {
            // Get AdsManager from Hilt
            val app = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(app, AdsEntryPoint::class.java)
            val adsManager = entryPoint.adsManager()
            adsManager.initialize()
            Log.d("BootReceiver", "Ads re-initialized")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Failed to init ads", e)
        }
    }

    private fun rescheduleWork(context: Context) {
        // Reschedule periodic sync, backup, etc.
        // WorkManager handles this automatically if using PeriodicWorkRequest
        Log.d("BootReceiver", "Work rescheduled")
    }

    private fun refreshFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("BootReceiver", "FCM token refreshed: $token")
            // Send token to your server if needed
        }
    }

    private fun onAppUpdated(context: Context) {
        Log.d("BootReceiver", "App updated - running migrations if needed")
        // Run any post-update migrations
        // e.g., database migrations, preference migrations
    }
}

interface AdsEntryPoint {
    fun adsManager(): AdsManager
}