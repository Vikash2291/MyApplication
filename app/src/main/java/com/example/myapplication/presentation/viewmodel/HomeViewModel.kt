package com.example.myapplication.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.ads.RewardOutcome
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val rewardPoints: Int = 0,
    val isRewardedAdReady: Boolean = false,
    val isInterstitialAdReady: Boolean = false,
    val showBanner: Boolean = false,
    val bannerAdUnitId: String = ""
)

sealed interface HomeEvent {
    data class RewardGranted(val amount: Int) : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val adsManager: AdsManager,
    private val preferencesRepository: PreferencesRepository,
    observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        observeCurrentUser(),
        preferencesRepository.settings,
        adsManager.state
    ) { user, settings, ads ->
        HomeUiState(
            user = user,
            rewardPoints = settings.rewardPoints,
            isRewardedAdReady = ads.isRewardedReady,
            isInterstitialAdReady = ads.isInterstitialReady,
            showBanner = ads.canRequestAds && settings.adMobEnabled,
            bannerAdUnitId = adsManager.bannerAdUnitId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    init {
        adsManager.preloadAds()
    }

    fun showRewardedAd(activity: Activity) {
        adsManager.showRewardedAd(activity) { outcome ->
            viewModelScope.launch {
                when (outcome) {
                    is RewardOutcome.Earned -> {
                        preferencesRepository.addRewardPoints(outcome.amount)
                        _events.send(HomeEvent.RewardGranted(outcome.amount))
                    }
                    RewardOutcome.Skipped ->
                        _events.send(HomeEvent.ShowMessage("Watch the full video to earn the reward"))
                    is RewardOutcome.Unavailable -> _events.send(HomeEvent.ShowMessage(outcome.reason))
                }
            }
        }
    }

    /** Call at natural breaks (e.g. leaving a screen); the manager applies a frequency cap. */
    fun maybeShowInterstitial(activity: Activity, onContinue: () -> Unit) {
        adsManager.showInterstitialAd(activity, onDismissed = onContinue)
    }
}
