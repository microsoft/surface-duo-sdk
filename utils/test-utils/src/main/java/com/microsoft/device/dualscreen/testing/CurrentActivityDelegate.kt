/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Activity delegate used to setup and support a test scenario.
 * This class is useful whenever you want to ensure that the Activity is running
 * before use any view action or assertion.
 */
class CurrentActivityDelegate {
    private var activityStartedCountDownLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)

    private var _currentActivity: Activity? = null
    val currentActivity: AppCompatActivity?
        get() = _currentActivity as? AppCompatActivity

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

    /**
     * Setup using a given ActivityScenarioRule {@see ActivityScenarioRule}
     *
     * @param T : of Activity type
     * @param activityScenarioRule : new scenario rule
     */
    fun <T : Activity> setup(activityScenarioRule: ActivityScenarioRule<T>) {
        activityScenarioRule.scenario.onActivity {
            _currentActivity = it
            it.application.registerActivityLifecycleCallbacks(activityLifecycleCallback)
        }
    }

    /**
     * Clear
     *
     * @param T : of Activity type.
     * @param activityScenarioRule : currently used ActivityScenarioRule
     */
    fun <T : Activity> clear(activityScenarioRule: ActivityScenarioRule<T>) {
        activityScenarioRule.scenario.onActivity {
            it.application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
        }
    }

    /**
     * Resets the last started [Activity] to {@code null}
     */
    fun resetActivity() {
        _currentActivity = null
    }

    /**
     * Resets activity counter when waiting for activity to start before calling
     * [waitForActivity].
     */
    fun resetActivityCounter() {
        activityStartedCountDownLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)
    }

    /**
     * Blocks and waits for the next activity to be started.
     * @return {@code true} if the activity was started before the timeout count reached zero and
     *         {@code false} if the waiting time elapsed before the changes happened.
     */
    fun waitForActivity(): Boolean {
        return try {
            val result = activityStartedCountDownLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            result
        } catch (e: InterruptedException) {
            false
        }
    }

    companion object {
        private const val COUNT_DOWN_LATCH_COUNT = 1
        private const val TIMEOUT_IN_SECONDS = 3L
    }
}