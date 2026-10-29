package com.example.myapplication.domain.quiz

/** Quiz categories. The names are stored in Room and in the bundled JSON, so never rename them. */
enum class QuizCategory {
    INDIA_GK, WORLD_GK, SCIENCE, HISTORY, GEOGRAPHY, SPORTS, CURRENT_AFFAIRS, COMPUTERS;

    companion object {
        fun fromName(name: String?): QuizCategory? = entries.firstOrNull { it.name == name }
    }
}

/** In-app language. English is the default. */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"), HINDI("hi");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

enum class QuizMode { CATEGORY, DAILY, BOOKMARKS }

/** One language version of a question. */
data class LocalizedQuestion(
    val text: String,
    val options: List<String>,
    val explanation: String
)

data class Question(
    val id: String,
    val category: QuizCategory,
    val answerIndex: Int,
    val english: LocalizedQuestion,
    val hindi: LocalizedQuestion
) {
    fun localized(language: AppLanguage): LocalizedQuestion =
        if (language == AppLanguage.HINDI) hindi else english
}

/** What the user did on one question. [selectedIndex] is [TIMED_OUT] when time ran out. */
data class AnswerRecord(
    val questionId: String,
    val selectedIndex: Int,
    val correctIndex: Int
) {
    val isCorrect: Boolean get() = selectedIndex == correctIndex
    val isTimedOut: Boolean get() = selectedIndex == TIMED_OUT

    companion object {
        const val TIMED_OUT = -1
    }
}

data class QuizScore(
    val correct: Int,
    val total: Int,
    val timedOut: Int,
    val points: Int
) {
    val wrong: Int get() = total - correct - timedOut
    val percent: Int get() = if (total == 0) 0 else correct * 100 / total
    val isPerfect: Boolean get() = total > 0 && correct == total
}

/** A finished quiz loaded back from storage, used by the Result / review screen. */
data class AttemptReview(
    val attemptId: Long,
    val mode: QuizMode,
    val category: QuizCategory?,
    val correct: Int,
    val total: Int,
    val points: Int,
    val items: List<ReviewItem>
)

data class ReviewItem(
    val question: Question,
    val selectedIndex: Int
) {
    val isCorrect: Boolean get() = selectedIndex == question.answerIndex
}

data class CategoryProgress(
    val category: QuizCategory,
    val totalQuestions: Int,
    val seenQuestions: Int,
    val quizzesPlayed: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val bestScore: Int
) {
    val accuracyPercent: Int get() = if (questionsAnswered == 0) 0 else correctAnswers * 100 / questionsAnswered
    val coverage: Float get() = if (totalQuestions == 0) 0f else seenQuestions.toFloat() / totalQuestions
}

data class QuizStats(
    val quizzesPlayed: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val categories: List<CategoryProgress> = emptyList()
) {
    val accuracyPercent: Int get() = if (questionsAnswered == 0) 0 else correctAnswers * 100 / questionsAnswered
}
