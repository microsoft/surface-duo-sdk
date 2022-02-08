/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutDualScreenActivity
import com.microsoft.device.dualscreen.testing.SurfaceDuo1
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.setOrientationRight
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class FoldableLayoutDualScreenTestForSurfaceDuo {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(FoldableLayoutDualScreenActivity::class.java)
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @Before
    fun before() {
        resetOrientation()

        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun after() {
        windowLayoutInfoConsumer.reset()
    }

    @Test
    fun testLayoutSingleScreen() {
        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutSingleScreenLandscape() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        setOrientationRight()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        SurfaceDuo1.switchFromSingleToDualScreen()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_portrait)))
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        SurfaceDuo1.switchFromSingleToDualScreen()

        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        setOrientationRight()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_landscape)))
    }
}
