package com.example.myapplication.data.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/** Shared flag so the different full-screen formats never stack on top of each other. */
@Singleton
class FullScreenAdTracker @Inject constructor() {
    private val showing = AtomicBoolean(false)
    val isShowing: Boolean get() = showing.get()
    fun setShowing(value: Boolean) = showing.set(value)
}

/**
 * App open ads, shown when the user returns to the app after it was in the background.
 * Never shown on the first cold-start foreground, before consent allows requests, over another
 * full-screen ad, or more than once every 4 hours (see [AppOpenPacer]).
 */
@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tracker: FullScreenAdTracker
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private val pacer = AppOpenPacer(clock = SystemClock::elapsedRealtime)

    private var appOpenAd: AppOpenAd? = null
    private var adLoadedAt: Long? = null
    private var isLoading = false
    private var enabled = false
    private var isColdStartForeground = true
    private var currentActivity: Activity? = null
    private var registered = false

    private val adUnitId get() = context.getString(R.string.admob_app_open_ad_unit_id)

    fun register(application: Application) {
        if (registered) return
        registered = true
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Called by [AdsManagerImpl] once consent allows requests and MobileAds is initialised. */
    fun setEnabled(value: Boolean) {
        enabled = value
        if (value) {
            loadAd()
        } else {
            appOpenAd = null
            adLoadedAt = null
        }
    }

    private fun loadAd() {
        if (!enabled || isLoading || (appOpenAd != null && pacer.isAdFresh(adLoadedAt))) return
        isLoading = true
        AppOpenAd.load(context, adUnitId, AdRequest.Builder().build(), object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                adLoadedAt = SystemClock.elapsedRealtime()
                isLoading = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Timber.d("App open failed to load: %s", error.message)
                isLoading = false
            }
        })
    }

    override fun onStart(owner: LifecycleOwner) {
        val coldStart = isColdStartForeground
        isColdStartForeground = false
        val activity = currentActivity ?: return
        if (!pacer.canShow(coldStart, enabled, tracker.isShowing, adLoadedAt)) {
            loadAd()
            return
        }
        val ad = appOpenAd ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Timber.d("App open failed to show: %s", error.message)
                finish()
            }
        }
        appOpenAd = null
        adLoadedAt = null
        tracker.setShowing(true)
        pacer.onShown()
        ad.show(activity)
    }

    private fun finish() {
        tracker.setShowing(false)
        loadAd()
    }

    // Track the foreground activity; ignore the ad SDK's own activities.
    override fun onActivityStarted(activity: Activity) {
        val name = activity.javaClass.name
        if (!name.startsWith("com.google.android.gms.ads") && !name.startsWith("com.unity3d.")) currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
