/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.tabs

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.tabs.test.R
import com.microsoft.device.dualscreen.tabs.utils.SimpleTabActivity
import com.microsoft.device.dualscreen.tabs.utils.areTabsOnScreen
import com.microsoft.device.dualscreen.tabs.utils.changeButtonArrangement
import com.microsoft.device.dualscreen.tabs.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.tabs.utils.checkChildCount
import com.microsoft.device.dualscreen.tabs.utils.hasHalfTransparentBackground
import com.microsoft.device.dualscreen.testing.DeviceModel.HorizontalFoldIn
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo
import com.microsoft.device.dualscreen.testing.DeviceModel.SurfaceDuo2
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@MediumTest
@RunWith(FoldableJUnit4ClassRunner::class)
class SurfaceDuoTabLayoutTest {

    private val activityScenarioRule = activityScenarioRule<SimpleTabActivity>()

    @get:Rule
    var ruleChain: RuleChain =
        RuleChain.outerRule(activityScenarioRule).around(FoldableTestRule())
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testDisplayPositionFromLayout() {
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testDisplayPositionStart() {
        arrangeButtonsAndCheckPosition(DisplayPosition.START)
    }

    @Test
    @DualScreenTest
    @TargetDevices(devices = [SurfaceDuo, SurfaceDuo2])
    fun testDisplayPositionEnd() {
        arrangeButtonsAndCheckPosition(DisplayPosition.END)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testDisplayPositionDual() {
        arrangeButtonsAndCheckPosition(DisplayPosition.DUAL)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testButtonSplit0_5() {
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testButtonSplit1_4() {
        arrangeButtonsAndCheckPosition(1, 4, DisplayPosition.DUAL)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testButtonSplit2_3() {
        arrangeButtonsAndCheckPosition(2, 3, DisplayPosition.DUAL)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testButtonSplit5_0() {
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testButtonSplit_invalid() {
        arrangeButtonsAndCheckPosition(5, 0, DisplayPosition.START)
        arrangeButtonsAndCheckPosition(5, 5, DisplayPosition.START)
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testSwipeLeft() {
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.tabs)).perform(swipeLeft())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.START)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testSwipeRight() {
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(3, 2))

        onView(withId(R.id.tabs)).perform(swipeRight())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.END)))
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testMultipleSwipes() {
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.tabs)).perform(swipeLeft())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.START)))

        onView(withId(R.id.tabs)).perform(swipeRight())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.END)))

        onView(withId(R.id.tabs)).perform(swipeLeft())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.START)))

        onView(withId(R.id.tabs)).perform(swipeRight())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.END)))

        onView(withId(R.id.tabs)).perform(changeButtonArrangement(2, 3))
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    @TargetDevices(devices = [SurfaceDuo, SurfaceDuo2])
    fun testOrientationChanges() {
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        uiDevice.spanFromStart()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        uiDevice.setOrientationLeft()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        uiDevice.setOrientationRight()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        uiDevice.unfreezeRotation()
    }

    @Test
    @DualScreenTest
    @TargetDevices(ignoreDevices = [HorizontalFoldIn])
    fun testTransparentBackground() {
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
        onView(withId(R.id.tabs)).check(matches(not(hasHalfTransparentBackground())))

        arrangeButtonsAndCheckPosition(DisplayPosition.START)
        onView(withId(R.id.tabs)).check(matches(hasHalfTransparentBackground()))

        arrangeButtonsAndCheckPosition(DisplayPosition.END)
        onView(withId(R.id.tabs)).check(matches(hasHalfTransparentBackground()))
    }

    private fun arrangeButtonsAndCheckPosition(
        startBtnCount: Int,
        endBtnCount: Int,
        expectedPosition: DisplayPosition
    ) {
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(startBtnCount, endBtnCount))
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(expectedPosition)))
    }

    private fun arrangeButtonsAndCheckPosition(
        displayPosition: DisplayPosition
    ) {
        onView(withId(R.id.tabs)).perform(changeDisplayPosition(displayPosition))
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(displayPosition)))
    }
}
