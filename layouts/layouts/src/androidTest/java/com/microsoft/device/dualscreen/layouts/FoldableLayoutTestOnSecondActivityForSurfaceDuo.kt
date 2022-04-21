/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.app.UiAutomation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutTestOnSecondActivity
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate
import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.ForceClick
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevice
import com.microsoft.device.dualscreen.testing.rules.DualScreenTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableTestRule
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@LargeTest
@RunWith(FoldableJUnit4ClassRunner::class)
class FoldableLayoutTestOnSecondActivityForSurfaceDuo {
    private val activityScenarioRule = activityScenarioRule<FoldableLayoutTestOnSecondActivity>()
    private val dualScreenTestRule = DualScreenTestRule()
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()
    private val currentActivityDelegate = CurrentActivityDelegate()

    @get:Rule
    val testRule: TestRule = foldableTestRule(activityScenarioRule, dualScreenTestRule)

    @Before
    fun before() {
        currentActivityDelegate.setup(activityScenarioRule)
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.reset()
        currentActivityDelegate.clear(activityScenarioRule)
    }

    @Test
    @SingleScreenTest
    fun testLayoutSingleScreen() {
        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(click())
        currentActivityDelegate.waitForActivity()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    @SingleScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    fun testLayoutSingleScreenLandscape() {
        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(ForceClick())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    @DualScreenTest
    @TargetDevice(device = DeviceModel.SurfaceDuo)
    fun testLayoutDualScreenLandscape() {
        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(click())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_portrait_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
    }

    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    @TargetDevice(device = DeviceModel.SurfaceDuo)
    fun testLayoutDualScreenPortrait() {
        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(ForceClick())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_landscape_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
    }
}