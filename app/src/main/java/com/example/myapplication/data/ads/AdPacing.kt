package com.example.myapplication.data.ads

/**
 * Pure, clock-injected pacing rules for full-screen ads, kept free of Android types so they
 * can be unit tested.
 *
 * Interstitial policy (AdMob "one interstitial per two user actions", no launch/exit ads):
 *  - at most one interstitial per [minActionsBetween] user actions (actions start at 0 on
 *    launch, so the first interstitial can never appear before the user has acted twice), and
 *  - at least [minIntervalMs] between two interstitials.
 */
class InterstitialPacer(
    private val minActionsBetween: Int = 2,
    private val minIntervalMs: Long = 2 * 60 * 1000L,
    private val clock: () -> Long
) {
    private var actionsSinceLastAd = 0
    private var lastShownAt: Long? = null

    /** Records a deliberate user action (e.g. a navigation tap) that ends at a natural break. */
    fun recordUserAction() {
        actionsSinceLastAd++
    }

    fun canShow(): Boolean {
        if (actionsSinceLastAd < minActionsBetween) return false
        val last = lastShownAt ?: return true
        return clock() - last >= minIntervalMs
    }

    fun onShown() {
        actionsSinceLastAd = 0
        lastShownAt = clock()
    }
}

/**
 * App open policy:
 *  - never on the first cold-start foreground (consent may still be in progress and AdMob
 *    asks not to interrupt the very first launch), only on a later return to foreground,
 *  - only when consent allows ad requests,
 *  - never while another full-screen ad (interstitial / rewarded) is showing,
 *  - at most once per [minIntervalMs] (4 hours),
 *  - only with an ad younger than [maxAdAgeMs] (Google expires app open ads after 4 hours).
 */
class AppOpenPacer(
    private val minIntervalMs: Long = 4 * 60 * 60 * 1000L,
    private val maxAdAgeMs: Long = 4 * 60 * 60 * 1000L,
    private val clock: () -> Long
) {
    private var lastShownAt: Long? = null

    fun isAdFresh(loadedAt: Long?): Boolean =
        loadedAt != null && clock() - loadedAt < maxAdAgeMs

    fun canShow(
        isColdStartForeground: Boolean,
        canRequestAds: Boolean,
        isOtherFullScreenAdShowing: Boolean,
        adLoadedAt: Long?
    ): Boolean {
        if (isColdStartForeground || !canRequestAds || isOtherFullScreenAdShowing) return false
        if (!isAdFresh(adLoadedAt)) return false
        val last = lastShownAt ?: return true
        return clock() - last >= minIntervalMs
    }

    fun onShown() {
        lastShownAt = clock()
    }
}
