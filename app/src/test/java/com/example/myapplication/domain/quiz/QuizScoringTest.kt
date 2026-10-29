package com.example.myapplication.domain.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuizScoringTest {

    private fun answer(selected: Int, correct: Int = 1) = AnswerRecord("q", selected, correct)

    @Test
    fun `points are earned per correct answer only`() {
        val answers = listOf(answer(1), answer(1), answer(0), answer(AnswerRecord.TIMED_OUT))
        val score = QuizScoring.score(answers, QuizMode.CATEGORY)

        assertThat(score.correct).isEqualTo(2)
        assertThat(score.timedOut).isEqualTo(1)
        assertThat(score.wrong).isEqualTo(1)
        assertThat(score.total).isEqualTo(4)
        assertThat(score.percent).isEqualTo(50)
        assertThat(score.points).isEqualTo(2 * QuizScoring.POINTS_PER_CORRECT)
    }

    @Test
    fun `perfect score adds a bonus`() {
        val score = QuizScoring.score(List(10) { answer(1) }, QuizMode.CATEGORY)
        assertThat(score.isPerfect).isTrue()
        assertThat(score.points).isEqualTo(10 * QuizScoring.POINTS_PER_CORRECT + QuizScoring.PERFECT_SCORE_BONUS)
    }

    @Test
    fun `daily quiz adds a completion bonus even with no correct answers`() {
        val score = QuizScoring.score(List(10) { answer(0) }, QuizMode.DAILY)
        assertThat(score.correct).isEqualTo(0)
        assertThat(score.points).isEqualTo(QuizScoring.DAILY_QUIZ_BONUS)
    }

    @Test
    fun `empty quiz scores zero`() {
        val score = QuizScoring.score(emptyList(), QuizMode.DAILY)
        assertThat(score.points).isEqualTo(0)
        assertThat(score.percent).isEqualTo(0)
        assertThat(score.isPerfect).isFalse()
    }

    @Test
    fun `timed out answers are never correct`() {
        val record = AnswerRecord("q", AnswerRecord.TIMED_OUT, 0)
        assertThat(record.isCorrect).isFalse()
        assertThat(record.isTimedOut).isTrue()
    }
}
