/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.bottomnavigation.test.R
import com.microsoft.device.dualscreen.bottomnavigation.utils.SimpleBottomNavigationActivity
import com.microsoft.device.dualscreen.bottomnavigation.utils.areTabsOnScreen
import com.microsoft.device.dualscreen.bottomnavigation.utils.changeButtonArrangement
import com.microsoft.device.dualscreen.bottomnavigation.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.bottomnavigation.utils.checkChildCount
import com.microsoft.device.dualscreen.bottomnavigation.utils.disableAnimation
import com.microsoft.device.dualscreen.bottomnavigation.utils.hasHalfTransparentBackground
import com.microsoft.device.dualscreen.utils.test.setOrientationLeft
import com.microsoft.device.dualscreen.utils.test.setOrientationRight
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.utils.test.unfreezeRotation
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceBottomNavigationTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleBottomNavigationActivity::class.java)

    @Before
    fun before() {
        onView(withId(R.id.nav_view)).perform(disableAnimation())
    }

    @Test
    fun testDisplayPositionFromLayout() {
        switchFromSingleToDualScreen()
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.START)
    }

    @Test
    fun testDisplayPositionEnd() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.END)
    }

    @Test
    fun testDisplayPositionDual() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit0_5() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit1_4() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(1, 4, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit2_3() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(2, 3, DisplayPosition.DUAL)
    }

    @Test
    fun testButtonSplit5_0() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(0, 5, DisplayPosition.END)
    }

    @Test
    fun testButtonSplit_invalid() {
        switchFromSingleToDualScreen()
        arrangeButtonsAndCheckPosition(5, 0, DisplayPosition.START)
        arrangeButtonsAndCheckPosition(5, 5, DisplayPosition.START)
    }

    @Test
    fun testSwipeLeft() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.nav_view)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeLeft())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.START)))
    }

    @Test
    fun testSwipeRight() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.nav_view)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeRight())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testMultipleSwipes() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.nav_view)).perform(changeButtonArrangement(2, 3))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeLeft())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.START)))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeRight())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.END)))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeLeft())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.START)))

        onView(withId(R.id.nav_view)).perform(ViewActions.swipeRight())
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.END)))

        onView(withId(R.id.nav_view)).perform(changeButtonArrangement(2, 3))
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testTransparentBackground() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(DisplayPosition.DUAL)))
        onView(withId(R.id.nav_view)).check(matches(Matchers.not(hasHalfTransparentBackground())))

        arrangeButtonsAndCheckPosition(DisplayPosition.START)
        onView(withId(R.id.nav_view)).check(matches(hasHalfTransparentBackground()))

        arrangeButtonsAndCheckPosition(DisplayPosition.END)
        onView(withId(R.id.nav_view)).check(matches(hasHalfTransparentBackground()))
    }

    @Test
    fun testOrientationChanges() {
        onView(withId(R.id.nav_view)).check(matches(checkChildCount(5)))

        switchFromSingleToDualScreen()

        onView(withId(R.id.nav_view)).check(matches(checkChildCount(5)))

        setOrientationLeft()
        onView(withId(R.id.nav_view)).check(matches(checkChildCount(5)))

        setOrientationRight()
        onView(withId(R.id.nav_view)).check(matches(checkChildCount(5)))

        unfreezeRotation()
    }

    private fun arrangeButtonsAndCheckPosition(
        startBtnCount: Int,
        endBtnCount: Int,
        expectedPosition: DisplayPosition
    ) {
        onView(withId(R.id.nav_view)).perform(changeButtonArrangement(startBtnCount, endBtnCount))
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(expectedPosition)))
    }

    private fun arrangeButtonsAndCheckPosition(
        displayPosition: DisplayPosition
    ) {
        onView(withId(R.id.nav_view)).perform(changeDisplayPosition(displayPosition))
        onView(withId(R.id.nav_view)).check(matches(areTabsOnScreen(displayPosition)))
    }
}
