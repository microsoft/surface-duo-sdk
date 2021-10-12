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
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.SimpleStartActivity
import com.microsoft.device.dualscreen.utils.test.resetOrientation
import com.microsoft.device.dualscreen.utils.test.setOrientationRight
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ONE_SEC = 1000L

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FoldableLayoutTestOnSecondActivityForSurfaceDuo {
    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleStartActivity::class.java)

    @Before
    fun before() {
        resetOrientation()
    }

    @Test
    fun testLayoutSingleScreen() {
        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutSingleScreenLandscape() {
        setOrientationRight()
        Thread.sleep(ONE_SEC)

        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_portrait_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        switchFromSingleToDualScreen()
        setOrientationRight()

        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_landscape_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
    }
}