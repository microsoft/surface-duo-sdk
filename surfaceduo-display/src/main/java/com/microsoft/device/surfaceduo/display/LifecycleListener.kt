/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.surfaceduo.display

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat

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

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        surfaceDuoScreenManager.addNewActivityLayoutListener(activity)
        val inflater = LayoutInflater.from(activity)
        LayoutInflaterCompat.setFactory2(inflater,
            SurfaceDuoInflaterFactory(
                surfaceDuoScreenManager
            )
        )
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
