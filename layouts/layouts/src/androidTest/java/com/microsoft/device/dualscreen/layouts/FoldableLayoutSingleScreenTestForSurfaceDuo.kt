/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.app.UiAutomation
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutSingleScreenActivity
import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevice
import com.microsoft.device.dualscreen.testing.isViewOnScreen
import com.microsoft.device.dualscreen.testing.rules.DualScreenTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableTestRule
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(FoldableJUnit4ClassRunner::class)
class FoldableLayoutSingleScreenTestForSurfaceDuo {
    private val activityScenarioRule = activityScenarioRule<FoldableLayoutSingleScreenActivity>()
    private val dualScreenTestRule = DualScreenTestRule()
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @get:Rule
    val testRule: TestRule = foldableTestRule(activityScenarioRule, dualScreenTestRule)

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
    @TargetDevice(device = DeviceModel.SurfaceDuo)
    fun testLayoutDualScreenLandscape() {
        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_portrait_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.START,
                    orientation = ORIENTATION_LANDSCAPE,
                    firstDisplay = DeviceModel.SurfaceDuo.paneWidth,
                    totalDisplay = DeviceModel.SurfaceDuo.totalDisplay,
                    foldingFeature = DeviceModel.SurfaceDuo.foldWidth
                )
            )
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.END,
                    orientation = ORIENTATION_LANDSCAPE,
                    firstDisplay = DeviceModel.SurfaceDuo.paneWidth,
                    totalDisplay = DeviceModel.SurfaceDuo.totalDisplay,
                    foldingFeature = DeviceModel.SurfaceDuo.foldWidth
                )
            )
        )
    }

    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    @TargetDevice(device = DeviceModel.SurfaceDuo)
    fun testLayoutDualScreenPortrait() {
        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_landscape_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.START,
                    orientation = ORIENTATION_PORTRAIT,
                    firstDisplay = DeviceModel.SurfaceDuo.paneWidth,
                    totalDisplay = DeviceModel.SurfaceDuo.totalDisplay,
                    foldingFeature = DeviceModel.SurfaceDuo.foldWidth
                )
            )
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.END,
                    orientation = ORIENTATION_PORTRAIT,
                    firstDisplay = DeviceModel.SurfaceDuo.paneWidth,
                    totalDisplay = DeviceModel.SurfaceDuo.totalDisplay,
                    foldingFeature = DeviceModel.SurfaceDuo.foldWidth
                )
            )
        )
    }
}
