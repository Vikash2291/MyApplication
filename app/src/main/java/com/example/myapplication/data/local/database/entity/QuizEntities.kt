package com.example.myapplication.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.domain.quiz.LocalizedQuestion
import com.example.myapplication.domain.quiz.Question
import com.example.myapplication.domain.quiz.QuizCategory

/** A bundled question in both languages (options stored as JSON via [QuizConverters]). */
@Entity(tableName = "questions", indices = [Index("category")])
data class QuestionEntity(
    @PrimaryKey val id: String,
    val category: String,
    val answerIndex: Int,
    val questionEn: String,
    val optionsEn: List<String>,
    val explanationEn: String,
    val questionHi: String,
    val optionsHi: List<String>,
    val explanationHi: String
)

/** How often the user has seen / answered a question correctly (drives "unseen first"). */
@Entity(tableName = "question_progress")
data class QuestionProgressEntity(
    @PrimaryKey val questionId: String,
    val timesSeen: Int,
    val timesCorrect: Int,
    val lastAnsweredAt: Long
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val category: String?,
    val epochDay: Long,
    val correctCount: Int,
    val totalCount: Int,
    val points: Int,
    val completedAt: Long
)

@Entity(tableName = "attempt_answers", indices = [Index("attemptId")])
data class AttemptAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val position: Int,
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean
)

@Entity(tableName = "category_progress")
data class CategoryProgressEntity(
    @PrimaryKey val category: String,
    val quizzesPlayed: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val bestScore: Int
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: String,
    val createdAt: Long
)

/** Result row of the per-category coverage query. */
data class CategoryCoverageRow(
    val category: String,
    val totalQuestions: Int,
    val seenQuestions: Int
)

fun QuestionEntity.toDomain(): Question? {
    val cat = QuizCategory.fromName(category) ?: return null
    return Question(
        id = id,
        category = cat,
        answerIndex = answerIndex,
        english = LocalizedQuestion(questionEn, optionsEn, explanationEn),
        hindi = LocalizedQuestion(questionHi, optionsHi, explanationHi)
    )
}

fun Question.toEntity(): QuestionEntity = QuestionEntity(
    id = id,
    category = category.name,
    answerIndex = answerIndex,
    questionEn = english.text,
    optionsEn = english.options,
    explanationEn = english.explanation,
    questionHi = hindi.text,
    optionsHi = hindi.options,
    explanationHi = hindi.explanation
)
