/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Abstract implementation for [Application.ActivityLifecycleCallbacks]
 */
abstract class ActivityLifecycle : Application.ActivityLifecycleCallbacks {
    /**
     * Called when the Activity calls [Activity.onPause] super.onPause()
     */
    override fun onActivityPaused(activity: Activity) = Unit

    /**
     * Called when the Activity calls [Activity.onStart] super.onStart().
     */
    override fun onActivityStarted(activity: Activity) = Unit

    /**
     * Called when the Activity calls [Activity.onDestroy] super.onDestroy().
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    /**
     * Called when the Activity calls [Activity.onSaveInstanceState] super.onSaveInstanceState().
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    /**
     * Called when the Activity calls [Activity.onStop] super.onStop().
     */
    override fun onActivityStopped(activity: Activity) = Unit

    /**
     * Called when the Activity calls [Activity.onCreate] super.onCreate().
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    /**
     * Called when the Activity calls [Activity.onResume] super.onResume().
     */
    override fun onActivityResumed(activity: Activity) = Unit
}