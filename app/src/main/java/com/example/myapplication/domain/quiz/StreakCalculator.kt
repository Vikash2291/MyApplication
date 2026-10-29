package com.example.myapplication.domain.quiz

/** Daily-quiz streak persisted in DataStore. Days are epoch days in the user's local time zone. */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val lastCompletedEpochDay: Long? = null
)

object StreakCalculator {

    /** Applies "the daily quiz was completed on [today]". Completing twice on one day counts once. */
    fun onDailyCompleted(state: StreakState, today: Long): StreakState {
        val last = state.lastCompletedEpochDay
        val current = when {
            last == null -> 1
            last == today -> return state
            last > today -> return state // device clock moved backwards: don't reward or punish
            last == today - 1 -> state.current + 1
            else -> 1
        }
        return StreakState(current = current, best = maxOf(state.best, current), lastCompletedEpochDay = today)
    }

    /** The streak to show today: still alive if the last daily quiz was today or yesterday. */
    fun displayedStreak(state: StreakState, today: Long): Int {
        val last = state.lastCompletedEpochDay ?: return 0
        return if (last == today || last == today - 1) state.current else 0
    }

    fun isCompletedToday(state: StreakState, today: Long): Boolean = state.lastCompletedEpochDay == today
}
