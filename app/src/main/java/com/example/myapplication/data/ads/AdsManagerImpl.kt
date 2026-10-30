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

/**
 * AdMob Mediation only. Unity Ads participates as a bidding ad source through the
 * `com.google.ads.mediation:unity` adapter and is configured in the AdMob dashboard
 * (mediation group), so there is no Unity-specific code or placement handling here.
 */
@Singleton
class AdsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appOpenAdManager: AppOpenAdManager,
    private val fullScreenTracker: FullScreenAdTracker
) : AdsManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val sdkInitStarted = AtomicBoolean(false)
    private val interstitialPacer = InterstitialPacer(clock = SystemClock::elapsedRealtime)

    private val _state = MutableStateFlow(AdsState())
    override val state: StateFlow<AdsState> = _state.asStateFlow()

    private var settings = AppSettings()

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false

    private val rewardedUnitId get() = context.getString(R.string.admob_rewarded_ad_unit_id)
    private val interstitialUnitId get() = context.getString(R.string.admob_interstitial_ad_unit_id)

    override val bannerAdUnitId: String get() = context.getString(R.string.admob_banner_ad_unit_id)
    override val isFullScreenAdShowing: Boolean get() = fullScreenTracker.isShowing

    private val adsAllowed: Boolean get() = settings.adMobEnabled && _state.value.canRequestAds

    init {
        scope.launch {
            preferencesRepository.settings.collect { newSettings ->
                val enabledChanged = newSettings.adMobEnabled != settings.adMobEnabled
                settings = newSettings
                if (!newSettings.adMobEnabled) {
                    rewardedAd = null
                    interstitialAd = null
                }
                if (enabledChanged && _state.value.isInitialized) {
                    appOpenAdManager.setEnabled(adsAllowed)
                    preloadAds()
                }
                refreshReadiness()
            }
        }
    }

    // region Consent + init

    override fun gatherConsentAndInitialize(activity: Activity) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        // Required on every launch: refreshes consent status and privacy-options requirement.
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

        // Consent obtained in a previous session lets ads start loading in parallel.
        if (consentInformation.canRequestAds()) initializeIfAllowed()
    }

    override fun showPrivacyOptionsForm(activity: Activity, onDismissed: (error: String?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            error?.let { Timber.w("Privacy options error: %s", it.message) }
            onConsentResolved()
            onDismissed(error?.message)
        }
    }

    private fun onConsentResolved() {
        val canRequest = consentInformation.canRequestAds()
        _state.update {
            it.copy(
                isConsentResolved = true,
                canRequestAds = canRequest,
                isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            )
        }
        scope.launch { preferencesRepository.setGdprConsent(canRequest) }
        if (canRequest) {
            initializeIfAllowed()
        } else {
            // Consent withdrawn: drop anything cached so nothing is shown.
            rewardedAd = null
            interstitialAd = null
            appOpenAdManager.setEnabled(false)
            refreshReadiness()
        }
    }

    private fun initializeIfAllowed() {
        _state.update { it.copy(canRequestAds = true) }
        if (!sdkInitStarted.compareAndSet(false, true)) {
            if (_state.value.isInitialized) {
                appOpenAdManager.setEnabled(adsAllowed)
                preloadAds()
            }
            return
        }
        // MobileAds.initialize does disk/network work: start it off the main thread.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(context) {
                scope.launch {
                    Timber.d("Mobile Ads initialised (mediation adapters included)")
                    _state.update { it.copy(isInitialized = true) }
                    appOpenAdManager.setEnabled(adsAllowed)
                    preloadAds()
                }
            }
        }
    }

    // endregion

    // region Loading

    override fun preloadAds() {
        if (!_state.value.isInitialized || !adsAllowed) return
        loadRewarded()
        loadInterstitial()
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

    private fun refreshReadiness() {
        _state.update {
            it.copy(
                isRewardedReady = adsAllowed && rewardedAd != null,
                isInterstitialReady = adsAllowed && interstitialAd != null
            )
        }
    }

    // endregion

    // region Showing

    override fun showRewardedAd(activity: Activity, onResult: (RewardOutcome) -> Unit) {
        // Always answer the caller: the quiz pauses its timer until it hears back.
        if (fullScreenTracker.isShowing) {
            onResult(RewardOutcome.Unavailable("Another ad is showing."))
            return
        }
        val ad = rewardedAd.takeIf { adsAllowed }
        if (ad == null) {
            preloadAds()
            onResult(RewardOutcome.Unavailable("No video available right now. Please try again shortly."))
            return
        }
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
        fullScreenTracker.setShowing(true)
        refreshReadiness()
        ad.show(activity) { reward -> earned = reward.amount.takeIf { it > 0 } ?: DEFAULT_REWARD }
    }

    override fun onNaturalBreak(activity: Activity, onContinue: () -> Unit) {
        interstitialPacer.recordUserAction()
        val ad = interstitialAd.takeIf { adsAllowed }
        if (ad == null || fullScreenTracker.isShowing || !interstitialPacer.canShow()) {
            if (ad == null) preloadAds()
            onContinue()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishFullScreen()
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                finishFullScreen()
                onContinue()
            }
        }
        interstitialAd = null
        fullScreenTracker.setShowing(true)
        interstitialPacer.onShown()
        refreshReadiness()
        ad.show(activity)
    }

    /** Clears the full-screen flag and immediately loads the next ad of each format. */
    private fun finishFullScreen() {
        fullScreenTracker.setShowing(false)
        refreshReadiness()
        preloadAds()
    }

    // endregion

    private companion object {
        const val DEFAULT_REWARD = 10
    }
}
