package com.example.myapplication.domain.quiz

/** Points rules: earned only for correct answers, plus small completion bonuses. */
object QuizScoring {
    const val POINTS_PER_CORRECT = 10
    const val PERFECT_SCORE_BONUS = 20
    const val DAILY_QUIZ_BONUS = 10

    fun score(answers: List<AnswerRecord>, mode: QuizMode): QuizScore {
        val correct = answers.count { it.isCorrect }
        val timedOut = answers.count { it.isTimedOut }
        val total = answers.size
        var points = correct * POINTS_PER_CORRECT
        if (total > 0 && correct == total) points += PERFECT_SCORE_BONUS
        if (mode == QuizMode.DAILY && total > 0) points += DAILY_QUIZ_BONUS
        return QuizScore(correct = correct, total = total, timedOut = timedOut, points = points)
    }
}
