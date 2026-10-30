package com.example.myapplication.domain.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StreakCalculatorTest {

    private val today = 20_000L

    @Test
    fun `first completion starts a streak of one`() {
        val s = StreakCalculator.onDailyCompleted(StreakState(), today)
        assertThat(s).isEqualTo(StreakState(current = 1, best = 1, lastCompletedEpochDay = today))
    }

    @Test
    fun `completing on consecutive days increments the streak`() {
        var s = StreakState()
        for (day in 0 until 5) s = StreakCalculator.onDailyCompleted(s, today + day)
        assertThat(s.current).isEqualTo(5)
        assertThat(s.best).isEqualTo(5)
    }

    @Test
    fun `completing twice on the same day counts once`() {
        val once = StreakCalculator.onDailyCompleted(StreakState(current = 3, best = 4, lastCompletedEpochDay = today - 1), today)
        val twice = StreakCalculator.onDailyCompleted(once, today)
        assertThat(once.current).isEqualTo(4)
        assertThat(twice).isEqualTo(once)
    }

    @Test
    fun `missing a day resets the streak but keeps the best`() {
        val s = StreakCalculator.onDailyCompleted(StreakState(current = 7, best = 7, lastCompletedEpochDay = today - 2), today)
        assertThat(s.current).isEqualTo(1)
        assertThat(s.best).isEqualTo(7)
    }

    @Test
    fun `clock moving backwards changes nothing`() {
        val state = StreakState(current = 2, best = 2, lastCompletedEpochDay = today + 3)
        assertThat(StreakCalculator.onDailyCompleted(state, today)).isEqualTo(state)
    }

    @Test
    fun `displayed streak survives until the end of the next day`() {
        val state = StreakState(current = 4, best = 6, lastCompletedEpochDay = today)
        assertThat(StreakCalculator.displayedStreak(state, today)).isEqualTo(4)
        assertThat(StreakCalculator.displayedStreak(state, today + 1)).isEqualTo(4)
        assertThat(StreakCalculator.displayedStreak(state, today + 2)).isEqualTo(0)
        assertThat(StreakCalculator.displayedStreak(StreakState(), today)).isEqualTo(0)
    }

    @Test
    fun `completed today only for the same epoch day`() {
        val state = StreakState(current = 1, best = 1, lastCompletedEpochDay = today)
        assertThat(StreakCalculator.isCompletedToday(state, today)).isTrue()
        assertThat(StreakCalculator.isCompletedToday(state, today + 1)).isFalse()
    }
}
