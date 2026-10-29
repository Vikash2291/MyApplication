package com.example.myapplication.data.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.example.myapplication.R
import com.example.myapplication.data.local.preferences.AppSettings
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.metadata.MetaData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : AdsManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val sdkInitStarted = AtomicBoolean(false)

    private val _state = MutableStateFlow(AdsState())
    override val state: StateFlow<AdsState> = _state.asStateFlow()

    private var settings = AppSettings()

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false
    private var unityInitialized = false
    private var unityRewardedLoaded = false
    private var unityInterstitialLoaded = false
    private var lastInterstitialShownAt = 0L
    private var isShowingFullScreenAd = false

    private val rewardedUnitId get() = context.getString(R.string.admob_rewarded_ad_unit_id)
    private val interstitialUnitId get() = context.getString(R.string.admob_interstitial_ad_unit_id)
    private val unityGameId get() = context.getString(R.string.unity_game_id)
    private val unityRewardedPlacement get() = context.getString(R.string.unity_rewarded_placement_id)
    private val unityInterstitialPlacement get() = context.getString(R.string.unity_interstitial_placement_id)

    override val bannerAdUnitId: String get() = context.getString(R.string.admob_banner_ad_unit_id)
    override val bannersEnabled: Boolean get() = settings.adMobEnabled && _state.value.canRequestAds

    init {
        scope.launch {
            preferencesRepository.settings.collect { newSettings ->
                val networksChanged = newSettings.adMobEnabled != settings.adMobEnabled ||
                    newSettings.unityAdsEnabled != settings.unityAdsEnabled
                settings = newSettings
                if (newSettings.adMobEnabled) {
                    // nothing to do: AdMob ads load lazily
                } else {
                    rewardedAd = null
                    interstitialAd = null
                }
                applyUnityConsent()
                if (networksChanged && _state.value.isInitialized) preloadAds()
                refreshReadiness()
            }
        }
    }

    // region Consent + init

    override fun gatherConsentAndInitialize(activity: Activity) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Timber.w("Consent form error: %s", it.message) }
                    onConsentResolved()
                }
            },
            { requestError ->
                Timber.w("Consent info update failed: %s", requestError.message)
                onConsentResolved()
            }
        )

        // Consent obtained in a previous session lets us start immediately.
        if (consentInformation.canRequestAds()) onConsentResolved()
    }

    override fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            error?.let { Timber.w("Privacy options error: %s", it.message) }
            onConsentResolved()
        }
    }

    private fun onConsentResolved() {
        val canRequest = consentInformation.canRequestAds()
        _state.update {
            it.copy(
                canRequestAds = canRequest,
                isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            )
        }
        scope.launch {
            preferencesRepository.setGdprConsent(canRequest)
        }
        if (canRequest && sdkInitStarted.compareAndSet(false, true)) initializeSdks()
    }

    private fun initializeSdks() {
        MobileAds.initialize(context) {
            Timber.d("AdMob initialised")
            _state.update { it.copy(isInitialized = true) }
            preloadAds()
        }
        initializeUnity()
    }

    private fun initializeUnity() {
        if (unityGameId.isBlank() || UnityAds.isInitialized) return
        applyUnityConsent()
        UnityAds.initialize(
            context,
            unityGameId,
            com.example.myapplication.BuildConfig.DEBUG,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    unityInitialized = true
                    loadUnityAds()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Timber.w("Unity Ads init failed: %s %s", error, message)
                }
            }
        )
    }

    private fun applyUnityConsent() {
        runCatching {
            MetaData(context).apply {
                set("gdpr.consent", settings.gdprConsent && settings.adPersonalizationConsent)
                commit()
            }
            MetaData(context).apply {
                set("privacy.consent", settings.adPersonalizationConsent)
                commit()
            }
        }
    }

    // endregion

    // region Loading

    override fun preloadAds() {
        if (!_state.value.canRequestAds) return
        if (settings.adMobEnabled) {
            loadRewarded()
            loadInterstitial()
        }
        loadUnityAds()
    }

    private fun adRequest(): AdRequest = AdRequest.Builder().build()

    private fun loadRewarded() {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        RewardedAd.load(context, rewardedUnitId, adRequest(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingRewarded = false
                refreshReadiness()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Timber.d("Rewarded failed to load: %s", error.message)
                rewardedAd = null
                isLoadingRewarded = false
                refreshReadiness()
            }
        })
    }

    private fun loadInterstitial() {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        InterstitialAd.load(context, interstitialUnitId, adRequest(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoadingInterstitial = false
                refreshReadiness()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Timber.d("Interstitial failed to load: %s", error.message)
                interstitialAd = null
                isLoadingInterstitial = false
                refreshReadiness()
            }
        })
    }

    private fun loadUnityAds() {
        if (!unityInitialized || !settings.unityAdsEnabled) return
        if (!unityRewardedLoaded) loadUnityPlacement(unityRewardedPlacement) { unityRewardedLoaded = it }
        if (!unityInterstitialLoaded) loadUnityPlacement(unityInterstitialPlacement) { unityInterstitialLoaded = it }
    }

    private fun loadUnityPlacement(placementId: String, onLoaded: (Boolean) -> Unit) {
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                scope.launch { onLoaded(true); refreshReadiness() }
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                Timber.d("Unity load failed for %s: %s", placementId, message)
                scope.launch { onLoaded(false); refreshReadiness() }
            }
        })
    }

    private fun refreshReadiness() {
        _state.update {
            it.copy(
                isRewardedReady = (settings.adMobEnabled && rewardedAd != null) ||
                    (settings.unityAdsEnabled && unityRewardedLoaded),
                isInterstitialReady = (settings.adMobEnabled && interstitialAd != null) ||
                    (settings.unityAdsEnabled && unityInterstitialLoaded)
            )
        }
    }

    // endregion

    // region Showing

    override fun showRewardedAd(activity: Activity, onResult: (RewardOutcome) -> Unit) {
        if (isShowingFullScreenAd) return
        val ad = rewardedAd.takeIf { settings.adMobEnabled }
        when {
            ad != null -> showAdMobRewarded(activity, ad, onResult)
            settings.unityAdsEnabled && unityRewardedLoaded -> showUnity(activity, unityRewardedPlacement) { completed ->
                unityRewardedLoaded = false
                onResult(if (completed) RewardOutcome.Earned(DEFAULT_REWARD) else RewardOutcome.Skipped)
            }
            else -> {
                preloadAds()
                onResult(RewardOutcome.Unavailable("No ad available right now. Please try again shortly."))
            }
        }
    }

    private fun showAdMobRewarded(activity: Activity, ad: RewardedAd, onResult: (RewardOutcome) -> Unit) {
        var earned: Int? = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishFullScreen()
                onResult(earned?.let { RewardOutcome.Earned(it) } ?: RewardOutcome.Skipped)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                finishFullScreen()
                onResult(RewardOutcome.Unavailable(error.message))
            }
        }
        rewardedAd = null
        isShowingFullScreenAd = true
        refreshReadiness()
        ad.show(activity) { reward -> earned = reward.amount.takeIf { it > 0 } ?: DEFAULT_REWARD }
    }

    override fun showInterstitialAd(activity: Activity, force: Boolean, onDismissed: () -> Unit) {
        val now = SystemClock.elapsedRealtime()
        val capped = !force && lastInterstitialShownAt != 0L &&
            now - lastInterstitialShownAt < INTERSTITIAL_MIN_INTERVAL_MS
        if (capped || isShowingFullScreenAd) {
            onDismissed()
            return
        }

        val ad = interstitialAd.takeIf { settings.adMobEnabled }
        when {
            ad != null -> {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        finishFullScreen()
                        onDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        finishFullScreen()
                        onDismissed()
                    }
                }
                interstitialAd = null
                isShowingFullScreenAd = true
                lastInterstitialShownAt = now
                refreshReadiness()
                ad.show(activity)
            }
            settings.unityAdsEnabled && unityInterstitialLoaded -> {
                lastInterstitialShownAt = now
                showUnity(activity, unityInterstitialPlacement) {
                    unityInterstitialLoaded = false
                    onDismissed()
                }
            }
            else -> {
                preloadAds()
                onDismissed()
            }
        }
    }

    private fun showUnity(activity: Activity, placementId: String, onFinished: (completed: Boolean) -> Unit) {
        isShowingFullScreenAd = true
        refreshReadiness()
        UnityAds.show(activity, placementId, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String?,
                error: UnityAds.UnityAdsShowError?,
                message: String?
            ) {
                scope.launch { finishFullScreen(); onFinished(false) }
            }

            override fun onUnityAdsShowStart(placementId: String?) = Unit
            override fun onUnityAdsShowClick(placementId: String?) = Unit

            override fun onUnityAdsShowComplete(
                placementId: String?,
                state: UnityAds.UnityAdsShowCompletionState?
            ) {
                scope.launch {
                    finishFullScreen()
                    onFinished(state == UnityAds.UnityAdsShowCompletionState.COMPLETED)
                }
            }
        })
    }

    private fun finishFullScreen() {
        isShowingFullScreenAd = false
        refreshReadiness()
        preloadAds()
    }

    // endregion

    private companion object {
        const val DEFAULT_REWARD = 10
        const val INTERSTITIAL_MIN_INTERVAL_MS = 120_000L // at most one interstitial every 2 minutes
    }
}
