/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect

/**
 * Abstract class used to retrieve basic screen information.
 */
abstract class AbstractScreenInfo(val context: Context) : ScreenInfo {
    private var hingeRect: Rect? = null

    /**
     * Returns the coordinates for hinge location.
     * This method also updates the hinge rectangle if it's null.
     * @return [Rect] object with hinge rectangle or null if device is not SurfaceDuo
     */
    override fun getHinge(): Rect? {
        updateHingeIfNull()
        return hingeRect
    }

    /**
     * Updates the hinge rectangle if it's null
     */
    override fun updateHingeIfNull() {
        if (hingeRect == null) {
            hingeRect = extractHinge()
        }
    }

    /**
     * Returns coordinates for hinge location
     * @return [Rect] object with hinge coordinates or null if device is not SurfaceDuo
     */
    protected abstract fun extractHinge(): Rect?

    /**
     * Check if the device is SurfaceDuo
     * @return [true] if the device is SurfaceDuo, false otherwise
     */
    override fun isSurfaceDuoDevice(): Boolean {
        val feature = "com.microsoft.device.display.displaymask"
        val packageManager: PackageManager = context.packageManager
        return packageManager.hasSystemFeature(feature)
    }

    /**
     * Returns the coordinates of the two screens of the device
     * @return list of the [Rect] objects containing the screen's coordinates
     */
    override fun getScreenRectangles(): List<Rect>? {
        return getScreenRectangles(getHinge(), getWindowRect())
    }
}