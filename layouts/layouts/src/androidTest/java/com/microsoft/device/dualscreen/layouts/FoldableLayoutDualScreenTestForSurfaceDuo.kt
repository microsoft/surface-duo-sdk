/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.app.UiAutomation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutDualScreenActivity
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo2
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(FoldableJUnit4ClassRunner::class)
class FoldableLayoutDualScreenTestForSurfaceDuo {
    private val activityScenarioRule = activityScenarioRule<FoldableLayoutDualScreenActivity>()
    private val foldableTestRule = FoldableTestRule()
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)

    @Before
    fun before() {
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.reset()
    }

    @Test
    @SingleScreenTest
    fun testLayoutSingleScreen() {
        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    @SingleScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    fun testLayoutSingleScreenLandscape() {
        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(devices = [SurfaceDuo, SurfaceDuo2])
    fun testLayoutDualScreenLandscape() {
        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_portrait)))
    }

    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    @TargetDevices(devices = [SurfaceDuo, SurfaceDuo2])
    fun testLayoutDualScreenPortrait() {
        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_landscape)))
    }
}
