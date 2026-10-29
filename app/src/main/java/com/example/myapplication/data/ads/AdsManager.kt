package com.example.myapplication.data.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class AdsState(
    /** UMP finished its first update of this launch (successfully or not). */
    val isConsentResolved: Boolean = false,
    val canRequestAds: Boolean = false,
    val isInitialized: Boolean = false,
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
 * Single entry point for ads. All demand goes through AdMob Mediation; Unity Ads takes part as
 * a bidding source configured in the AdMob dashboard (no Unity code in the app). Consent is
 * gathered with Google UMP on every launch, and nothing is requested before canRequestAds().
 */
interface AdsManager {
    val state: StateFlow<AdsState>

    /** Requests a UMP consent update (every launch), shows the form if required, then initialises. */
    fun gatherConsentAndInitialize(activity: Activity)

    /** Opens the UMP privacy options form (only offered when it is required). */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (error: String?) -> Unit = {})

    fun preloadAds()

    /** User-initiated, opt-in rewarded ad. */
    fun showRewardedAd(activity: Activity, onResult: (RewardOutcome) -> Unit)

    /**
     * Call at a natural break that follows a user action (e.g. navigating to another screen).
     * Records the action, then shows an interstitial only if the pacing rules allow it.
     * [onContinue] always runs exactly once: immediately, or after the ad is dismissed.
     */
    fun onNaturalBreak(activity: Activity, onContinue: () -> Unit)

    /** True while an interstitial, rewarded or app open ad is on screen. */
    val isFullScreenAdShowing: Boolean

    val bannerAdUnitId: String
}
