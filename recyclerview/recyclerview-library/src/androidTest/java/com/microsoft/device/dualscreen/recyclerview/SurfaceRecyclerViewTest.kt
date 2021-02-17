/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.recyclerview.activities.SimpleRecyclerViewActivity
import com.microsoft.device.dualscreen.recyclerview.test.R
import com.microsoft.device.dualscreen.recyclerview.utils.areItemsDisplayed
import com.microsoft.device.dualscreen.test.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.test.utils.resetOrientation
import com.microsoft.device.dualscreen.test.utils.setOrientationLeft
import com.microsoft.device.dualscreen.test.utils.setOrientationRight
import com.microsoft.device.dualscreen.test.utils.switchFromDualToSingleScreen
import com.microsoft.device.dualscreen.test.utils.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.test.utils.unfreezeRotation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceRecyclerViewTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleRecyclerViewActivity::class.java)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun before() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
    }

    @After
    fun after() {
        screenInfoListener.resetScreenInfoCounter()
        ScreenManagerProvider.getScreenManager().clear()
        resetOrientation()
    }

    @Test
    fun testSpanMode() {
        switchFromSingleToDualScreen()

        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testRotationLeft() {
        switchFromSingleToDualScreen()

        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        setOrientationLeft()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testRotationRight() {
        switchFromSingleToDualScreen()

        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        setOrientationRight()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testMultipleRotations() {
        switchFromSingleToDualScreen()

        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        setOrientationRight()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        setOrientationLeft()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        setOrientationRight()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        unfreezeRotation()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testMultipleSpanning() {
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }
}