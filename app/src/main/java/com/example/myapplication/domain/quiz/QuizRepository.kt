package com.example.myapplication.domain.quiz

import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    /** Imports the bundled question bank into Room when it is missing or out of date. */
    suspend fun ensureSeeded()

    suspend fun questionsFor(mode: QuizMode, category: QuizCategory?, epochDay: Long, count: Int = DEFAULT_QUIZ_SIZE): List<Question>

    /** Stores a finished quiz and updates per-question and per-category progress. Returns the attempt id. */
    suspend fun saveAttempt(
        mode: QuizMode,
        category: QuizCategory?,
        epochDay: Long,
        answers: List<AnswerRecord>,
        score: QuizScore
    ): Long

    suspend fun getAttemptReview(attemptId: Long): AttemptReview?

    fun observeBookmarkIds(): Flow<Set<String>>
    fun observeBookmarkedQuestions(): Flow<List<Question>>
    suspend fun setBookmarked(questionId: String, bookmarked: Boolean)

    fun observeStats(): Flow<QuizStats>
    fun observeQuestionCounts(): Flow<Map<QuizCategory, Int>>

    /** Deletes attempts, progress and bookmarks (used on account deletion). The bank stays. */
    suspend fun clearUserData()

    companion object {
        const val DEFAULT_QUIZ_SIZE = 10
    }
}
