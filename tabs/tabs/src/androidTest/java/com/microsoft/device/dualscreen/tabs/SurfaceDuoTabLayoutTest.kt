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
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.tabs.test.R
import com.microsoft.device.dualscreen.tabs.utils.SimpleTabActivity
import com.microsoft.device.dualscreen.tabs.utils.areTabsOnScreen
import com.microsoft.device.dualscreen.tabs.utils.changeButtonArrangement
import com.microsoft.device.dualscreen.tabs.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.tabs.utils.checkChildCount
import com.microsoft.device.dualscreen.tabs.utils.hasHalfTransparentBackground
import com.microsoft.device.dualscreen.testing.SurfaceDuo1
import com.microsoft.device.dualscreen.testing.setOrientationLeft
import com.microsoft.device.dualscreen.testing.setOrientationRight
import com.microsoft.device.dualscreen.testing.unfreezeRotation
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoTabLayoutTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleTabActivity::class.java)

    @Test
    fun testDisplayPositionFromLayout() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.START)
    }

    @Test
    fun testDisplayPositionEnd() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.END)
    }

    @Test
    fun testDisplayPositionDual() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit0_5() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit1_4() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(1, 4, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit2_3() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(2, 3, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit5_0() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit_invalid() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(5, 0, DisplayPosition.START)
        arrangeButtonsAndCheckPosition(5, 5, DisplayPosition.START)
    }

    @Test
    fun testSwipeLeft() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.tabs)).perform(swipeLeft())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.START)))
    }

    @Test
    fun testSwipeRight() {
        SurfaceDuo1.switchFromSingleToDualScreen()
        onView(withId(R.id.tabs)).perform(changeButtonArrangement(3, 2))

        onView(withId(R.id.tabs)).perform(swipeRight())
        onView(withId(R.id.tabs)).check(matches(areTabsOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testMultipleSwipes() {
        SurfaceDuo1.switchFromSingleToDualScreen()
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

        SurfaceDuo1.switchFromSingleToDualScreen()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        setOrientationLeft()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        setOrientationRight()
        onView(withId(R.id.tabs)).check(matches(checkChildCount(5)))

        unfreezeRotation()
    }

    @Test
    fun testTransparentBackground() {
        SurfaceDuo1.switchFromSingleToDualScreen()
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
