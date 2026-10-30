package com.example.myapplication.data.ads

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InterstitialPacerTest {

    private var now = 1_000_000L
    private val pacer = InterstitialPacer(minActionsBetween = 2, minIntervalMs = 120_000L, clock = { now })

    @Test
    fun `never shows before two user actions (no launch interstitial)`() {
        assertThat(pacer.canShow()).isFalse()
        pacer.recordUserAction()
        assertThat(pacer.canShow()).isFalse()
        pacer.recordUserAction()
        assertThat(pacer.canShow()).isTrue()
    }

    @Test
    fun `requires two new actions and two minutes after a show`() {
        repeat(2) { pacer.recordUserAction() }
        pacer.onShown()

        repeat(5) { pacer.recordUserAction() }
        now += 119_999L
        assertThat(pacer.canShow()).isFalse()

        now += 1L
        assertThat(pacer.canShow()).isTrue()
    }

    @Test
    fun `time alone is not enough without actions`() {
        repeat(2) { pacer.recordUserAction() }
        pacer.onShown()
        now += 10 * 60_000L
        pacer.recordUserAction()
        assertThat(pacer.canShow()).isFalse()
        pacer.recordUserAction()
        assertThat(pacer.canShow()).isTrue()
    }
}

class AppOpenPacerTest {

    private var now = 50_000_000L
    private val fourHours = 4 * 60 * 60 * 1000L
    private val pacer = AppOpenPacer(minIntervalMs = fourHours, maxAdAgeMs = fourHours, clock = { now })

    private fun canShow(
        coldStart: Boolean = false,
        canRequestAds: Boolean = true,
        otherShowing: Boolean = false,
        loadedAt: Long? = now - 1_000L
    ) = pacer.canShow(coldStart, canRequestAds, otherShowing, loadedAt)

    @Test
    fun `shows on a warm return when everything allows it`() {
        assertThat(canShow()).isTrue()
    }

    @Test
    fun `never on cold start, without consent, or over another full screen ad`() {
        assertThat(canShow(coldStart = true)).isFalse()
        assertThat(canShow(canRequestAds = false)).isFalse()
        assertThat(canShow(otherShowing = true)).isFalse()
    }

    @Test
    fun `needs a loaded ad younger than four hours`() {
        assertThat(canShow(loadedAt = null)).isFalse()
        assertThat(canShow(loadedAt = now - fourHours)).isFalse()
        assertThat(canShow(loadedAt = now - fourHours + 1)).isTrue()
    }

    @Test
    fun `at most once per four hours`() {
        pacer.onShown()
        now += fourHours - 1
        assertThat(canShow()).isFalse()
        now += 1
        assertThat(canShow()).isTrue()
    }
}
