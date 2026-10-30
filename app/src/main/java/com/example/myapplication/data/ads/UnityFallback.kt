package com.example.myapplication.data.ads

import android.app.Activity
import android.content.Context
import com.example.myapplication.R
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import timber.log.Timber

/**
 * Direct Unity Ads backup. Used only when AdMob has no ad ready (e.g. AdMob account limited),
 * and always behind the same consent check and pacing rules as AdMob ads.
 */
class UnityFallback(private val context: Context) {

    private val gameId get() = context.getString(R.string.unity_game_id)
    val interstitialPlacement get() = context.getString(R.string.unity_interstitial_placement)
    val rewardedPlacement get() = context.getString(R.string.unity_rewarded_placement)
    val bannerPlacement get() = context.getString(R.string.unity_banner_placement)
    private val testMode get() = context.resources.getBoolean(R.bool.unity_test_mode)

    private val loaded = mutableSetOf<String>()
    private val loading = mutableSetOf<String>()
    var isInitialized = false
        private set

    fun initialize(onReady: () -> Unit = {}) {
        if (gameId.isBlank()) return
        if (isInitialized) { onReady(); return }
        UnityAds.initialize(context, gameId, testMode, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isInitialized = true
                onReady()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                Timber.d("Unity init failed: %s", message)
            }
        })
    }

    fun load(placement: String) {
        if (!isInitialized || placement.isBlank() || placement in loaded || placement in loading) return
        loading += placement
        UnityAds.load(placement, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                loading -= placementId
                loaded += placementId
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError?, message: String?) {
                loading -= placementId
                Timber.d("Unity load failed %s: %s", placementId, message)
            }
        })
    }

    fun isReady(placement: String) = placement in loaded

    /** [onDone] gets true when a rewarded view completed. Always called exactly once. */
    fun show(activity: Activity, placement: String, onDone: (completed: Boolean) -> Unit) {
        if (!isReady(placement)) { onDone(false); return }
        loaded -= placement
        UnityAds.show(activity, placement, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError?, message: String?) {
                onDone(false); load(placementId)
            }

            override fun onUnityAdsShowStart(placementId: String) = Unit
            override fun onUnityAdsShowClick(placementId: String) = Unit

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState?) {
                onDone(state == UnityAds.UnityAdsShowCompletionState.COMPLETED); load(placementId)
            }
        })
    }
}
