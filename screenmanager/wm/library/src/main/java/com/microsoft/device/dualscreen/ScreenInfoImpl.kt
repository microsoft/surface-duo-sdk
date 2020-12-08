/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.view.Surface
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager

/**
 * Class that offers different screen information depending on the screen mode
 */
internal class ScreenInfoImpl(context: Context) : AbstractScreenInfo(context) {
    /**
     * Check if the device is in Dual-screen mode or not.
     * @return [true] if the device is in dual screen mode, [false] otherwise
     */
    override fun isDualMode(): Boolean {
        return getWindowLayoutInfo()?.displayFeatures?.isEmpty() == false
    }

    /**
     * [WindowManager] instance used to retrieve information about screen
     */
    private var windowManager = WindowManager(context, null)

    /**
     * Returns coordinates for hinge location
     * @return [Rect] object with hinge coordinates or null if device is not SurfaceDuo
     */
    override fun getHinge(): Rect? {
        // Hinge's coordinates of its 4 edges in different mode
        // Dual Landscape Rect(0, 1350 - 1800, 1434)
        // Dual Portrait  Rect(1350, 0 - 1434, 1800)
        val windowLayoutInfo = getWindowLayoutInfo()
        return windowLayoutInfo?.let {
            val screenBounds = it.displayFeatures
            if (screenBounds.size == 0) {
                Rect(0, 0, 0, 0)
            } else {
                screenBounds.first().bounds
            }
        }
    }

    /**
     * Get current window layout information for the associated [Context]. Must be called
     * only after it is attached to the window and the layout pass has happened, otherwise will return [null]
     * @see [Activity.onAttachedToWindow]
     * @see [WindowLayoutInfo]
     */
    private fun getWindowLayoutInfo(): WindowLayoutInfo? {
        val activity = getActivityFromContext()
        return if (activity?.window?.decorView?.isAttachedToWindow == true) {
            return windowManager.windowLayoutInfo
        } else {
            null
        }
    }

    /**
     * Unwrap the hierarchy of [ContextWrapper]-s until [Activity] is reached.
     * @return Base [Activity] context or [null] if not available.
     */
    private fun getActivityFromContext(): Activity? {
        var contextBuffer = context
        while (contextBuffer is ContextWrapper) {
            if (contextBuffer is Activity) {
                return contextBuffer
            }
            contextBuffer = contextBuffer.baseContext
        }
        return null
    }

    /**
     * Returns coordinates of the entire device window
     * @return [Rect] object with the device window coordinates
     */
    override fun getWindowRect(): Rect {
        val windowRect = Rect()
        val activity = getActivityFromContext()
        activity?.windowManager?.defaultDisplay?.getRectSize(windowRect)
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
            val activity = getActivityFromContext()
            activity?.let {
                it.windowManager.defaultDisplay.rotation
            } ?: Surface.ROTATION_0
        } catch (e: IllegalStateException) {
            Surface.ROTATION_0
        }
    }
}
