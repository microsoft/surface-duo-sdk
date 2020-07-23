/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.core.manager.ActivityLifecycle
import com.microsoft.device.dualscreen.core.manager.SurfaceDuoScreenManager

/**
 * Handles restore Fragments logic in single and dual screen modes
 * by swapping FragmentManagerState inside the bundle when an activity is recreated.
 */
class FragmentManagerStateHandler private constructor (
    app: Application,
    private val surfaceDuoScreenManager: SurfaceDuoScreenManager
) : ActivityLifecycle() {

    companion object {
        private var instance: FragmentManagerStateHandler? = null

        @JvmStatic
        fun getInstance(
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

    /**
     * Contains swap FragmentManagerState logic depending on the activity state:
     *  - Transition to dual screen or single screen
     *  - Resuming to single screen or dual screen
     */
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
        savedInstanceState?.let {
            when {
                // Transition from single to dual
                surfaceDuoScreenManager.screenMode == ScreenMode.SINGLE_SCREEN &&
                    ScreenHelper.isDualMode(activity) -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)
                }
                // Transition from dual to single
                surfaceDuoScreenManager.screenMode == ScreenMode.DUAL_SCREEN &&
                    ScreenHelper.isDualMode(activity).not() -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)
                }
                // Resume to Single
                // Handles case when an activity is left in dual screen state and resumed in single screen
                surfaceDuoScreenManager.screenMode == ScreenMode.SINGLE_SCREEN &&
                    ScreenHelper.isDualMode(activity).not() -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)
                }
                // Resume to Dual
                // Handles case when an activity is left in single screen state and resumed in dual screen
                surfaceDuoScreenManager.screenMode == ScreenMode.DUAL_SCREEN &&
                    ScreenHelper.isDualMode(activity) -> {
                    fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)
                }
                else -> {}
            }
        } ?: run {
            fragmentManagerStateMap[activity.getMapKey()] =
                FragmentManagerStateWrapper()
        }
    }
}

private fun Activity.getMapKey(): String = this::class.java.simpleName
