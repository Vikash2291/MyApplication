package com.example.myapplication.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.local.database.entity.AttemptAnswerEntity
import com.example.myapplication.data.local.database.entity.BookmarkEntity
import com.example.myapplication.data.local.database.entity.CategoryCoverageRow
import com.example.myapplication.data.local.database.entity.CategoryProgressEntity
import com.example.myapplication.data.local.database.entity.QuestionEntity
import com.example.myapplication.data.local.database.entity.QuestionProgressEntity
import com.example.myapplication.data.local.database.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizDao {

    // region Question bank

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    abstract suspend fun questionCount(): Int

    @Query("SELECT id FROM questions ORDER BY id")
    abstract suspend fun allQuestionIds(): List<String>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    abstract suspend fun questionsByIds(ids: List<String>): List<QuestionEntity>

    /** Least-seen questions first, random within the same "seen" count. */
    @Query(
        """
        SELECT q.* FROM questions q
        LEFT JOIN question_progress p ON p.questionId = q.id
        WHERE q.category = :category
        ORDER BY COALESCE(p.timesSeen, 0) ASC, RANDOM()
        LIMIT :limit
        """
    )
    abstract suspend fun questionsForCategory(category: String, limit: Int): List<QuestionEntity>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN bookmarks b ON b.questionId = q.id
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    abstract suspend fun randomBookmarkedQuestions(limit: Int): List<QuestionEntity>

    @Query(
        """
        SELECT q.category AS category,
               COUNT(q.id) AS totalQuestions,
               SUM(CASE WHEN COALESCE(p.timesSeen, 0) > 0 THEN 1 ELSE 0 END) AS seenQuestions
        FROM questions q
        LEFT JOIN question_progress p ON p.questionId = q.id
        GROUP BY q.category
        """
    )
    abstract fun observeCategoryCoverage(): Flow<List<CategoryCoverageRow>>

    // endregion

    // region Attempts & progress

    @Insert
    abstract suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Insert
    abstract suspend fun insertAnswers(answers: List<AttemptAnswerEntity>)

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    abstract suspend fun attemptById(id: Long): QuizAttemptEntity?

    @Query("SELECT * FROM attempt_answers WHERE attemptId = :attemptId ORDER BY position")
    abstract suspend fun answersForAttempt(attemptId: Long): List<AttemptAnswerEntity>

    @Query("SELECT * FROM question_progress WHERE questionId IN (:ids)")
    abstract suspend fun progressFor(ids: List<String>): List<QuestionProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertProgress(progress: List<QuestionProgressEntity>)

    @Query("SELECT * FROM category_progress WHERE category = :category")
    abstract suspend fun categoryProgress(category: String): CategoryProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertCategoryProgress(progress: List<CategoryProgressEntity>)

    @Query("SELECT * FROM category_progress")
    abstract fun observeCategoryProgress(): Flow<List<CategoryProgressEntity>>

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    abstract fun observeAttemptCount(): Flow<Int>

    /**
     * Saves a finished quiz atomically: the attempt, its answers, per-question progress and the
     * per-category totals. [categoryOf] maps question id -> category name.
     */
    @Transaction
    open suspend fun recordAttempt(
        attempt: QuizAttemptEntity,
        answers: List<AttemptAnswerEntity>,
        categoryOf: Map<String, String>,
        now: Long
    ): Long {
        val attemptId = insertAttempt(attempt)
        insertAnswers(answers.map { it.copy(attemptId = attemptId) })

        val existing = progressFor(answers.map { it.questionId }).associateBy { it.questionId }
        upsertProgress(
            answers.map { a ->
                val old = existing[a.questionId]
                QuestionProgressEntity(
                    questionId = a.questionId,
                    timesSeen = (old?.timesSeen ?: 0) + 1,
                    timesCorrect = (old?.timesCorrect ?: 0) + if (a.isCorrect) 1 else 0,
                    lastAnsweredAt = now
                )
            }
        )

        val byCategory = answers.groupBy { categoryOf[it.questionId] }.filterKeys { it != null }
        val updated = byCategory.map { (category, list) ->
            val old = categoryProgress(category!!)
            val correct = list.count { it.isCorrect }
            val percent = if (list.isEmpty()) 0 else correct * 100 / list.size
            CategoryProgressEntity(
                category = category,
                // A quiz counts as "played" for the category it was started from (or every
                // category it touched, for the mixed daily / bookmark quizzes).
                quizzesPlayed = (old?.quizzesPlayed ?: 0) + 1,
                questionsAnswered = (old?.questionsAnswered ?: 0) + list.size,
                correctAnswers = (old?.correctAnswers ?: 0) + correct,
                bestScore = maxOf(old?.bestScore ?: 0, if (attempt.category == category) percent else 0)
            )
        }
        upsertCategoryProgress(updated)
        return attemptId
    }

    // endregion

    // region Bookmarks

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    abstract suspend fun deleteBookmark(questionId: String)

    @Query("SELECT questionId FROM bookmarks")
    abstract fun observeBookmarkIds(): Flow<List<String>>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN bookmarks b ON b.questionId = q.id
        ORDER BY b.createdAt DESC
        """
    )
    abstract fun observeBookmarkedQuestions(): Flow<List<QuestionEntity>>

    // endregion

    // region Clearing

    @Query("DELETE FROM quiz_attempts")
    abstract suspend fun clearAttempts()

    @Query("DELETE FROM attempt_answers")
    abstract suspend fun clearAnswers()

    @Query("DELETE FROM question_progress")
    abstract suspend fun clearQuestionProgress()

    @Query("DELETE FROM category_progress")
    abstract suspend fun clearCategoryProgress()

    @Query("DELETE FROM bookmarks")
    abstract suspend fun clearBookmarks()

    @Transaction
    open suspend fun clearUserData() {
        clearAnswers()
        clearAttempts()
        clearQuestionProgress()
        clearCategoryProgress()
        clearBookmarks()
    }

    // endregion
}
