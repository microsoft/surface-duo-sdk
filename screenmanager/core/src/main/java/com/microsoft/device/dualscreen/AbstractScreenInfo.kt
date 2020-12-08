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