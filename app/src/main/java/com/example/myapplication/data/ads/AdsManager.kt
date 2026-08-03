package com.example.myapplication.data.ads

interface AdsManager {
    fun initialize()
    fun loadRewardedAd(placementId: String)
    fun showRewardedAd(placementId: String, onReward: (Boolean) -> Unit)
    fun loadInterstitialAd(placementId: String)
    fun showInterstitialAd(placementId: String, onComplete: () -> Unit)
    fun loadBannerAd(adUnitId: String, onLoad: () -> Unit, onError: (String) -> Unit)
    fun destroyBannerAd()
    fun setConsent(consent: Boolean)
    fun requestConsentInfoUpdate()
    fun isRewardedAdReady(placementId: String): Boolean
    fun isInterstitialAdReady(placementId: String): Boolean
    fun onDestroy()
}