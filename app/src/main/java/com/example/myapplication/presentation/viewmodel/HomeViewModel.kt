package com.example.myapplication.presentation.viewmodel

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.ads.RewardOutcome
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.quiz.DayClock
import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.domain.quiz.QuizRepository
import com.example.myapplication.domain.quiz.StreakCalculator
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
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val rewardPoints: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val dailyCompletedToday: Boolean = false,
    val questionCounts: Map<QuizCategory, Int> = emptyMap(),
    val isRewardedAdReady: Boolean = false,
    val showBanner: Boolean = false,
    val bannerAdUnitId: String = ""
)

sealed interface HomeEvent {
    data class RewardGranted(val amount: Int) : HomeEvent
    data class Message(@StringRes val res: Int) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val adsManager: AdsManager,
    private val preferencesRepository: PreferencesRepository,
    private val quizRepository: QuizRepository,
    observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        observeCurrentUser(),
        preferencesRepository.settings,
        adsManager.state,
        quizRepository.observeQuestionCounts()
    ) { user, settings, ads, counts ->
        val today = DayClock.epochDay()
        HomeUiState(
            user = user,
            rewardPoints = settings.rewardPoints,
            streak = StreakCalculator.displayedStreak(settings.streak, today),
            bestStreak = settings.streak.best,
            dailyCompletedToday = StreakCalculator.isCompletedToday(settings.streak, today),
            questionCounts = counts,
            isRewardedAdReady = ads.isRewardedReady,
            showBanner = ads.canRequestAds && settings.adMobEnabled,
            bannerAdUnitId = adsManager.bannerAdUnitId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    init {
        adsManager.preloadAds()
        viewModelScope.launch {
            runCatching { quizRepository.ensureSeeded() }
                .onFailure { Timber.e(it, "Question bank import failed") }
        }
    }

    /** Optional, user-initiated rewarded video for bonus points. */
    fun showRewardedAd(activity: Activity) {
        if (adsManager.isFullScreenAdShowing) return
        adsManager.showRewardedAd(activity) { outcome ->
            viewModelScope.launch {
                when (outcome) {
                    is RewardOutcome.Earned -> {
                        preferencesRepository.addRewardPoints(outcome.amount)
                        _events.send(HomeEvent.RewardGranted(outcome.amount))
                    }
                    RewardOutcome.Skipped -> _events.send(HomeEvent.Message(R.string.reward_skipped))
                    is RewardOutcome.Unavailable -> _events.send(HomeEvent.Message(R.string.ad_unavailable))
                }
            }
        }
    }
}
