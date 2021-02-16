/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.tabs.utils

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.tabs.SurfaceDuoTabLayout
import com.microsoft.device.dualscreen.test.utils.DUAL_SCREEN_WIDTH
import com.microsoft.device.dualscreen.test.utils.HINGE_WIDTH
import com.microsoft.device.dualscreen.test.utils.SINGLE_SCREEN_WIDTH
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun changeDisplayPosition(pos: DisplayPosition): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of SurfaceDuoTabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as SurfaceDuoTabLayout
            tabLayout.displayPosition = pos

            uiController.loopMainThreadUntilIdle()
        }
    }

fun changeButtonArrangement(startBtnCount: Int, endBtnCount: Int): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of SurfaceDuoTabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as SurfaceDuoTabLayout
            tabLayout.arrangeButtons(startBtnCount, endBtnCount)

            uiController.loopMainThreadUntilIdle()
        }
    }

fun areTabsOnScreen(pos: DisplayPosition): Matcher<View> =
    object : BoundedMatcher<View, SurfaceDuoTabLayout>(SurfaceDuoTabLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the tabs are displayed on the right screen"
            )
        }

        override fun matchesSafely(item: SurfaceDuoTabLayout?): Boolean {
            if (item == null || pos != item.displayPosition) {
                return false
            }
            val firstTab = item.getTabAt(0)?.view
            val lastTab = item.getTabAt(item.tabCount - 1)?.view
            if (firstTab == null || lastTab == null) {
                return false
            }
            val startArray = IntArray(2)
            val endArray = IntArray(2)
            firstTab.getLocationInWindow(startArray)
            lastTab.getLocationInWindow(endArray)
            val xStart = startArray[0]
            val xEnd = endArray[0] + lastTab.width

            return when (pos) {
                DisplayPosition.DUAL ->
                    xStart in 0..SINGLE_SCREEN_WIDTH && xEnd in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH
                DisplayPosition.START ->
                    xStart in 0..SINGLE_SCREEN_WIDTH && xEnd in 0..SINGLE_SCREEN_WIDTH
                DisplayPosition.END ->
                    xStart in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH && xEnd in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH
            }
        }
    }

fun checkChildCount(expectedChildCount: Int): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "the background color of the view is the same as the expected one"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null || item !is SurfaceDuoTabLayout) {
                return false
            }

            val child = item.getChildAt(0) as ViewGroup
            return child.childCount == expectedChildCount
        }
    }

fun hasHalfTransparentBackground(): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "the background color of the view is the same as the expected one"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null) {
                return false
            }

            return item.background is LayerDrawable
        }
    }