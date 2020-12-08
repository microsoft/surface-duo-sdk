/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.graphics.Rect
import android.view.Surface
import com.microsoft.device.dualscreen.ScreenMode

/**
 * Internal application screen state.
 * Contains all required information used by [SurfaceDuoLayout] in order to draw his content.
 */
internal data class ScreenSavedState(
    /**
     * Application spanning mode. Can be one of the following values [ScreenMode.SINGLE_SCREEN] or [ScreenMode.DUAL_SCREEN]
     */
    val screenMode: ScreenMode = ScreenMode.SINGLE_SCREEN,

    /**
     * Contains only one screen rectangle, if the application spanning mode is [ScreenMode.SINGLE_SCREEN],
     * or two if the application spanning mode is [ScreenMode.DUAL_SCREEN]
     */
    val screenRectangles: List<Rect>? = null,

    /**
     * Contains the hinge coordinates.
     */
    val hingeRect: Rect? = Rect(0, 0, 0, 0),

    /**
     * The application orientation.
     * Can be one of the following values: [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180], [Surface.ROTATION_270]
     */
    val orientation: Int = Surface.ROTATION_0
) {
    override fun toString(): String {
        return "ScreenMode = " + screenMode.name +
            ", ScreenRectangles = " + screenRectangles +
            ", Hinge Rectangle = " + hingeRect +
            ", Orientation = " + orientation
    }
}