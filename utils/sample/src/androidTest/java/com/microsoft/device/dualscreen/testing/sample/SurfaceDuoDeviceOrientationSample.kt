/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.sample

import android.app.UiAutomation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo2
import com.microsoft.device.dualscreen.testing.filters.DeviceOrientation
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@TargetDevices(devices = [SurfaceDuo, SurfaceDuo2])
@DeviceOrientation(orientation = UiAutomation.ROTATION_FREEZE_270)
@RunWith(FoldableJUnit4ClassRunner::class)
class SurfaceDuoDeviceOrientationSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()

    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)

    @Test
    @SingleScreenTest
    fun testLayoutSingleScreenLandscape() {
        onView(withId(R.id.text_view_single)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_single)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    @DualScreenTest
    fun testLayoutDualScreenPortrait() {
        onView(withId(R.id.text_view_start)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_start)).check(matches(withText(R.string.dual_landscape_start)))

        onView(withId(R.id.text_view_end)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_end)).check(matches(withText(R.string.dual_landscape_end)))
    }
}