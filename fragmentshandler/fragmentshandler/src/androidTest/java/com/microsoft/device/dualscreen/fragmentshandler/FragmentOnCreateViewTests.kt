/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.fragmentshandler.utils.SampleActivity
import com.microsoft.device.dualscreen.fragmentshandler.utils.runAction
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.testing.unspanToStart
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FragmentOnCreateViewTests {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(SampleActivity::class.java)

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
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
        windowLayoutInfoConsumer.run {
            uiDevice.resetOrientation()
        }

        FragmentManagerStateHandler.instance?.clear()
        windowLayoutInfoConsumer.reset()
    }

    @Test
    fun testOnCreateView() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.singleScreenFragment.onCreateViewWasCalled).isTrue()
            assertThat(activity.dualScreenStartFragment.onCreateViewWasCalled).isFalse()
            assertThat(activity.dualScreenEndFragment.onCreateViewWasCalled).isFalse()

            activity.resetFragments()
        }

        windowLayoutInfoConsumer.run {
            uiDevice.spanFromStart()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.singleScreenFragment.onCreateViewWasCalled).isFalse()
            assertThat(activity.dualScreenStartFragment.onCreateViewWasCalled).isTrue()
            assertThat(activity.dualScreenEndFragment.onCreateViewWasCalled).isTrue()

            activity.resetFragments()
        }

        windowLayoutInfoConsumer.run {
            uiDevice.unspanToStart()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            assertThat(activity.singleScreenFragment.onCreateViewWasCalled).isTrue()
            assertThat(activity.dualScreenStartFragment.onCreateViewWasCalled).isFalse()
            assertThat(activity.dualScreenEndFragment.onCreateViewWasCalled).isFalse()
        }
    }

    @Test
    fun testOnCreateViewWithRotation270() {
        activityScenarioRule.scenario.onActivity { activity ->
            activity.resetFragments()
        }

        windowLayoutInfoConsumer.runAction {
            uiDevice.setOrientationRight()
        }

        testOnCreateView()
    }

    @Test
    fun testOnCreateViewWithRotation90() {
        windowLayoutInfoConsumer.runAction {
            uiDevice.setOrientationLeft()
        }

        testOnCreateView()
    }
}