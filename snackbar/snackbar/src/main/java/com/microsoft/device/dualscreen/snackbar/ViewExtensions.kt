package com.microsoft.device.dualscreen.snackbar

import android.view.View

data class Point(val x: Int, val y: Int)

/**
 * <p>Computes the coordinates of this view on the screen.
 *
 * @return a [Point] object that holds the coordinates
 */
val View.locationOnScreen: Point
    get() {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return Point(location[0], location[1])
    }