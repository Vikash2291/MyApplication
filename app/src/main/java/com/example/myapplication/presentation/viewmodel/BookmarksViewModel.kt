package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.domain.quiz.Question
import com.example.myapplication.domain.quiz.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val language: AppLanguage = AppLanguage.ENGLISH
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<BookmarksUiState> = combine(
        quizRepository.observeBookmarkedQuestions(),
        preferencesRepository.settings
    ) { questions, settings ->
        BookmarksUiState(isLoading = false, questions = questions, language = settings.language)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookmarksUiState())

    fun removeBookmark(questionId: String) {
        viewModelScope.launch { quizRepository.setBookmarked(questionId, false) }
    }
}
