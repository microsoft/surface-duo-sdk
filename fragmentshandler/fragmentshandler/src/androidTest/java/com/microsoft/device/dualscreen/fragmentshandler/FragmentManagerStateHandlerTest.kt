/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.os.Parcelable
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.fragmentshandler.utils.SampleActivity
import com.microsoft.device.dualscreen.fragmentshandler.utils.runAction
import com.microsoft.device.dualscreen.testing.SurfaceDuo1
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.setOrientationLeft
import com.microsoft.device.dualscreen.testing.setOrientationRight
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FragmentManagerStateHandlerTest {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(SampleActivity::class.java)
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @Before
    fun before() {
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }

        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.runAction {
            resetOrientation()
        }

        FragmentManagerStateHandler.instance?.clear()
        windowLayoutInfoConsumer.reset()
    }

    @Test
    fun testFragmentManagerStateHandler() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromDualToSingleScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }
    }

    @Test
    fun testFragmentManagerStateHandlerWithRotation270() {
        windowLayoutInfoConsumer.runAction {
            setOrientationRight()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromDualToSingleScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }
    }

    @Test
    fun testFragmentManagerStateHandlerWithRotation90() {
        windowLayoutInfoConsumer.runAction {
            setOrientationLeft()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromDualToSingleScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }

        windowLayoutInfoConsumer.runAction {
            SurfaceDuo1.switchFromSingleToDualScreen()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.lastSavedInstanceState).isNotNull()
            assertThat(activity.fragmentManagerState).isNotNull()
        }
    }
}

private val SampleActivity.fragmentManagerState: Parcelable?
    get() = lastSavedInstanceState?.getParcelable(FragmentManagerStateWrapper.BUNDLE_SAVED_STATE_REGISTRY_KEY)