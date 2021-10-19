/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import com.microsoft.device.dualscreen.recyclerview.activities.SimpleRecyclerViewActivity
import com.microsoft.device.dualscreen.recyclerview.test.R
import com.microsoft.device.dualscreen.recyclerview.utils.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.recyclerview.utils.areItemsDisplayed
import com.microsoft.device.dualscreen.utils.test.resetOrientation
import com.microsoft.device.dualscreen.utils.test.setOrientationLeft
import com.microsoft.device.dualscreen.utils.test.setOrientationRight
import com.microsoft.device.dualscreen.utils.test.switchFromDualToSingleScreen
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.utils.test.unfreezeRotation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoRecyclerViewTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleRecyclerViewActivity::class.java)

    private var windowLayoutInfoConsumerLatch = WindowLayoutInfoConsumer()
    private var adapter: WindowInfoRepositoryCallbackAdapter? = null
    private val runOnUiThreadExecutor = Executor { command: Runnable? ->
        command?.let {
            Handler(Looper.getMainLooper()).post(it)
        }
    }

    private fun resetAdapterAndLatch() {
        windowLayoutInfoConsumerLatch.resetWindowInfoLayoutCounter()
        adapter = WindowInfoRepositoryCallbackAdapter(activityTestRule.activity.windowInfoRepository())
        adapter?.addWindowLayoutInfoListener(runOnUiThreadExecutor, windowLayoutInfoConsumerLatch)
        windowLayoutInfoConsumerLatch.waitForWindowInfoLayoutChanges()
    }

    @Before
    fun before() {
        resetAdapterAndLatch()
    }

    @After
    fun after() {
        windowLayoutInfoConsumerLatch.resetWindowInfoLayoutCounter()
        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)
        resetOrientation()
    }

    @Test
    fun testSpanMode() {
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testRotationLeft() {
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        setOrientationLeft()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testRotationRight() {
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        setOrientationRight()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testMultipleRotations() {
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        setOrientationRight()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        setOrientationLeft()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        setOrientationRight()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        unfreezeRotation()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }

    @Test
    fun testMultipleSpanning() {
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromDualToSingleScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromSingleToDualScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))

        adapter?.removeWindowLayoutInfoListener(windowLayoutInfoConsumerLatch)

        switchFromDualToSingleScreen()
        resetAdapterAndLatch()
        onView(withId(R.id.recyclerView)).check(matches(areItemsDisplayed()))
    }
}