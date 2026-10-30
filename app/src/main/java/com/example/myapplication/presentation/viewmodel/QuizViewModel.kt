package com.example.myapplication.presentation.viewmodel

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.ads.RewardOutcome
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AnswerRecord
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.domain.quiz.DayClock
import com.example.myapplication.domain.quiz.Question
import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.domain.quiz.QuizMode
import com.example.myapplication.domain.quiz.QuizRepository
import com.example.myapplication.domain.quiz.QuizScoring
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val mode: QuizMode = QuizMode.CATEGORY,
    val category: QuizCategory? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val questions: List<Question> = emptyList(),
    val index: Int = 0,
    /** null = not answered yet; [AnswerRecord.TIMED_OUT] = time ran out. */
    val selectedIndex: Int? = null,
    val answers: List<AnswerRecord> = emptyList(),
    val secondsLeft: Int = QuizViewModel.SECONDS_PER_QUESTION,
    val hiddenOptions: Set<Int> = emptySet(),
    val hintUsed: Boolean = false,
    val extraTimeUsed: Boolean = false,
    val isRewardedReady: Boolean = false,
    val isAdShowing: Boolean = false,
    val bookmarkIds: Set<String> = emptySet(),
    val isSaving: Boolean = false
) {
    val current: Question? get() = questions.getOrNull(index)
    val isAnswered: Boolean get() = selectedIndex != null
    val isLast: Boolean get() = index == questions.lastIndex
    val correctSoFar: Int get() = answers.count { it.isCorrect }
}

sealed interface QuizEvent {
    data class Finished(val attemptId: Long) : QuizEvent
    data class Message(@StringRes val res: Int) : QuizEvent
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val preferencesRepository: PreferencesRepository,
    private val adsManager: AdsManager
) : ViewModel() {

    private val mode: QuizMode =
        runCatching { QuizMode.valueOf(savedStateHandle.get<String>(ARG_MODE).orEmpty()) }.getOrDefault(QuizMode.CATEGORY)
    private val category: QuizCategory? = QuizCategory.fromName(savedStateHandle.get<String>(ARG_CATEGORY))
    private val epochDay = DayClock.epochDay()

    private val _uiState = MutableStateFlow(QuizUiState(mode = mode, category = category))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = Channel<QuizEvent>(Channel.BUFFERED)
    val events: Flow<QuizEvent> = _events.receiveAsFlow()

    private var timerJob: Job? = null
    private var timerPaused = false

    init {
        viewModelScope.launch {
            preferencesRepository.settings.collect { s -> _uiState.update { it.copy(language = s.language) } }
        }
        viewModelScope.launch {
            adsManager.state.collect { a -> _uiState.update { it.copy(isRewardedReady = a.isRewardedReady) } }
        }
        viewModelScope.launch {
            quizRepository.observeBookmarkIds().collect { ids -> _uiState.update { it.copy(bookmarkIds = ids) } }
        }
        adsManager.preloadAds()
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val questions = runCatching { quizRepository.questionsFor(mode, category, epochDay) }
                .onFailure { Timber.e(it, "Failed to load questions") }
                .getOrDefault(emptyList())
            _uiState.update { it.copy(isLoading = false, isEmpty = questions.isEmpty(), questions = questions) }
            if (questions.isNotEmpty()) startTimer(SECONDS_PER_QUESTION)
        }
    }

    // region Timer

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        timerPaused = false
        _uiState.update { it.copy(secondsLeft = seconds) }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                if (timerPaused) continue
                val left = _uiState.value.secondsLeft - 1
                _uiState.update { it.copy(secondsLeft = left.coerceAtLeast(0)) }
                if (left <= 0) {
                    onTimeUp()
                    break
                }
            }
        }
    }

    /** Stops the clock while the app is in the background so the user isn't penalised. */
    fun setPaused(paused: Boolean) {
        if (!_uiState.value.isAdShowing) timerPaused = paused
    }

    private fun onTimeUp() {
        val s = _uiState.value
        val q = s.current ?: return
        if (s.isAnswered) return
        recordAnswer(q, AnswerRecord.TIMED_OUT)
    }

    // endregion

    fun selectOption(optionIndex: Int) {
        val s = _uiState.value
        val q = s.current ?: return
        if (s.isAnswered || s.isAdShowing || optionIndex in s.hiddenOptions) return
        timerJob?.cancel()
        recordAnswer(q, optionIndex)
    }

    private fun recordAnswer(question: Question, selected: Int) {
        _uiState.update {
            it.copy(
                selectedIndex = selected,
                answers = it.answers + AnswerRecord(question.id, selected, question.answerIndex)
            )
        }
    }

    fun next() {
        val s = _uiState.value
        if (!s.isAnswered || s.isSaving) return
        if (s.isLast) {
            finish()
        } else {
            _uiState.update {
                it.copy(
                    index = it.index + 1,
                    selectedIndex = null,
                    hiddenOptions = emptySet(),
                    hintUsed = false,
                    extraTimeUsed = false
                )
            }
            startTimer(SECONDS_PER_QUESTION)
        }
    }

    private fun finish() {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val score = QuizScoring.score(s.answers, mode)
                val attemptId = quizRepository.saveAttempt(mode, category, epochDay, s.answers, score)
                if (score.points > 0) preferencesRepository.addRewardPoints(score.points)
                if (mode == QuizMode.DAILY) preferencesRepository.recordDailyQuizCompleted(epochDay)
                attemptId
            }.onSuccess { id ->
                _events.send(QuizEvent.Finished(id))
            }.onFailure {
                Timber.e(it, "Failed to save attempt")
                _uiState.update { st -> st.copy(isSaving = false) }
                _events.send(QuizEvent.Message(R.string.error_generic))
            }
        }
    }

    fun toggleBookmark() {
        val s = _uiState.value
        val q = s.current ?: return
        viewModelScope.launch { quizRepository.setBookmarked(q.id, q.id !in s.bookmarkIds) }
    }

    // region Rewarded lifelines (opt-in, user initiated)

    /** 50:50 — removes two wrong options after a completed rewarded video. */
    fun useHint(activity: Activity) {
        val s = _uiState.value
        if (s.isAnswered || s.hintUsed || s.current == null) return
        showRewarded(activity) {
            val q = _uiState.value.current ?: return@showRewarded
            val wrong = (0 until OPTION_COUNT).filter { it != q.answerIndex }.shuffled().take(2).toSet()
            _uiState.update { it.copy(hiddenOptions = wrong, hintUsed = true) }
            _events.trySend(QuizEvent.Message(R.string.hint_applied))
        }
    }

    /** Adds [EXTRA_SECONDS] to the current question after a completed rewarded video. */
    fun useExtraTime(activity: Activity) {
        val s = _uiState.value
        if (s.isAnswered || s.extraTimeUsed || s.current == null) return
        showRewarded(activity) {
            _uiState.update { it.copy(secondsLeft = it.secondsLeft + EXTRA_SECONDS, extraTimeUsed = true) }
            _events.trySend(QuizEvent.Message(R.string.extra_time_applied))
        }
    }

    private fun showRewarded(activity: Activity, onEarned: () -> Unit) {
        if (adsManager.isFullScreenAdShowing || _uiState.value.isAdShowing) return
        if (!_uiState.value.isRewardedReady) {
            _events.trySend(QuizEvent.Message(R.string.ad_unavailable))
            return
        }
        timerPaused = true
        _uiState.update { it.copy(isAdShowing = true) }
        adsManager.showRewardedAd(activity) { outcome ->
            _uiState.update { it.copy(isAdShowing = false) }
            timerPaused = false
            when (outcome) {
                is RewardOutcome.Earned -> if (!_uiState.value.isAnswered) onEarned()
                RewardOutcome.Skipped -> _events.trySend(QuizEvent.Message(R.string.reward_skipped))
                is RewardOutcome.Unavailable -> _events.trySend(QuizEvent.Message(R.string.ad_unavailable))
            }
        }
    }

    // endregion

    companion object {
        const val ARG_MODE = "mode"
        const val ARG_CATEGORY = "category"
        const val SECONDS_PER_QUESTION = 20
        const val EXTRA_SECONDS = 10
        private const val OPTION_COUNT = 4
        private const val TICK_MS = 1_000L
    }
}
