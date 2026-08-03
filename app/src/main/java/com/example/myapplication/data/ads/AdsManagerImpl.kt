package com.example.myapplication.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.core.log.DeviceLog
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManagerImpl @Inject constructor(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : AdsManager {

    private var admobInitialized = false
    private var unityInitialized = false
    private var consentForm: ConsentForm? = null
    
    // Coroutine scope for async operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val disposables = CompositeDisposable()
    private val handler = Handler(Looper.getMainLooper())
    
    // Configuration constants
    private val AD_LOAD_TIMEOUT_MS = 15000L // 15 seconds
    private val BANNER_REFRESH_INTERVAL_MS = 45000L // 45 seconds
    private val MAX_RETRY_ATTEMPTS = 3
    private val BASE_RETRY_DELAY_MS = 1000L
    
    // Test device IDs (add your test device hashes here)
    private val testDeviceIds = mutableSetOf<String>().apply {
        // Add test device hashes here, e.g.:
        // add("ABC123DEF456")
        // Get hash from logcat: "Use RequestConfiguration.Builder().setTestDeviceIds..."
    }
    
    // State tracking
    private var isDestroyed = false
    private var bannerRefreshRunnable: Runnable? = null
    
    // AdMob ads
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var bannerAd: AdView? = null
    
    // Unity Ads
    private var unityRewardedPlacement: String? = "ueo788l6qmoasnip"
    private var unityInterstitialPlacement: String? = "ysaqr7c7aa7qdmpu"
    private var unityBannerView: BannerView? = null
    
    // Retry tracking
    private var rewardedRetryCount = 0
    private var interstitialRetryCount = 0
    private var bannerRetryCount = 0
    private var currentPlacementId: String? = null
    private var currentAdType: Int = 0 // 0=rewarded, 1=interstitial, 2=banner
    
    // Callbacks
    private var rewardedCallback: ((Boolean) -> Unit)? = null
    private var interstitialCallback: (() -> Unit)? = null
    private var bannerLoadCallback: ((Boolean) -> Unit)? = null
    private var bannerErrorCallback: ((String) -> Unit)? = null
    
    // Network quality detection
    private var isWifiConnected = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun initialize() {
        if (admobInitialized && unityInitialized) return

        // Initialize AdMob
        MobileAds.initialize(context) { status ->
            admobInitialized = true
            Log.d("AdsManager", "AdMob initialized: $status")
            
            // Request consent info
            requestConsentInfoUpdate()
        }

        // Initialize Unity Ads
        val gameId = context.getString(R.string.unity_game_id)
        if (gameId.isNotEmpty() && gameId != "YOUR_UNITY_GAME_ID") {
            UnityAds.initialize(context, gameId, **false**, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    unityInitialized = true
                    Log.d("AdsManager", "Unity Ads initialized")
                    DeviceLog.setDebugMode(BuildConfig.DEBUG)
                }

                override fun onInitializationFailed(error: com.unity3d.ads.UnityAds.UnityAdsInitializationError, message: String) {
                    Log.e("AdsManager", "Unity Ads init failed: $error - $message")
                }
            })
        } else {
            Log.w("AdsManager", "Unity Game ID not configured")
        }
    }

    // Helper functions to check if ad networks are enabled
    private suspend fun isAdMobEnabled(): Boolean {
        return try {
            preferencesRepository.adMobEnabled.first()
        } catch (e: Exception) {
            Log.w("AdsManager", "DataStore error, defaulting to true", e)
            true // Fail open - show ads if storage fails
        }
    }

    private suspend fun isUnityAdsEnabled(): Boolean {
        return try {
            preferencesRepository.unityAdsEnabled.first()
        } catch (e: Exception) {
            Log.w("AdsManager", "DataStore error, defaulting to true", e)
            true
        }
    }

    private suspend fun isIronSourceEnabled(): Boolean {
        return try {
            preferencesRepository.ironSourceEnabled.first()
        } catch (e: Exception) {
            Log.w("AdsManager", "DataStore error, defaulting to true", e)
            true
        }
    }

    // Build AdRequest with timeout and test devices
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        
        // Add test device IDs if any
        if (testDeviceIds.isNotEmpty()) {
            builder.setRequestConfiguration(
                com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
            )
        }
        
        return builder.build()
    }

    // Exponential backoff retry
    private suspend fun loadWithRetry(
        attempt: Int,
        maxAttempts: Int,
        loadAction: suspend () -> Boolean
    ): Boolean {
        if (attempt >= maxAttempts || isDestroyed) return false
        
        val success = loadAction()
        if (success) return true
        
        val delay = (BASE_RETRY_DELAY_MS * (2.0.pow(attempt))).toLong()
        delay(delay)
        return loadWithRetry(attempt + 1, maxAttempts, loadAction)
    }

    override fun loadRewardedAd(placementId: String) {
        currentPlacementId = placementId
        currentAdType = 0
        rewardedRetryCount = 0
        
        if (admobInitialized) {
            val adMobEnabled = isAdMobEnabled()
            if (adMobEnabled) {
                val adUnitId = context.getString(R.string.admob_rewarded_ad_unit_id)
                
                if (adUnitId.isNotEmpty() && adUnitId != "YOUR_ADMOB_REWARDED_AD_UNIT_ID") {
                    coroutineScope.launch {
                        val loaded = withTimeoutOrNull(AD_LOAD_TIMEOUT_MS) {
                            loadWithRetry(0, MAX_RETRY_ATTEMPTS) {
                                suspendedLoadRewardedAd(adUnitId)
                            }
                        }
                        if (loaded != true) {
                            Log.w("AdsManager", "Rewarded ad load timeout/failed, falling back to Unity")
                            loadUnityRewardedAd(placementId)
                        }
                    }
                    return
                }
            }
        }
        
        // Fallback to Unity Ads
        loadUnityRewardedAd(placementId)
    }

    private suspend fun suspendedLoadRewardedAd(adUnitId: String): Boolean {
        return suspendCancellableCoroutine { cont ->
            RewardedAd.load(context, adUnitId, buildAdRequest(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            // Preload next
                            loadRewardedAd(currentPlacementId ?: "rewarded")
                        }

                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                            Log.e("AdsManager", "AdMob rewarded ad failed to show: $error")
                            rewardedAd = null
                            loadRewardedAd(currentPlacementId ?: "rewarded")
                        }
                    }
                    Log.d("AdsManager", "AdMob rewarded ad loaded")
                    cont.resume(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdsManager", "AdMob rewarded ad failed to load: $error")
                    cont.resume(false)
                }
            })
        }
    }

    private fun loadUnityRewardedAd(placementId: String) {
        val unityEnabled = isUnityAdsEnabled()
        if (!unityInitialized || !unityEnabled) return
        
        unityRewardedPlacement = placementId
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d("AdsManager", "Unity rewarded ad loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: com.unity3d.ads.UnityAds.UnityAdsLoadError, message: String) {
                Log.e("AdsManager", "Unity rewarded ad failed to load: $error - $message")
            }
        })
    }

    override fun showRewardedAd(placementId: String, onReward: (Boolean) -> Unit) {
        rewardedCallback = onReward

        // Try AdMob first if enabled
        val adMobEnabled = isAdMobEnabled()
        if (adMobEnabled && rewardedAd != null) {
            val activity = context as? Activity ?: return
            rewardedAd?.show(activity) { reward ->
                Log.d("AdsManager", "AdMob rewarded: ${reward.amount} ${reward.type}")
                onReward(true)
            }
            return
        }

        // Fallback to Unity Ads
        val unityEnabled = isUnityAdsEnabled()
        if (unityInitialized && unityEnabled && unityRewardedPlacement != null) {
            val activity = context as? Activity ?: return
            UnityAds.show(activity, unityRewardedPlacement!!, object : IUnityAdsShowListener {
                override fun onUnityAdsShowStart(placementId: String) {}

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(placementId: String, state: com.unity3d.ads.UnityAds.UnityAdsShowCompletionState) {
                    val rewarded = state == com.unity3d.ads.UnityAds.UnityAdsShowCompletionState.COMPLETED
                    Log.d("AdsManager", "Unity rewarded completed: $rewarded")
                    onReward(rewarded)
                    
                    // Preload next
                    loadUnityRewardedAd(placementId)
                }

                override fun onUnityAdsShowFailure(placementId: String, error: com.unity3d.ads.UnityAds.UnityAdsShowError, message: String) {
                    Log.e("AdsManager", "Unity rewarded show failed: $error - $message")
                    onReward(false)
                }
            })
        } else {
            onReward(false)
        }
    }

    override fun loadInterstitialAd(placementId: String) {
        currentPlacementId = placementId
        currentAdType = 1
        interstitialRetryCount = 0
        
        if (admobInitialized) {
            val adMobEnabled = isAdMobEnabled()
            if (adMobEnabled) {
                val adUnitId = context.getString(R.string.admob_interstitial_ad_unit_id)
                
                if (adUnitId.isNotEmpty() && adUnitId != "YOUR_ADMOB_INTERSTITIAL_AD_UNIT_ID") {
                    coroutineScope.launch {
                        val loaded = withTimeoutOrNull(AD_LOAD_TIMEOUT_MS) {
                            loadWithRetry(0, MAX_RETRY_ATTEMPTS) {
                                suspendedLoadInterstitialAd(adUnitId)
                            }
                        }
                        if (loaded != true) {
                            Log.w("AdsManager", "Interstitial ad load timeout/failed, falling back to Unity")
                            loadUnityInterstitialAd(placementId)
                        }
                    }
                    return
                }
            }
        }
        
        loadUnityInterstitialAd(placementId)
    }

    private suspend fun suspendedLoadInterstitialAd(adUnitId: String): Boolean {
        return suspendCancellableCoroutine { cont ->
            InterstitialAd.load(context, adUnitId, buildAdRequest(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitialAd(currentPlacementId ?: "interstitial")
                        }

                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                            Log.e("AdsManager", "AdMob interstitial failed to show: $error")
                            interstitialAd = null
                            loadInterstitialAd(currentPlacementId ?: "interstitial")
                        }
                    }
                    Log.d("AdsManager", "AdMob interstitial loaded")
                    cont.resume(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdsManager", "AdMob interstitial failed to load: $error")
                    cont.resume(false)
                }
            })
        }
    }

    private fun loadUnityInterstitialAd(placementId: String) {
        val unityEnabled = isUnityAdsEnabled()
        if (!unityInitialized || !unityEnabled) return
        
        unityInterstitialPlacement = placementId
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d("AdsManager", "Unity interstitial loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: com.unity3d.ads.UnityAds.UnityAdsLoadError, message: String) {
                Log.e("AdsManager", "Unity interstitial failed to load: $error - $message")
            }
        })
    }

    override fun showInterstitialAd(placementId: String, onComplete: () -> Unit) {
        interstitialCallback = onComplete

        val adMobEnabled = isAdMobEnabled()
        if (adMobEnabled && interstitialAd != null) {
            val activity = context as? Activity ?: return
            interstitialAd?.show(activity)
            return
        }

        val unityEnabled = isUnityAdsEnabled()
        if (unityInitialized && unityEnabled && unityInterstitialPlacement != null) {
            val activity = context as? Activity ?: return
            UnityAds.show(activity, unityInterstitialPlacement!!, object : IUnityAdsShowListener {
                override fun onUnityAdsShowStart(placementId: String) {}

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(placementId: String, state: com.unity3d.ads.UnityAds.UnityAdsShowCompletionState) {
                    Log.d("AdsManager", "Unity interstitial completed")
                    onComplete()
                    loadUnityInterstitialAd(placementId)
                }

                override fun onUnityAdsShowFailure(placementId: String, error: com.unity3d.ads.UnityAds.UnityAdsShowError, message: String) {
                    Log.e("AdsManager", "Unity interstitial show failed: $error - $message")
                    onComplete()
                }
            })
        } else {
            onComplete()
        }
    }

    override fun loadBannerAd(adUnitId: String, onLoad: () -> Unit, onError: (String) -> Unit) {
        bannerLoadCallback = { onLoad() }
        bannerErrorCallback = { onError(it) }
        bannerRetryCount = 0
        currentPlacementId = adUnitId
        currentAdType = 2
        
        // Try AdMob banner if enabled
        val adMobEnabled = isAdMobEnabled()
        if (admobInitialized && adMobEnabled) {
            val activity = context as? Activity ?: return
            val adView = AdView(activity).apply {
                this.adUnitId = adUnitId
                adSize = AdSize.BANNER
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            adView.loadAd(buildAdRequest())
            adView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    bannerAd = adView
                    startBannerAutoRefresh()
                    bannerLoadCallback?.invoke(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdsManager", "AdMob banner failed: $error")
                    bannerErrorCallback?.invoke(error.message)
                    loadUnityBannerAd(onLoad, onError)
                }
            })
            return
        }

        loadUnityBannerAd(onLoad, onError)
    }

    private fun startBannerAutoRefresh() {
        bannerRefreshRunnable = Runnable {
            if (!isDestroyed && bannerAd != null && isAdMobEnabled()) {
                bannerAd?.loadAd(buildAdRequest())
                handler.postDelayed(this, BANNER_REFRESH_INTERVAL_MS)
            }
        }
        handler.postDelayed(bannerRefreshRunnable!!, BANNER_REFRESH_INTERVAL_MS)
    }

    private fun stopBannerAutoRefresh() {
        bannerRefreshRunnable?.let { handler.removeCallbacks(it) }
        bannerRefreshRunnable = null
    }

    private fun loadUnityBannerAd(onLoad: () -> Unit, onError: (String) -> Unit) {
        val unityEnabled = isUnityAdsEnabled()
        if (!unityInitialized || !unityEnabled) {
            onError("Unity not initialized or disabled")
            return
        }

        val activity = context as? Activity ?: return
        val placementId = "t3s1x1lx8d5xny56"
        
        unityBannerView = BannerView(activity, placementId, UnityBannerSize.BANNER)
        unityBannerView?.load()

        unityBannerView?.bannerListener = object : com.unity3d.services.banners.BannerView.IListener {
            override fun onUnityBannerLoaded(bannerView: BannerView, placementId: String) {
                Log.d("AdsManager", "Unity banner loaded")
                onLoad()
            }

            override fun onUnityBannerUnloaded(bannerView: BannerView, placementId: String) {
                Log.d("AdsManager", "Unity banner unloaded")
            }

            override fun onUnityBannerShow(bannerView: BannerView, placementId: String) {}

            override fun onUnityBannerClick(bannerView: BannerView, placementId: String) {}

            override fun onUnityBannerError(bannerView: BannerView, error: com.unity3d.services.banners.BannerError, message: String) {
                Log.e("AdsManager", "Unity banner error: $error - $message")
                onError(message)
            }
        }
    }

    override fun destroyBannerAd() {
        stopBannerAutoRefresh()
        bannerAd?.destroy()
        bannerAd = null
        unityBannerView?.destroy()
        unityBannerView = null
    }

    override fun setConsent(consent: Boolean) {
        // Store consent for AdMob
        preferencesRepository.setAdPersonalizationConsent(consent).subscribe()
        
        // Unity Ads consent
        if (unityInitialized) {
            UnityAds.setConsentMetaData(com.unity3d.ads.metadata.ConsentMetaData(context).apply {
                setConsent(if (consent) "true" else "false")
                commit()
            })
        }
    }

    override fun requestConsentInfoUpdate() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(context, params, {
            if (consentInformation.isConsentFormAvailable) {
                loadConsentForm()
            }
        }, { error ->
            Log.e("AdsManager", "Consent info update failed: $error")
        })
    }

    private fun loadConsentForm() {
        UserMessagingPlatform.loadConsentForm(context, { form ->
            consentForm = form
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                form.show(context as Activity)
            }
        }, { error ->
            Log.e("AdsManager", "Consent form load failed: $error")
        })
    }

    override fun isRewardedAdReady(placementId: String): Boolean {
        val adMobEnabled = isAdMobEnabled()
        val unityEnabled = isUnityAdsEnabled()
        return (adMobEnabled && rewardedAd != null) || (unityInitialized && unityEnabled && UnityAds.isReady(placementId))
    }

    override fun isInterstitialAdReady(placementId: String): Boolean {
        val adMobEnabled = isAdMobEnabled()
        val unityEnabled = isUnityAdsEnabled()
        return (adMobEnabled && interstitialAd != null) || (unityInitialized && unityEnabled && UnityAds.isReady(placementId))
    }

    override fun onDestroy() {
        isDestroyed = true
        coroutineScope.cancel()
        disposables.clear()
        stopBannerAutoRefresh()
        handler.removeCallbacksAndMessages(null)
        destroyBannerAd()
        
        rewardedAd = null
        interstitialAd = null
        bannerAd = null
        unityRewardedPlacement = null
        unityInterstitialPlacement = null
    }

    companion object {
        fun initialize(context: Context) {
            // Called from MyApplication
        }
    }
}