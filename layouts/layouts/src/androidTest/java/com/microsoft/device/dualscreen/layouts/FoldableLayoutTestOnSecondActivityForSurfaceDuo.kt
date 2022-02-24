/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutTestOnSecondActivity
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate
import com.microsoft.device.dualscreen.testing.ForceClick
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.spanFromStart
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FoldableLayoutTestOnSecondActivityForSurfaceDuo {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(FoldableLayoutTestOnSecondActivity::class.java)

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()
    private val currentActivityDelegate = CurrentActivityDelegate()

    @Before
    fun before() {
        uiDevice.resetOrientation()

        currentActivityDelegate.setup(activityScenarioRule)
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.reset()
        currentActivityDelegate.clear(activityScenarioRule)
    }

    @Test
    fun testLayoutSingleScreen() {
        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(click())
        currentActivityDelegate.waitForActivity()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutSingleScreenLandscape() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        uiDevice.setOrientationRight()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(ForceClick())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        uiDevice.spanFromStart()

        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(click())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_portrait_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        uiDevice.spanFromStart()
        uiDevice.setOrientationRight()

        currentActivityDelegate.resetActivityCounter()
        onView(withId(R.id.start)).perform(ForceClick())
        currentActivityDelegate.waitForActivity()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        windowLayoutInfoConsumer.reset()
        windowLayoutInfoConsumer.register(currentActivityDelegate.currentActivity!!)
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_landscape_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
    }
}