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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.tabs.test.R
import com.microsoft.device.dualscreen.tabs.utils.SimpleTabActivity
import com.microsoft.device.dualscreen.tabs.utils.areTabsOnScreen
import com.microsoft.device.dualscreen.tabs.utils.changeButtonArrangement
import com.microsoft.device.dualscreen.tabs.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.tabs.utils.checkChildCount
import com.microsoft.device.dualscreen.tabs.utils.hasHalfTransparentBackground
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoTabLayoutTest {

    @get:Rule
    val activityTestRule = ActivityScenarioRule(SimpleTabActivity::class.java)
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun testDisplayPositionFromLayout() {
        uiDevice.spanFromStart()
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(DisplayPosition.START)
    }

    @Test
    fun testDisplayPositionEnd() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(DisplayPosition.END)
    }

    @Test
    fun testDisplayPositionDual() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit0_5() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit1_4() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(1, 4, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit2_3() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(2, 3, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit5_0() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit_invalid() {
        uiDevice.spanFromStart()
        arrangeButtonsAndCheckPosition(5, 0, DisplayPosition.START)
        arrangeButtonsAndCheckPosition(5, 5, DisplayPosition.START)
    }

    @Test
    fun testSwipeLeft() {
        uiDevice.spanFromStart()
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.tabs)).perform(swipeLeft())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.START)))
    }

    @Test
    fun testSwipeRight() {
        uiDevice.spanFromStart()
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(3, 2))

        onView(withId(R.id.tabs)).perform(swipeRight())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testMultipleSwipes() {
        uiDevice.spanFromStart()
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
    fun testTransparentBackground() {
        uiDevice.spanFromStart()
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
