package com.example.myapplication.domain.quiz

import java.util.Random

/**
 * Picks the daily quiz: the same [count] questions for everyone on a given date, independent of
 * the order the ids are supplied in. Uses java.util.Random, whose algorithm is fixed by the Java
 * spec, so results are stable across devices and app versions (as long as the bank is unchanged).
 */
object DailyQuizSelector {
    const val DAILY_QUIZ_SIZE = 10

    fun select(questionIds: Collection<String>, epochDay: Long, count: Int = DAILY_QUIZ_SIZE): List<String> {
        if (questionIds.isEmpty() || count <= 0) return emptyList()
        val pool = questionIds.distinct().sorted().toMutableList()
        val random = Random(seedFor(epochDay))
        val n = minOf(count, pool.size)
        // Partial Fisher–Yates: the first n slots become a uniform random sample.
        for (i in 0 until n) {
            val j = i + random.nextInt(pool.size - i)
            val tmp = pool[i]
            pool[i] = pool[j]
            pool[j] = tmp
        }
        return pool.subList(0, n).toList()
    }

    internal fun seedFor(epochDay: Long): Long = (epochDay * SEED_MULTIPLIER) xor SEED_SALT

    private const val SEED_MULTIPLIER = -7046029254386353131L // 0x9E3779B97F4A7C15
    private const val SEED_SALT = 0x5DEECE66DL
}
