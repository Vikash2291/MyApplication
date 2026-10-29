package com.example.myapplication.data.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class AdsState(
    val isInitialized: Boolean = false,
    val canRequestAds: Boolean = false,
    val isRewardedReady: Boolean = false,
    val isInterstitialReady: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false
)

/** Outcome of showing a rewarded ad. */
sealed interface RewardOutcome {
    data class Earned(val amount: Int) : RewardOutcome
    data object Skipped : RewardOutcome
    data class Unavailable(val reason: String) : RewardOutcome
}

/**
 * AdMob is the primary network; Unity Ads is used as a fallback whenever an AdMob
 * full-screen ad is not ready. Consent is gathered with Google UMP before any request.
 */
interface AdsManager {
    val state: StateFlow<AdsState>

    /** Gathers UMP consent (showing the form if required) and initialises SDKs. */
    fun gatherConsentAndInitialize(activity: Activity)
    fun showPrivacyOptionsForm(activity: Activity)

    fun preloadAds()
    fun showRewardedAd(activity: Activity, onResult: (RewardOutcome) -> Unit)

    /** Shows an interstitial if one is ready and the frequency cap allows it. */
    fun showInterstitialAd(activity: Activity, force: Boolean = false, onDismissed: () -> Unit = {})

    val bannerAdUnitId: String
    val bannersEnabled: Boolean
}
