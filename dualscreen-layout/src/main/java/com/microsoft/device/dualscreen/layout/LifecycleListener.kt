/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Class that sets the SurfaceDuoInflaterFactory to every activity that is created inside an app.
 * This is done because we need to pass the SurfaceDuoScreenManager to every SurfaceDuoLayout object that
 * will be created.
 * Also we need to pass the activity to the SurfaceDuoScreenManager so that we can add an
 * OnGlobalLayoutListener to it. This way we can catch if an activity changed it's size and react
 * accordingly.
 */
internal class LifecycleListener(
    private val surfaceDuoScreenManager: SurfaceDuoScreenManager
) : Application.ActivityLifecycleCallbacks {
    
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        surfaceDuoScreenManager.addNewActivityLayoutListener(activity)
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
