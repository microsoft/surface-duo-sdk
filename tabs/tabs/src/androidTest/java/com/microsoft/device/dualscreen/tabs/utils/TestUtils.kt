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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.tabs.TabLayout
import com.microsoft.device.dualscreen.testing.getDeviceModel
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun changeDisplayPosition(pos: DisplayPosition): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of TabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as TabLayout
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
            return "Change Display Position value of TabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as TabLayout
            tabLayout.arrangeButtons(startBtnCount, endBtnCount)

            uiController.loopMainThreadUntilIdle()
        }
    }

fun areTabsOnScreen(pos: DisplayPosition): Matcher<View> =
    object : BoundedMatcher<View, TabLayout>(TabLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the tabs are displayed on the right screen"
            )
        }

        override fun matchesSafely(item: TabLayout?): Boolean {
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

            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            with(uiDevice.getDeviceModel()) {
                return when (pos) {
                    DisplayPosition.DUAL ->
                        xStart in 0..paneWidth && xEnd in (paneWidth + foldWidth)..totalDisplay
                    DisplayPosition.START ->
                        xStart in 0..paneWidth && xEnd in 0..paneWidth
                    DisplayPosition.END ->
                        xStart in (paneWidth + foldWidth)..totalDisplay && xEnd in (paneWidth + foldWidth)..totalDisplay
                }
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
            if (item == null || item !is TabLayout) {
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