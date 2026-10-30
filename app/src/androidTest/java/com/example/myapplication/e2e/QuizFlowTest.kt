package com.example.myapplication.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.myapplication.presentation.ui.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke test on the real app (real Hilt graph, Room, bundled question bank):
 * guest sign-in -> daily quiz -> answer every question -> result -> back to Home.
 */
@RunWith(AndroidJUnit4::class)
class QuizFlowTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun exists(tag: String) = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun guestPlaysDailyQuizToResultAndBack() {
        rule.waitUntil(TIMEOUT) { exists("guest_button") || exists("daily_quiz_play") }
        if (exists("guest_button")) rule.onNodeWithTag("guest_button").performScrollTo().performClick()

        rule.waitUntil(TIMEOUT) { exists("daily_quiz_play") }
        rule.onNodeWithTag("daily_quiz_play").performClick()

        var answered = 0
        while (!exists("result_home") && answered < MAX_QUESTIONS) {
            rule.waitUntil(TIMEOUT) { exists("option_0") || exists("quiz_next") || exists("result_home") }
            if (exists("result_home")) break
            if (!exists("quiz_next")) rule.onNodeWithTag("option_0").performScrollTo().performClick()
            rule.waitUntil(TIMEOUT) { exists("quiz_next") }
            rule.onNodeWithTag("quiz_next").performClick()
            answered++
            rule.waitUntil(TIMEOUT) { !exists("quiz_next") || exists("result_home") }
        }

        rule.waitUntil(TIMEOUT) { exists("result_home") }
        assertTrue("quiz should have had questions", answered > 0)
        rule.onNodeWithTag("result_home").performClick()
        rule.waitUntil(TIMEOUT) { exists("daily_quiz_play") }
    }

    private companion object {
        const val TIMEOUT = 30_000L
        const val MAX_QUESTIONS = 20
    }
}
