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
 * Returns a Matcher that checks if a View is shown in the display area given its position and screen dimensions.
 *
 * @param position: target position.
 * @param orientation: orientation of the display. See Configuration {@see Configuration}.
 * @param firstDisplayWidth : width of the left/start/first display area
 * @param totalDisplayWidth : width of the right/end/second/total display area
 * @param foldingFeatureWidth : width of the FoldingFeature {@see FoldingFeature} if any.
 * @return
 */
fun isViewOnScreen(
    position: DisplayPosition,
    orientation: Int,
    firstDisplayWidth: Int,
    totalDisplayWidth: Int,
    foldingFeatureWidth: Int = 0
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
                xStart = start,
                xEnd = end,
                firstDisplayWidth = firstDisplayWidth,
                totalDisplayWidth = totalDisplayWidth,
                foldingFeatureWidth = foldingFeatureWidth
            )
        }
    }

/**
 * Checks whether a specific screen-position is the screen defined by {@param xStart}, {@param xEnd},
 * {@param firstDisplayWith}, {@param totalDisplayWith} and {@param foldingFeatureWidth}.
 *
 * @param targetScreenPosition : the target position you want to check.
 * @param xStart : the start of the screen in the x-axis
 * @param xEnd : the end of the screen in the y-axis
 * @param firstDisplayWidth : the width of the first display.
 * @param totalDisplayWidth : the width of the total display area.
 * @param foldingFeatureWidth : the width of the folding feature (if any).
 * @return
 */
fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    xStart: Int,
    xEnd: Int,
    firstDisplayWidth: Int,
    totalDisplayWidth: Int,
    foldingFeatureWidth: Int = 0
): Boolean {
    return when (targetScreenPosition) {
        DisplayPosition.DUAL ->
            xStart in 0..firstDisplayWidth &&
                xEnd in (firstDisplayWidth + foldingFeatureWidth)..totalDisplayWidth
        DisplayPosition.START ->
            xStart in 0..firstDisplayWidth &&
                xEnd in 0..firstDisplayWidth
        DisplayPosition.END ->
            xStart in (firstDisplayWidth + foldingFeatureWidth)..totalDisplayWidth &&
                xEnd in (firstDisplayWidth + foldingFeatureWidth)..totalDisplayWidth
    }
}
