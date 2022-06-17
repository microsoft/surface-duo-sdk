/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.SmallTest
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FrameLayoutActivity
import com.microsoft.device.dualscreen.layouts.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.layouts.utils.isFrameLayoutOnScreen
import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@SmallTest
@RunWith(FoldableJUnit4ClassRunner::class)
class FrameLayoutTestForSurfaceDuo {
    private val activityScenarioRule = activityScenarioRule<FrameLayoutActivity>()
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
    @DualScreenTest
    fun testDisplayPositionFromLayout() {
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [DeviceModel.HorizontalFoldIn])
    fun testDisplayPositionEnd() {
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.END))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.END)))
    }

    @Test
    @DualScreenTest
    fun testDisplayPositionDual() {
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.DUAL))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [DeviceModel.HorizontalFoldIn])
    fun testDisplayPositionStart() {
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.START))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.START)))
    }
}
