/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule

class CurrentActivityDelegate {
    private var _currentActivity: Activity? = null
    val currentActivity: FragmentActivity?
        get() = _currentActivity as? FragmentActivity

    private val activityLifecycleCallback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            _currentActivity = activity
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            _currentActivity = activity
        }
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            _currentActivity = null
        }
    }

    fun <T : Activity> setup(activityScenarioRule: ActivityScenarioRule<T>) {
        activityScenarioRule.scenario.onActivity {
            it.application.registerActivityLifecycleCallbacks(activityLifecycleCallback)
        }
    }

    fun <T : Activity> clear(activityScenarioRule: ActivityScenarioRule<T>) {
        activityScenarioRule.scenario.onActivity {
            it.application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
        }
    }
}