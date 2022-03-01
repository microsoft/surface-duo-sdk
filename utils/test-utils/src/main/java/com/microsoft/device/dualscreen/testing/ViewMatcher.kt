/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import android.content.res.Configuration
import android.view.View
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Returns a Matcher that checks if a View is shown in the display area given its position
 * and screen dimensions.
 *
 * @param position: target position.
 * @param orientation: orientation of the display. See Configuration {@see Configuration}.
 * @param firstDisplay : width or height (depending on the device orientation) of the
 * left/start/first display area
 * @param totalDisplay : width or height (depending on the device orientation) of the
 * total display area
 * @param foldingFeature : width or height (depending on the device orientation) of the
 * FoldingFeature {@see FoldingFeature} if any.
 * @return
 */
fun isViewOnScreen(
    position: DisplayPosition,
    orientation: Int,
    firstDisplay: Int,
    totalDisplay: Int,
    foldingFeature: Int = 0
): Matcher<View> =
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
            return areCoordinatesOnTargetScreen(
                targetScreenPosition = position,
                start = start,
                end = end,
                firstDisplay = firstDisplay,
                totalDisplay = totalDisplay,
                foldingFeature = foldingFeature
            )
        }
    }

/**
 * Checks whether a specific screen-position is the screen defined by {@param start}, {@param end},
 * {@param firstDisplay}, {@param totalDisplay} and {@param foldingFeature}.
 *
 * @param targetScreenPosition : the target position you want to check.
 * @param start : the start of the screen in the x or y axis depending on the device orientation.
 * @param end : the end of the screen in the x or y axis depending on the device orientation.
 * @param firstDisplay : width or height (depending on the device orientation) of the
 * left/start/first display area
 * @param totalDisplay : width or height (depending on the device orientation) of the
 * total display area
 * @param foldingFeature : width or height (depending on the device orientation) of the
 * FoldingFeature {@see FoldingFeature} if any.
 * @return
 */
fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    start: Int,
    end: Int,
    firstDisplay: Int,
    totalDisplay: Int,
    foldingFeature: Int = 0
): Boolean {
    return when (targetScreenPosition) {
        DisplayPosition.DUAL ->
            start in 0..firstDisplay &&
                end in (firstDisplay + foldingFeature)..totalDisplay
        DisplayPosition.START ->
            start in 0..firstDisplay &&
                end in 0..firstDisplay
        DisplayPosition.END ->
            start in (firstDisplay + foldingFeature)..totalDisplay &&
                end in (firstDisplay + foldingFeature)..totalDisplay
    }
}
