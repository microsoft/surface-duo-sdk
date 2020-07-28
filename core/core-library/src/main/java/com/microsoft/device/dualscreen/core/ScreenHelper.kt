/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.core

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
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
        @JvmStatic fun getHinge(context: Context): Rect? {
            // Hinge's coordinates of its 4 edges in different mode
            // Dual Landscape Rect(0, 1350 - 1800, 1434)
            // Dual Portrait  Rect(1350, 0 - 1434, 1800)
            return if (isDeviceSurfaceDuo(context)) {
                val displayMask = DisplayMask.fromResourcesRectApproximation(context)
                if (displayMask != null) {
                    val screensBounding = displayMask.getBoundingRectsForRotation(
                        getCurrentRotation(context)
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
        @JvmStatic fun isDeviceSurfaceDuo(context: Context): Boolean {
            val feature = "com.microsoft.device.display.displaymask"
            val pm: PackageManager = context.packageManager
            return pm.hasSystemFeature(feature)
        }

        /**
         * Returns coordinates of the entire device window
         * @return Rect object with coordinates
         *
         */
        @JvmStatic fun getWindowRect(context: Context): Rect {
            val windowRect = Rect()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getRectSize(windowRect)
            return windowRect
        }

        /**
         * Returns the coordinates of the two screens of the SurfaceDuo
         * @return List of the Rect objects containing the coordinates
         */
        @JvmStatic fun getScreenRectangles(context: Context): List<Rect>? {
            val screenRect1 = Rect()
            val screenRect2 = Rect()
            val hinge = getHinge(context)
            val windowRect = getWindowRect(context)

            // Hinge's coordinates of its 4 edges in different mode
            // Dual Landscape Rect(0, 1350 - 1800, 1434)
            // Dual Portrait  Rect(1350, 0 - 1434, 1800)
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

            return if (!screenRect1.isEmpty && !screenRect2.isEmpty) {
                listOf(screenRect1, screenRect2)
            } else {
                Log.e(
                    ScreenHelper::class.java.simpleName,
                    "Could NOT retrieve dual screens dimensions"
                )
                null
            }
        }

        /**
         * Returns if device is in Dual-screen mode or not
         */
        @JvmStatic fun isDualMode(context: Context): Boolean {
            val hinge = getHinge(context)
            val windowRect = getWindowRect(context)

            return if (hinge != null && windowRect.width() > 0 && windowRect.height() > 0) {
                // The windowRect doesn't intersect hinge
                hinge.intersect(windowRect)
            } else false
        }

        /**
         * Returns a constant int for the rotation of the screen
         * according to the rotation the function will return:
         * {Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270}
         */
        @JvmStatic fun getCurrentRotation(context: Context): Int {
            return try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.rotation
            } catch (e: IllegalStateException) { Surface.ROTATION_0 }
        }
    }
}
