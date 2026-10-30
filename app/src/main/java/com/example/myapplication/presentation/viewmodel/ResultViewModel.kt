package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.domain.quiz.AttemptReview
import com.example.myapplication.domain.quiz.DayClock
import com.example.myapplication.domain.quiz.QuizRepository
import com.example.myapplication.domain.quiz.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val isLoading: Boolean = true,
    val review: AttemptReview? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val streak: Int = 0,
    val totalPoints: Int = 0,
    val bookmarkIds: Set<String> = emptySet(),
    val showBanner: Boolean = false,
    val bannerAdUnitId: String = ""
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    preferencesRepository: PreferencesRepository,
    private val adsManager: AdsManager
) : ViewModel() {

    private val attemptId: Long = savedStateHandle.get<Long>(ARG_ATTEMPT_ID) ?: -1L
    private val review = MutableStateFlow<AttemptReview?>(null)
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<ResultUiState> = combine(
        review,
        loading,
        preferencesRepository.settings,
        quizRepository.observeBookmarkIds(),
        adsManager.state
    ) { r, isLoading, settings, bookmarks, ads ->
        ResultUiState(
            isLoading = isLoading,
            review = r,
            language = settings.language,
            streak = StreakCalculator.displayedStreak(settings.streak, DayClock.epochDay()),
            totalPoints = settings.rewardPoints,
            bookmarkIds = bookmarks,
            showBanner = ads.canRequestAds && settings.adMobEnabled,
            bannerAdUnitId = adsManager.bannerAdUnitId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultUiState())

    init {
        viewModelScope.launch {
            review.value = runCatching { quizRepository.getAttemptReview(attemptId) }.getOrNull()
            loading.value = false
        }
    }

    fun toggleBookmark(questionId: String) {
        val bookmarked = questionId in uiState.value.bookmarkIds
        viewModelScope.launch { quizRepository.setBookmarked(questionId, !bookmarked) }
    }

    /** Result -> Home is the one natural break between quizzes where an interstitial may show. */
    fun onNaturalBreak(activity: android.app.Activity, onContinue: () -> Unit) =
        adsManager.onNaturalBreak(activity, onContinue)

    companion object {
        const val ARG_ATTEMPT_ID = "attemptId"
    }
}
