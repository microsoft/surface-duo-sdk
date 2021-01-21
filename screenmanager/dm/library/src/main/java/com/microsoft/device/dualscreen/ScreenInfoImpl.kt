/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen

import android.content.Context
import android.graphics.Rect
import android.view.Surface
import android.view.WindowManager
import com.microsoft.device.display.DisplayMask

/**
 * Class that offers different screen information depending on the screen mode
 */
internal class ScreenInfoImpl(context: Context) : AbstractScreenInfo(context) {
    /**
     * Check if the device is in Dual-screen mode or not.
     * @return [true] if the device is in dual screen mode, [false] otherwise
     */
    override fun isDualMode(): Boolean {
        val hinge = getHinge()
        val windowRect = getWindowRect()

        return if (hinge != null && windowRect.width() > 0 && windowRect.height() > 0) {
            // The windowRect doesn't intersect hinge
            hinge.intersect(windowRect)
        } else false
    }

    /**
     * Returns coordinates for hinge location
     * @return [Rect] object with hinge coordinates or null if device is not SurfaceDuo
     */
    override fun extractHinge(): Rect? {
        // Hinge's coordinates of its 4 edges in different mode
        // Dual Landscape Rect(0, 1350 - 1800, 1434)
        // Dual Portrait  Rect(1350, 0 - 1434, 1800)
        return if (isSurfaceDuoDevice()) {
            val displayMask = DisplayMask.fromResourcesRectApproximation(context)
            if (displayMask != null) {
                val screensBounding = displayMask.getBoundingRectsForRotation(getScreenRotation())
                if (screensBounding.size == 0) {
                    Rect(0, 0, 0, 0)
                } else {
                    screensBounding[0]
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Returns coordinates of the entire device window
     * @return [Rect] object with the device window coordinates
     *
     */
    override fun getWindowRect(): Rect {
        val windowRect = Rect()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.defaultDisplay?.getRectSize(windowRect)
        return windowRect
    }

    /**
     * Returns a constant int for the rotation of the screen
     * according to the rotation the function will return:
     * [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180], [Surface.ROTATION_270]
     * @return the screen rotation
     */
    override fun getScreenRotation(): Int {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.rotation
        } catch (e: IllegalStateException) {
            Surface.ROTATION_0
        }
    }
}
