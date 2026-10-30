package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.example.myapplication.core.common.Constants
import com.example.myapplication.data.ads.AppOpenAdManager
import com.example.myapplication.utils.SyncWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var appOpenAdManager: AppOpenAdManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    val isFirebaseAvailable: Boolean
        get() = FirebaseApp.getApps(this).isNotEmpty()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.FIREBASE_CONFIGURED) {
            FirebaseApp.initializeApp(this)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else if (isFirebaseAvailable) {
            Timber.plant(CrashlyticsTree())
        }

        if (isFirebaseAvailable) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            ProcessLifecycleOwner.get().lifecycle.addObserver(
                AppLifecycleObserver(FirebaseAnalytics.getInstance(this))
            )
        } else {
            Timber.w("Firebase not configured: add app/google-services.json to enable auth & analytics")
        }

        // App open ads stay disabled until UMP consent allows requests (see AdsManagerImpl).
        appOpenAdManager.register(this)

        createNotificationChannels()
        SyncWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_GENERAL,
                    getString(R.string.notification_channel_general),
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_SYNC,
                    getString(R.string.notification_channel_sync),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        )
    }
}

private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        if (priority >= Log.ERROR) crashlytics.recordException(t ?: Exception(message))
    }
}

private class AppLifecycleObserver(
    private val analytics: FirebaseAnalytics
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        analytics.logEvent("app_foreground", null)
    }

    override fun onStop(owner: LifecycleOwner) {
        analytics.logEvent("app_background", null)
    }
}
