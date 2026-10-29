package com.example.myapplication.domain.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DailyQuizSelectorTest {

    private val ids = (1..320).map { "q-%03d".format(it) }

    @Test
    fun `same date always gives the same questions in the same order`() {
        val first = DailyQuizSelector.select(ids, epochDay = 20_000)
        val second = DailyQuizSelector.select(ids, epochDay = 20_000)
        assertThat(first).containsExactlyElementsIn(second).inOrder()
    }

    @Test
    fun `selection does not depend on input order`() {
        val sorted = DailyQuizSelector.select(ids, epochDay = 20_391)
        val shuffled = DailyQuizSelector.select(ids.shuffled(java.util.Random(7)), epochDay = 20_391)
        assertThat(shuffled).containsExactlyElementsIn(sorted).inOrder()
    }

    @Test
    fun `returns ten distinct questions from the pool`() {
        val picked = DailyQuizSelector.select(ids, epochDay = 19_999)
        assertThat(picked).hasSize(DailyQuizSelector.DAILY_QUIZ_SIZE)
        assertThat(picked.toSet()).hasSize(DailyQuizSelector.DAILY_QUIZ_SIZE)
        assertThat(ids).containsAtLeastElementsIn(picked)
    }

    @Test
    fun `consecutive days give different quizzes`() {
        val sets = (20_000L until 20_030L).map { DailyQuizSelector.select(ids, it).toSet() }
        // A month of daily quizzes should never repeat a full set.
        assertThat(sets.toSet()).hasSize(sets.size)
    }

    @Test
    fun `small pools return every question and empty pools return nothing`() {
        val small = listOf("a", "b", "c")
        assertThat(DailyQuizSelector.select(small, 5)).containsExactlyElementsIn(small)
        assertThat(DailyQuizSelector.select(emptyList(), 5)).isEmpty()
        assertThat(DailyQuizSelector.select(ids, 5, count = 0)).isEmpty()
    }

    @Test
    fun `duplicate ids are ignored`() {
        val picked = DailyQuizSelector.select(ids + ids, epochDay = 123)
        assertThat(picked.toSet()).hasSize(picked.size)
        assertThat(picked).containsExactlyElementsIn(DailyQuizSelector.select(ids, 123)).inOrder()
    }
}
