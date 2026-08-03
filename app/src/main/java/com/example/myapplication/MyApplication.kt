package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.data.ads.AdsManagerImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        // Initialize Analytics
        FirebaseAnalytics.getInstance(this)

        // Setup Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        // Initialize Ads (AdMob & Unity)
        AdsManagerImpl.initialize(this)

        // Register lifecycle observer for analytics
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun getInstance(context: Context): MyApplication {
            return context.applicationContext as MyApplication
        }
    }
}

private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= android.util.Log.ERROR) {
            FirebaseCrashlytics.getInstance().recordException(
                t ?: Exception(message)
            )
        }
    }
}

class AppLifecycleObserver : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        FirebaseAnalytics.getInstance(owner as Application).logEvent("app_foreground", null)
    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to background
        FirebaseAnalytics.getInstance(owner as Application).logEvent("app_background", null)
    }
}