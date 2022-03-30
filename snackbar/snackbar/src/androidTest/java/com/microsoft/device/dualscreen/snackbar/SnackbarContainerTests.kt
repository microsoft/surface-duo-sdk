/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.microsoft.device.dualscreen.snackbar.test.R
import com.microsoft.device.dualscreen.snackbar.utils.SampleActivity
import com.microsoft.device.dualscreen.snackbar.utils.hasMargins
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class SnackbarContainerTests {
    @get:Rule
    val activityScenarioRule = activityScenarioRule<SampleActivity>()
    protected val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    protected val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @Before
    fun setup() {
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun clear() {
        windowLayoutInfoConsumer.unregister()
    }

    protected fun testMargins(params: TestParams) {
        activityScenarioRule.scenario.onActivity {
            it.showSnackbar(it.getString(params.snackbarMessageResId), params.snackbarPosition, LENGTH_INDEFINITE)
        }
        onView(withId(R.id.snackbar_container))
            .check(matches(hasMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin)))
    }

    data class TestParams(
        val snackbarMessageResId: Int,
        val snackbarPosition: SnackbarPosition,
        val leftMargin: Int,
        val topMargin: Int,
        val rightMargin: Int,
        val bottomMargin: Int
    )
}