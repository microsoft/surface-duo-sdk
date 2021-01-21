/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.graphics.Rect
import android.view.Surface

/**
 * Interface containing methods used to retrieve screen information.
 */
interface ScreenInfo {
    /**
     * Returns coordinates for hinge location
     * @return [Rect] object with hinge coordinates or null if device is not SurfaceDuo
     */
    fun getHinge(): Rect?

    /**
     * Updates the hinge rectangle if it's null
     */
    fun updateHingeIfNull()

    /**
     * Check if the device is SurfaceDuo
     * @return [true] if the device is SurfaceDuo, false otherwise
     */
    fun isSurfaceDuoDevice(): Boolean

    /**
     * Returns coordinates of the entire device window
     * @return [Rect] object with the device window coordinates
     *
     */
    fun getWindowRect(): Rect

    /**
     * Returns the coordinates of the two screens of the device
     * @return list of the [Rect] objects containing the screen's coordinates
     */
    fun getScreenRectangles(): List<Rect>?

    /**
     * Check if the application is in dual-screen mode or not.
     * @return [true] if the application is in dual screen mode, [false] otherwise
     */
    fun isDualMode(): Boolean

    /**
     * Returns a constant int for the rotation of the screen
     * according to the rotation the function will return:
     * [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180], [Surface.ROTATION_270]
     * @return the screen rotation
     */
    fun getScreenRotation(): Int
}