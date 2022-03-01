/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FrameLayoutActivity
import com.microsoft.device.dualscreen.layouts.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.layouts.utils.isFrameLayoutOnScreen
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FrameLayoutTestForSurfaceDuo {

    @get:Rule
    val activityTestRule = ActivityScenarioRule(FrameLayoutActivity::class.java)

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @Before
    fun before() {
        uiDevice.resetOrientation()

        activityTestRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.reset()
    }

    @Test
    fun testDisplayPositionFromLayout() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionEnd() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.END))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testDisplayPositionDual() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.DUAL))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.START))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.START)))
    }
}
