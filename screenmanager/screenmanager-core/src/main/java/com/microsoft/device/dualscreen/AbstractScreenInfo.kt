/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.Context
import android.graphics.Rect

/**
 * Abstract class used to retrieve basic screen information.
 */
abstract class AbstractScreenInfo(val context: Context) : ScreenInfo {
    private var hingeRect: Rect? = null
    private var _isInDualMode: Boolean? = null

    /**
     * Check if the application is in dual-screen mode or not.
     * This method also updates the screen mode flag if it's null.
     *
     * @return [true] if the application is in dual screen mode, [false] otherwise
     */
    override fun isDualMode(): Boolean {
        updateScreenModeIfNull()
        return _isInDualMode ?: false
    }

    /**
     * Updates the screen mode flag if it's null
     */
    override fun updateScreenModeIfNull() {
        if (_isInDualMode == null) {
            _isInDualMode = checkForDualMode()
        }
    }

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
     * Check if the device is in Dual-screen mode or not.
     * @return [true] if the device is in dual screen mode, [false] otherwise
     */
    protected abstract fun checkForDualMode(): Boolean

    /**
     * Check if the device is SurfaceDuo
     * @return [true] if the device is SurfaceDuo, false otherwise
     */
    override fun isSurfaceDuoDevice(): Boolean {
        return context.isSurfaceDuoDevice()
    }

    /**
     * Returns the coordinates of the two screens of the device
     * @return list of the [Rect] objects containing the screen's coordinates
     */
    override fun getScreenRectangles(): List<Rect>? {
        return getScreenRectangles(getHinge(), getWindowRect())
    }
}