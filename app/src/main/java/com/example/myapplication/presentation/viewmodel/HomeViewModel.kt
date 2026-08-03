package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ads.AdsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val adsManager: AdsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<HomeEvent?>(null)
    val events: kotlinx.coroutines.flow.StateFlow<HomeEvent?> = _events.asStateFlow()

    init {
        loadAds()
    }

    private fun loadAds() {
        viewModelScope.launch {
            adsManager.loadRewardedAd("rewarded")
            adsManager.loadInterstitialAd("interstitial")
        }
    }

    fun showRewardedAd() {
        viewModelScope.launch {
            adsManager.showRewardedAd("rewarded") { rewarded ->
                if (rewarded) {
                    _events.value = HomeEvent.RewardGranted
                } else {
                    _events.value = HomeEvent.ShowMessage("Ad not available, try again later")
                }
            }
        }
    }

    fun showInterstitialAd() {
        viewModelScope.launch {
            adsManager.showInterstitialAd("interstitial") {
                // Interstitial closed
            }
        }
    }

    fun navigateToSettings() {
        _events.value = HomeEvent.NavigateToSettings
    }

    fun consumeEvent(): HomeEvent? {
        return _events.value?.also { _events.value = null }
    }
}

data class HomeUiState(
    val isRewardedAdReady: Boolean = false,
    val isInterstitialAdReady: Boolean = false
)

sealed interface HomeEvent {
    data class NavigateToSettings : HomeEvent
    data class RewardGranted : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
}