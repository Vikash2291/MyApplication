package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.DayClock
import com.example.myapplication.domain.quiz.QuizRepository
import com.example.myapplication.domain.quiz.QuizStats
import com.example.myapplication.domain.quiz.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val stats: QuizStats = QuizStats(),
    val points: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    quizRepository: QuizRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        quizRepository.observeStats(),
        preferencesRepository.settings
    ) { stats, settings ->
        StatsUiState(
            stats = stats,
            points = settings.rewardPoints,
            streak = StreakCalculator.displayedStreak(settings.streak, DayClock.epochDay()),
            bestStreak = settings.streak.best
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
