/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.viewbinding.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(FoldableJUnit4ClassRunner::class)
class ViewBindingTests {
    private val activityScenarioRule = activityScenarioRule<SampleActivity>()
    private val foldableTestRule = FoldableTestRule()

    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)

    @Test
    @SingleScreenTest
    fun contentOnSingleScreen() {
        onView(withId(R.id.single_text_view)).check(matches(isDisplayed()))
        onView(withId(R.id.single_text_view)).check(matches(withText(R.string.single_screen_fragment)))
    }

    @Test
    @DualScreenTest
    fun contentOnDualScreen() {
        onView(withId(R.id.dual_start_text_view)).check(matches(isDisplayed()))
        onView(withId(R.id.dual_start_text_view)).check(matches(withText(R.string.dual_start_screen_fragment)))

        onView(withId(R.id.dual_end_text_view)).check(matches(isDisplayed()))
        onView(withId(R.id.dual_end_text_view)).check(matches(withText(R.string.dual_end_screen_fragment)))
    }
}