package com.example.myapplication.domain.quiz

import java.util.TimeZone

/**
 * Local calendar day as an epoch-day number (days since 1970-01-01 in the device time zone).
 * Avoids java.time so it works on minSdk 24 without core library desugaring.
 */
object DayClock {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun epochDay(nowMillis: Long = System.currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Long =
        (nowMillis + timeZone.getOffset(nowMillis)).floorDiv(MILLIS_PER_DAY)
}
