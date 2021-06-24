/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.graphics.Rect

/**
 * Computes the screen rectangles using the hinge rectangle and entire screen rectangle.
 *
 * @return a list containing a single rectangle, if the device is in single screen mode,
 * or two rectangles, if the device is in dual screen mode
 */
fun getScreenRectangles(hingeRect: Rect?, windowRect: Rect): List<Rect>? {
    val startScreenRect = Rect()
    val endScreenRect = Rect()

    // hingeRect's coordinates of its 4 edges in different mode
    // Dual Landscape Rect(0, 1350 - 1800, 1434)
    // Dual Portrait  Rect(1350, 0 - 1434, 1800)
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