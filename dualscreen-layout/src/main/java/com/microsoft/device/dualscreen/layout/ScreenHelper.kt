/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.view.Surface
import android.view.WindowManager
import com.microsoft.device.display.DisplayMask
import java.lang.IllegalStateException

/**
 * Class that offers different screen information depending on the screen mode
 */
class ScreenHelper {

    companion object {

        /**
         * Returns coordinates for hinge location
         * @return Rect object with coordinates or null if device is not SurfaceDuo
         */
        @JvmStatic fun getHinge(activity: Activity): Rect? {
            // Hinge's coordinates of its 4 edges in different mode
            // Double Landscape Rect(0, 1350 - 1800, 1434)
            // Double Portrait  Rect(1350, 0 - 1434, 1800)
            return if (isDeviceSurfaceDuo(activity)) {
                val displayMask = DisplayMask.fromResourcesRectApproximation(activity)
                if (displayMask != null) {
                    val screensBounding = displayMask.getBoundingRectsForRotation(
                        getCurrentRotation(activity)
                    )
                    if (screensBounding.size == 0) {
                        Rect(0, 0, 0, 0)
                    } else screensBounding[0]
                } else { null }
            } else { null }
        }

        /**
         * Returns if device is SurfaceDuo
         */
        @JvmStatic fun isDeviceSurfaceDuo(activity: Activity): Boolean {
            val feature = "com.microsoft.device.display.displaymask"
            val pm: PackageManager = activity.packageManager
            return pm.hasSystemFeature(feature)
        }

        /**
         * Returns coordinates of the entire device window
         * @return Rect object with coordinates
         */
        @JvmStatic internal fun getWindowRect(activity: Activity): Rect {
            val windowRect = Rect()
            activity.windowManager.defaultDisplay.getRectSize(windowRect)
            return windowRect
        }

        /**
         * Returns the coordinates of the two screens of the SurfaceDuo
         * @return List of the Rect objects containing the coordinates
         */
        @JvmStatic fun getScreenRectangles(activity: Activity): List<Rect> {
            val screenRect1 = Rect()
            val screenRect2 = Rect()
            val hinge = getHinge(activity)
            val windowRect = getWindowRect(activity)

            // Hinge's coordinates of its 4 edges in different mode
            // Double Landscape Rect(0, 1350 - 1800, 1434)
            // Double Portrait  Rect(1350, 0 - 1434, 1800)
            if (hinge != null) {
                if (hinge.left > 0) {
                    screenRect1.left = 0
                    screenRect1.right = hinge.left
                    screenRect1.top = 0
                    screenRect1.bottom = windowRect.bottom
                    screenRect2.left = hinge.right
                    screenRect2.right = windowRect.right
                    screenRect2.top = 0
                    screenRect2.bottom = windowRect.bottom
                } else {
                    screenRect1.left = 0
                    screenRect1.right = windowRect.right
                    screenRect1.top = 0
                    screenRect1.bottom = hinge.top
                    screenRect2.left = 0
                    screenRect2.right = windowRect.right
                    screenRect2.top = hinge.bottom
                    screenRect2.bottom = windowRect.bottom
                }
            }

            return listOf(screenRect1, screenRect2)
        }

        /**
         * Returns if device is in Dual-screen mode or not
         */
        @JvmStatic fun isDualMode(activity: Activity): Boolean {
            val hinge = getHinge(activity)
            val windowRect = getWindowRect(activity)

            return if (hinge != null && windowRect.width() > 0 && windowRect.height() > 0) {
                // The windowRect doesn't intersect hinge
                hinge.intersect(windowRect)
            } else false
        }

        /**
         * Returns a constant int for the rotation of the screen
         * According to the rotation the function will return:
         * {Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270}
         */
        @JvmStatic fun getCurrentRotation(activity: Activity): Int {
            return try {
                val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.rotation
            } catch (e: IllegalStateException) { Surface.ROTATION_0 }
        }
    }
}
