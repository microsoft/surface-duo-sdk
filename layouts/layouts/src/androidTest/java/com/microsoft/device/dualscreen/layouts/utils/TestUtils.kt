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
import com.microsoft.device.dualscreen.layouts.FoldableFrameLayout
import com.microsoft.device.dualscreen.utils.test.DUAL_SCREEN_WIDTH
import com.microsoft.device.dualscreen.utils.test.HINGE_WIDTH
import com.microsoft.device.dualscreen.utils.test.SINGLE_SCREEN_WIDTH
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
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
            return areCoordinatesOnTargetScreen(pos, start, end)
        }
    }

fun changeDisplayPosition(pos: DisplayPosition): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of a FoldableFrameLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val frameLayout = view as FoldableFrameLayout
            frameLayout.foldableDisplayPosition = pos

            uiController.loopMainThreadUntilIdle()
        }
    }

fun isFrameLayoutOnScreen(pos: DisplayPosition): Matcher<View> =
    object : BoundedMatcher<View, FoldableFrameLayout>(FoldableFrameLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the layout is displayed on the right screen"
            )
        }

        override fun matchesSafely(item: FoldableFrameLayout?): Boolean {
            if (item == null || pos != item.foldableDisplayPosition) {
                return false
            }
            val child = item.getChildAt(0) ?: return false
            val startArray = IntArray(2)
            child.getLocationInWindow(startArray)
            return areCoordinatesOnTargetScreen(
                pos,
                startArray[0],
                startArray[0] + child.width
            )
        }
    }

fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    xStart: Int,
    xEnd: Int
): Boolean {
    return when (targetScreenPosition) {
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
