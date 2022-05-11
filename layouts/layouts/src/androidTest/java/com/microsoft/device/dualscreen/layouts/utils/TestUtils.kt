/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.layouts.FoldableFrameLayout
import com.microsoft.device.dualscreen.testing.areCoordinatesOnTargetScreen
import com.microsoft.device.dualscreen.testing.getDeviceModel
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Description
import org.hamcrest.Matcher

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

            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            with(uiDevice.getDeviceModel()) {
                return areCoordinatesOnTargetScreen(
                    targetScreenPosition = pos,
                    start = startArray[0],
                    end = startArray[0] + child.width,
                    firstDisplay = paneWidth,
                    totalDisplay = totalDisplay,
                    foldingFeature = foldWidth
                )
            }
        }
    }