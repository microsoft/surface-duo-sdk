/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.core.manager.ActivityLifecycle
import com.microsoft.device.dualscreen.core.manager.SurfaceDuoScreenManager

/**
 * Wrapper class that restores the state of the fragments when the host activity is recreated.
 */
class FragmentManagerStateHandler private constructor (
    app: Application,
    private val surfaceDuoScreenManager: SurfaceDuoScreenManager
) : ActivityLifecycle() {

    companion object {
        private var instance: FragmentManagerStateHandler? = null

        @JvmStatic
        fun initialize(
            app: Application,
            surfaceDuoScreenManager: SurfaceDuoScreenManager
        ): FragmentManagerStateHandler {
            return instance
                ?: FragmentManagerStateHandler(
                    app, surfaceDuoScreenManager
                ).also {
                    instance = it
                }
        }
    }

    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    private var fragmentManagerStateMap = mutableMapOf<String, FragmentManagerStateWrapper>()
    private var oldOrientation = Configuration.ORIENTATION_UNDEFINED
    private var orientation = Configuration.ORIENTATION_UNDEFINED

    /**
     * Contains swap FragmentManagerState logic depending on the activity state:
     *  - Transition to dual screen or single screen
     *  - Resuming to single screen or dual screen
     */
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
        orientation = activity.resources.configuration.orientation
        savedInstanceState?.let {
            when {
                // Transition from single-screen to dual-screen
                surfaceDuoScreenManager.screenMode == ScreenMode.SINGLE_SCREEN &&
                    ScreenHelper.isDualMode(activity) -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)
                }
                // Transition from dual-screen to single-screen
                surfaceDuoScreenManager.screenMode == ScreenMode.DUAL_SCREEN &&
                    ScreenHelper.isDualMode(activity).not() -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)
                }
                // If not a screen rotation -> Resume previous Activity to single-screen FragmentManagerState
                // Handles case when an activity is left in dual screen state and resumed in single screen
                surfaceDuoScreenManager.screenMode == ScreenMode.SINGLE_SCREEN &&
                    ScreenHelper.isDualMode(activity).not() &&
                    orientation == oldOrientation -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)
                }
                // If not a screen rotation -> Resume previous Activity to dual-screen FragmentManager
                // Handles case when an activity is left in single screen state and resumed in dual screen
                surfaceDuoScreenManager.screenMode == ScreenMode.DUAL_SCREEN &&
                    ScreenHelper.isDualMode(activity) &&
                    orientation == oldOrientation -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)
                }
                else -> {}
            }
        } ?: run {
            fragmentManagerStateMap[activity.getMapKey()] =
                FragmentManagerStateWrapper()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        oldOrientation = activity.resources.configuration.orientation
    }
}

private fun Activity.getMapKey(): String = this::class.java.simpleName
