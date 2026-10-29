package com.example.myapplication.domain.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.TimeZone

class DayClockTest {

    @Test
    fun `epoch day follows the local time zone`() {
        val utc = TimeZone.getTimeZone("UTC")
        val ist = TimeZone.getTimeZone("Asia/Kolkata")
        // 2024-01-01T20:00:00Z is already 2024-01-02 01:30 in India.
        val instant = 1_704_139_200_000L
        assertThat(DayClock.epochDay(instant, utc)).isEqualTo(19_723L)
        assertThat(DayClock.epochDay(instant, ist)).isEqualTo(19_724L)
    }

    @Test
    fun `times before 1970 floor to the previous day`() {
        assertThat(DayClock.epochDay(-1L, TimeZone.getTimeZone("UTC"))).isEqualTo(-1L)
    }
}
