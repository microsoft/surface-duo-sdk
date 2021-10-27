/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.graphics.Rect
import android.widget.LinearLayout

/**
 * Computes the screen rectangles using the hinge rectangle and entire screen rectangle.
 *
 * @return a list containing a single rectangle, if the device is in single screen mode,
 * or two rectangles, if the device is in dual screen mode
 */
fun getScreenRectangles(hingeRect: Rect?, windowRect: Rect): List<Rect>? {
    val startScreenRect = Rect()
    val endScreenRect = Rect()

    if (hingeRect != null) {
        if (hingeRect.left > 0) {
            startScreenRect.left = 0
            startScreenRect.right = hingeRect.left
            startScreenRect.top = 0
            startScreenRect.bottom = windowRect.bottom

            endScreenRect.left = hingeRect.right
            endScreenRect.right = windowRect.right
            endScreenRect.top = 0
            endScreenRect.bottom = windowRect.bottom
        } else {
            startScreenRect.left = 0
            startScreenRect.right = windowRect.right
            startScreenRect.top = 0
            startScreenRect.bottom = hingeRect.top

            endScreenRect.left = 0
            endScreenRect.right = windowRect.right
            endScreenRect.top = hingeRect.bottom
            endScreenRect.bottom = windowRect.bottom
        }
    }

    val screenRectangles = mutableListOf<Rect>()
    if (!startScreenRect.isEmpty) {
        screenRectangles.add(startScreenRect)
    }

    if (!endScreenRect.isEmpty) {
        screenRectangles.add(endScreenRect)
    }

    return if (screenRectangles.isNotEmpty()) {
        screenRectangles
    } else {
        null
    }
}

/**
 * If the hinge is present but the window rectangle doesn't intersect it, then recalculate the window rect
 * as the given window rect + hinge rect + window rect
 *
 * @note The result will not be accurate if the device has two screens with different sizes
 *
 * @param hingeRect The hinge rectangle
 * @param windowRect The window rectangle
 * @param orientation The screen orientation
 */
fun normalizeWindowRect(
    hingeRect: Rect?,
    windowRect: Rect,
    orientation: Int
): Rect {
    return if (hingeRect != null && !Rect.intersects(windowRect, hingeRect)) {
        return if (orientation == LinearLayout.HORIZONTAL) {
            Rect(
                windowRect.left,
                windowRect.top,
                2 * windowRect.width() + hingeRect.width(),
                windowRect.bottom
            )
        } else {
            Rect(
                windowRect.left,
                windowRect.top,
                windowRect.right,
                2 * windowRect.height() + hingeRect.height()
            )
        }
    } else {
        windowRect
    }
}