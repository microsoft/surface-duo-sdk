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
import com.microsoft.device.dualscreen.recyclerview.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.recyclerview.utils.areItemsDisplayed
import com.microsoft.device.dualscreen.recyclerview.utils.resetOrientation
import com.microsoft.device.dualscreen.recyclerview.utils.setOrientationLeft
import com.microsoft.device.dualscreen.recyclerview.utils.setOrientationRight
import com.microsoft.device.dualscreen.recyclerview.utils.spanApplication
import com.microsoft.device.dualscreen.recyclerview.utils.unSpanApplicationToEnd
import com.microsoft.device.dualscreen.recyclerview.utils.unSpanApplicationToStart
import com.microsoft.device.dualscreen.recyclerview.utils.unfreezeRotation
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
        resetOrientation()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
    }

    @After
    fun after() {
        screenInfoListener.resetScreenInfoCounter()
        ScreenManagerProvider.getScreenManager().clear()
        unfreezeRotation()
    }

    @Test
    fun testSpanMode() {
        spanApplication()

        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testRotationLeft() {
        spanApplication()

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
        spanApplication()

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
        spanApplication()

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

        resetOrientation()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testMultipleSpanning() {
        spanApplication()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        unSpanApplicationToStart()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        spanApplication()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        unSpanApplicationToEnd()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }
}