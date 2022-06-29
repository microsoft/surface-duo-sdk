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
import androidx.test.ext.junit.rules.activityScenarioRule
import com.microsoft.device.dualscreen.testing.DeviceModel.FoldInOuterDisplay
import com.microsoft.device.dualscreen.testing.DeviceModel.HorizontalFoldIn
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import com.microsoft.device.dualscreen.testing.sample.utils.withWidth
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(FoldableJUnit4ClassRunner::class)
class TargetDevicesSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()

    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)

    @Test
    @SingleScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    @TargetDevices(devices = [HorizontalFoldIn])
    fun testSingleScreenLayoutOnHorizontalFoldIn() {
        onView(withId(R.id.text_view_single)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_single)).check(matches(withWidth(2401)))
    }

    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    @TargetDevices(devices = [HorizontalFoldIn])
    fun testDualScreenLayoutOnHorizontalFoldIn() {
        onView(withId(R.id.text_view_start)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_start)).check(matches(withWidth(1056)))

        onView(withId(R.id.text_view_end)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_end)).check(matches(withWidth(1345)))
    }

    @Test
    @SingleScreenTest
    @TargetDevices(devices = [FoldInOuterDisplay])
    fun testSingleScreenLayoutOnFoldInOuterDisplay() {
        onView(withId(R.id.text_view_single)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_single)).check(matches(withWidth(1768)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(devices = [FoldInOuterDisplay])
    fun testDualScreenLayoutOnFoldInOuterDisplay() {
        onView(withId(R.id.text_view_start)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_start)).check(matches(withWidth(884)))

        onView(withId(R.id.text_view_end)).check(matches(isDisplayed()))
        onView(withId(R.id.text_view_end)).check(matches(withWidth(884)))
    }
}