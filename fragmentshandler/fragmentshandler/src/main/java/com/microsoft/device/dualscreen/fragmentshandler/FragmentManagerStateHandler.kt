/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.screenMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

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

    private var previousSpanningMode: ScreenMode? = null
    private var currentSpanningMode = previousSpanningMode
    private var screenModeWasChanged = false

    private var oldOrientation = Configuration.ORIENTATION_UNDEFINED
    private var orientation = oldOrientation
    private var orientationWasChanged = false

    private var fragmentManagerStateMap = mutableMapOf<String, FragmentManagerStateWrapper>()

    private var job: Job? = null
    private fun registerWindowInfoFlow(activity: Activity) {
        job = MainScope().launch {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .collectIndexed { index, info ->
                    if (index == 0 && !orientationWasChanged) {
                        checkForScreenModeChanges(info)
                    }
                }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        job?.cancel()
    }
    /**
     * Clears the internal data.
     */
    @VisibleForTesting
    fun clear() {
        previousSpanningMode = null
        currentSpanningMode = previousSpanningMode
        screenModeWasChanged = false

        oldOrientation = Configuration.ORIENTATION_UNDEFINED
        orientation = oldOrientation
        orientationWasChanged = false
        fragmentManagerStateMap.clear()
    }

    /**
     * Called as the first step of the Activity being created. This is always called before
     * [Activity.onCreate]
     */
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        registerWindowInfoFlow(activity)
        checkForOrientationChanges(activity)

        savedInstanceState?.let {
            if (shouldSwapStates()) {
                changeFragmentManagerState(activity, it)
            }
        } ?: run {
            fragmentManagerStateMap[activity.getMapKey()] = FragmentManagerStateWrapper()
        }
        super.onActivityPreCreated(activity, savedInstanceState)
    }

    /**
     * Check if the device orientation was changed
     *
     * @param activity [Activity] used to retrieve the current device orientation
     */
    private fun checkForOrientationChanges(activity: Activity) {
        orientation = activity.displayOrientation
        if (oldOrientation == Configuration.ORIENTATION_UNDEFINED) {
            oldOrientation = orientation
        }
        orientationWasChanged = orientation != oldOrientation
    }

    /**
     * Returns [true] if the app bundle can be swapped from one state to another, [false] otherwise
     */
    private fun shouldSwapStates(): Boolean = !orientationWasChanged && screenModeWasChanged

    /**
     * Contains swap FragmentManagerState logic depending on the activity state:
     *  - Transition to dual screen or single screen
     *  - Resuming to single screen or dual screen
     */
    private fun changeFragmentManagerState(activity: Activity, savedInstanceState: Bundle) {
        fragmentManagerStateMap[activity.getMapKey()]?.swap(savedInstanceState)
    }

    /**
     * Check if screen mode was changed from single to dual screen or vice versa
     *
     * @param windowLayoutInfo [WindowLayoutInfo] object received from [WindowManager]
     */
    private fun checkForScreenModeChanges(windowLayoutInfo: WindowLayoutInfo) {
        val nextSpanningMode = windowLayoutInfo.screenMode
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
        oldOrientation = activity.displayOrientation
    }
}

val Context.displayOrientation: Int
    get() {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.rotation
        } else {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay?.rotation
        } ?: Surface.ROTATION_0

        return when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> Configuration.ORIENTATION_PORTRAIT
            else -> Configuration.ORIENTATION_LANDSCAPE
        }
    }

private fun Activity.getMapKey(): String = this::class.java.simpleName