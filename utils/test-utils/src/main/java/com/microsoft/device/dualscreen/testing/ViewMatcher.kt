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
 * Checks if view given its position {@see DisplayPosition} is on the display given its dimensions.
 *
 * @param position: target position.
 * @param orientation: orientation of the display. See Configuration {@see Configuration}.
 * @param firstDisplayWith : width of the left/start/first display area
 * @param totalDisplayWith : width of the right/end/second/total display area
 * @param foldingFeatureWidth : width of the FoldingFeature {@see FoldingFeature} if any.
 * @return
 */
fun isViewOnScreen(
    position: DisplayPosition,
    orientation: Int,
    firstDisplayWith: Int,
    totalDisplayWith: Int,
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
                firstDisplayWith = firstDisplayWith,
                totalDisplayWith = totalDisplayWith,
                foldingFeatureWidth = foldingFeatureWidth
            )
        }
    }

/**
 * Checks in the given coordinates {@see targetScreenPosition} are on target screen defined by
 * {@see xStart}, {@see xEnd}, {@see firstDisplayWith}, {@see totalDisplayWith} and {@see foldingFeatureWidth}.
 *
 * @param targetScreenPosition : the target position you want to check.
 * @param xStart : the start of the screen in the x-axis
 * @param xEnd : the end of the screen in the y-axis
 * @param firstDisplayWith : the width of the first display.
 * @param totalDisplayWith : the width of the total display area.
 * @param foldingFeatureWidth : the width of the folding feature (if any).
 * @return
 */
fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    xStart: Int,
    xEnd: Int,
    firstDisplayWith: Int,
    totalDisplayWith: Int,
    foldingFeatureWidth: Int = 0
): Boolean {
    return when (targetScreenPosition) {
        DisplayPosition.DUAL ->
            xStart in 0..firstDisplayWith &&
                    xEnd in (firstDisplayWith + foldingFeatureWidth)..totalDisplayWith
        DisplayPosition.START ->
            xStart in 0..firstDisplayWith &&
                    xEnd in 0..firstDisplayWith
        DisplayPosition.END ->
            xStart in (firstDisplayWith + foldingFeatureWidth)..totalDisplayWith &&
                    xEnd in (firstDisplayWith + foldingFeatureWidth)..totalDisplayWith
    }
}
