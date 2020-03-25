/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.app.Application
import android.graphics.Rect

import java.util.ArrayList

/***
 * SurfaceDuoScreenManager should be initialized only in onCreate() function
 * from Application object.
 *
 * The class is responsible for adding an OnGlobalLayoutListener to the activity passed as
 * a parameter. When an activity's layout is resizing, the class will
 * notify all ScreenModeListeners that the screen mode changed.
 */
class SurfaceDuoScreenManager private constructor() {
    private val lifecycleListener = LifecycleListener(this)
    internal var isScreenManagerInitialized = false

    companion object {
        internal val instance by lazy { SurfaceDuoScreenManager() }
        private const val SINGLE_SCREEN_MODE = 1
        private const val DUAL_SCREEN_MODE = 2
        private val screenModeListeners = ArrayList<ScreenModeListener>()
        private var screenMode = -1

        @JvmStatic fun init(app: Application): SurfaceDuoScreenManager {
            app.registerActivityLifecycleCallbacks(instance.lifecycleListener)
            instance.isScreenManagerInitialized = true
            return instance
        }

        @JvmStatic fun clearScreenModeListeners() {
            screenModeListeners.clear()
        }
    }

    internal fun addNewActivityLayoutListener(activity: Activity) {
        clearScreenModeListeners()
        screenMode = -1
        val isDeviceSurfaceDuo = ScreenHelper.isDeviceSurfaceDuo(activity)
        if (isDeviceSurfaceDuo) {
            val hinge = ScreenHelper.getHinge(activity)
            val drawingRect = ScreenHelper.getWindowRect(activity)
            activity.window.decorView.rootView.viewTreeObserver
                .addOnGlobalLayoutListener {
                    // changeLayout() will be called when the activity's size is changed
                    changeLayout(false, hinge, drawingRect)
                }
        } else {
            // Lock into SINGLE_SCREEN_MODE
            activity.window.decorView.rootView.viewTreeObserver
                .addOnGlobalLayoutListener {
                    if (screenMode != SINGLE_SCREEN_MODE) {
                        for (screenModeListener in screenModeListeners) {
                            screenModeListener.onSwitchToSingleScreenMode()
                        }
                        screenMode = SINGLE_SCREEN_MODE
                    }
                }
        }
    }

    private fun changeLayout(forceChangeLayout: Boolean?, hinge: Rect?, drawingRect: Rect) {
        if (drawingRect.width() > 0 && drawingRect.height() > 0) {
            // The drawingRect don't intersect hinge
            if (hinge != null && !hinge.intersect(drawingRect)) {
                if (screenMode != SINGLE_SCREEN_MODE || forceChangeLayout!!) {
                    for (screenModeListener in screenModeListeners) {
                        screenModeListener.onSwitchToSingleScreenMode()
                    }
                    screenMode = SINGLE_SCREEN_MODE
                }
            } else {
                if (screenMode != DUAL_SCREEN_MODE || forceChangeLayout!!) {
                    for (screenModeListener in screenModeListeners) {
                        screenModeListener.onSwitchToDualScreenMode()
                    }
                    screenMode = DUAL_SCREEN_MODE
                }
            }
        }
    }

    fun forceChangeLayout(activity: Activity) {
        val hinge = ScreenHelper.getHinge(activity)
        val drawingRect = ScreenHelper.getWindowRect(activity)
        changeLayout(true, hinge, drawingRect)
    }

    fun addScreenModeListener(screenModeListener: ScreenModeListener) {
        screenModeListeners.add(screenModeListener)
    }

    fun removeScreenModeListener(screenModeListener: ScreenModeListener) {
        screenModeListeners.remove(screenModeListener)
    }

}
