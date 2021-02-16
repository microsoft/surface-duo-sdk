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
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.SimpleStartActivity
import com.microsoft.device.dualscreen.test.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.test.utils.resetOrientation
import com.microsoft.device.dualscreen.test.utils.setOrientationRight
import com.microsoft.device.dualscreen.test.utils.switchFromSingleToDualScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class TestLayoutOnSecondActivity {
    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleStartActivity::class.java)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun before() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        resetOrientation()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
    }

    @After
    fun after() {
        ScreenManagerProvider.getScreenManager().clear()
        screenInfoListener.resetScreenInfoCounter()
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
        screenInfoListener.waitForScreenInfoChanges()

        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

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
        screenInfoListener.waitForScreenInfoChanges()

        onView(withId(R.id.start)).perform(click())

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(matches(withText(R.string.dual_landscape_start)))
        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
    }
}