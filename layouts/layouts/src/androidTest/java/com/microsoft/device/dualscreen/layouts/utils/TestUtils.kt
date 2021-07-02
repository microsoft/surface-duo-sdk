/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts.utils

import android.content.res.Configuration
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.layouts.SurfaceDuoFrameLayout
import com.microsoft.device.dualscreen.test.utils.DUAL_SCREEN_WIDTH
import com.microsoft.device.dualscreen.test.utils.HINGE_WIDTH
import com.microsoft.device.dualscreen.test.utils.SINGLE_SCREEN_WIDTH
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun isViewOnScreen(pos: DisplayPosition, orientation: Int): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the view is displayed on the right screen"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null) {
                return false
            }
            val startArray = IntArray(2)
            item.getLocationInWindow(startArray)
            val start = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                startArray[0]
            } else {
                startArray[1]
            }
            val end = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                startArray[0] + item.width
            } else {
                startArray[1] + item.height
            }
            return areCoordinatesOnSurfaceScreen(pos, start, end)
        }
    }

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

            val frameLayout = view as SurfaceDuoFrameLayout
            frameLayout.surfaceDuoDisplayPosition = pos

            uiController.loopMainThreadUntilIdle()
        }
    }

fun isFrameLayoutOnScreen(pos: DisplayPosition): Matcher<View> =
    object : BoundedMatcher<View, SurfaceDuoFrameLayout>(SurfaceDuoFrameLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the layout is displayed on the right screen"
            )
        }

        override fun matchesSafely(item: SurfaceDuoFrameLayout?): Boolean {
            if (item == null || pos != item.surfaceDuoDisplayPosition) {
                return false
            }
            val child = item.getChildAt(0) ?: return false
            val startArray = IntArray(2)
            child.getLocationInWindow(startArray)
            return areCoordinatesOnSurfaceScreen(
                pos,
                startArray[0],
                startArray[0] + child.width
            )
        }
    }

fun areCoordinatesOnSurfaceScreen(pos: DisplayPosition, xStart: Int, xEnd: Int): Boolean {
    return when (pos) {
        DisplayPosition.DUAL ->
            xStart in 0..SINGLE_SCREEN_WIDTH &&
                xEnd in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH
        DisplayPosition.START ->
            xStart in 0..SINGLE_SCREEN_WIDTH &&
                xEnd in 0..SINGLE_SCREEN_WIDTH
        DisplayPosition.END ->
            xStart in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH &&
                xEnd in (SINGLE_SCREEN_WIDTH + HINGE_WIDTH)..DUAL_SCREEN_WIDTH
    }
}
