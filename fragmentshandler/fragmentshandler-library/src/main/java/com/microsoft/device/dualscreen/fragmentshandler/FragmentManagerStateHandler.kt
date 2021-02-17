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
import androidx.annotation.VisibleForTesting
import com.microsoft.device.dualscreen.ActivityLifecycle
import com.microsoft.device.dualscreen.ScreenInfoProvider
import com.microsoft.device.dualscreen.ScreenMode
import com.microsoft.device.dualscreen.Version
import com.microsoft.device.dualscreen.screenMode

/**
 * Wrapper class that restores the state of the fragments when the host activity is recreated.
 */
class FragmentManagerStateHandler private constructor(app: Application) : ActivityLifecycle() {

    companion object {
        @VisibleForTesting
        var instance: FragmentManagerStateHandler? = null

        @JvmStatic
        fun init(app: Application): FragmentManagerStateHandler {
            return instance ?: FragmentManagerStateHandler(app).also {
                instance = it
            }
        }
    }

    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    private var previousSpanningMode = ScreenMode.SINGLE_SCREEN
    private var currentSpanningMode = previousSpanningMode
    private var screenModeWasChanged = false

    private var oldOrientation = Configuration.ORIENTATION_UNDEFINED
    private var orientation = oldOrientation

    private var fragmentManagerStateMap = mutableMapOf<String, FragmentManagerStateWrapper>()

    /**
     * Clears the internal data.
     */
    @VisibleForTesting
    fun clear() {
        previousSpanningMode = ScreenMode.SINGLE_SCREEN
        currentSpanningMode = previousSpanningMode
        oldOrientation = Configuration.ORIENTATION_UNDEFINED
        orientation = oldOrientation
        screenModeWasChanged = false
        fragmentManagerStateMap.clear()
    }

    /**
     * Called as the first step of the Activity being created. This is always called before
     * [Activity.onCreate]
     */
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
        orientation = activity.resources.configuration.orientation
        if (ScreenInfoProvider.version == Version.DisplayMask) {
            detectSpanningMode(activity)
        }

        savedInstanceState?.let {
            if (screenModeWasChanged) {
                changeFragmentManagerState(activity, it)
            }
        } ?: run {
            fragmentManagerStateMap[activity.getMapKey()] = FragmentManagerStateWrapper()
        }
    }

    /**
     * Contains swap FragmentManagerState logic depending on the activity state:
     *  - Transition to dual screen or single screen
     *  - Resuming to single screen or dual screen
     */
    private fun changeFragmentManagerState(activity: Activity, savedInstanceState: Bundle) {
        when {
            // Transition from single-screen to dual-screen
            previousSpanningMode == ScreenMode.SINGLE_SCREEN &&
                currentSpanningMode == ScreenMode.DUAL_SCREEN ->
                fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)

            // Transition from dual-screen to single-screen
            previousSpanningMode == ScreenMode.DUAL_SCREEN &&
                currentSpanningMode == ScreenMode.SINGLE_SCREEN ->
                fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)

            // If not a screen rotation -> Resume previous Activity to single-screen FragmentManagerState
            // Handles case when an activity is left in dual screen state and resumed in single screen
            previousSpanningMode == ScreenMode.SINGLE_SCREEN &&
                currentSpanningMode == ScreenMode.SINGLE_SCREEN &&
                orientation == oldOrientation ->
                fragmentManagerStateMap[activity.getMapKey()]?.swapDualToSingle(savedInstanceState)

            // If not a screen rotation -> Resume previous Activity to dual-screen FragmentManager
            // Handles case when an activity is left in single screen state and resumed in dual screen
            previousSpanningMode == ScreenMode.DUAL_SCREEN &&
                currentSpanningMode == ScreenMode.DUAL_SCREEN &&
                orientation == oldOrientation ->
                fragmentManagerStateMap[activity.getMapKey()]?.swapSingleToDual(savedInstanceState)

            else -> {
            }
        }
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        super.onActivityPreDestroyed(activity)
        if (ScreenInfoProvider.version == Version.WindowManager) {
            detectSpanningMode(activity)
        }
    }

    private fun detectSpanningMode(activity: Activity) {
        val screenInfo = ScreenInfoProvider.getScreenInfo(activity)
        val nextSpanningMode = screenInfo.screenMode
        screenModeWasChanged = currentSpanningMode != nextSpanningMode
        if (screenModeWasChanged) {
            previousSpanningMode = currentSpanningMode
            currentSpanningMode = nextSpanningMode
        }
    }

    /**
     * Called when the Activity calls [Activity.onResume] super.onResume().
     */
    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        oldOrientation = activity.resources.configuration.orientation
    }
}

private fun Activity.getMapKey(): String = this::class.java.simpleName
