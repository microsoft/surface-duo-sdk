/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutSingleScreenActivity
import com.microsoft.device.dualscreen.testing.SurfaceDuo1_dimens
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.isViewOnScreen
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.setOrientationLeft
import com.microsoft.device.dualscreen.testing.setOrientationRight
import com.microsoft.device.dualscreen.testing.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class FoldableLayoutSingleScreenTestForSurfaceDuo {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(FoldableLayoutSingleScreenActivity::class.java)
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
        switchFromSingleToDualScreen()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_portrait_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.START,
                    orientation = ORIENTATION_LANDSCAPE,
                    firstDisplayWith = SurfaceDuo1_dimens.SINGLE_SCREEN_WIDTH,
                    totalDisplayWith = SurfaceDuo1_dimens.DUAL_SCREEN_WIDTH,
                    foldingFeatureWidth = SurfaceDuo1_dimens.HINGE_WIDTH
                )
            )
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.END,
                    orientation = ORIENTATION_LANDSCAPE,
                    firstDisplayWith = SurfaceDuo1_dimens.SINGLE_SCREEN_WIDTH,
                    totalDisplayWith = SurfaceDuo1_dimens.DUAL_SCREEN_WIDTH,
                    foldingFeatureWidth = SurfaceDuo1_dimens.HINGE_WIDTH
                )
            )
        )
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        switchFromSingleToDualScreen()

        windowLayoutInfoConsumer.resetWindowInfoLayoutCounter()
        setOrientationLeft()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_landscape_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.START,
                    orientation = ORIENTATION_PORTRAIT,
                    firstDisplayWith = SurfaceDuo1_dimens.SINGLE_SCREEN_WIDTH,
                    totalDisplayWith = SurfaceDuo1_dimens.DUAL_SCREEN_WIDTH,
                    foldingFeatureWidth = SurfaceDuo1_dimens.HINGE_WIDTH
                )
            )
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(
                isViewOnScreen(
                    position = DisplayPosition.END,
                    orientation = ORIENTATION_PORTRAIT,
                    firstDisplayWith = SurfaceDuo1_dimens.SINGLE_SCREEN_WIDTH,
                    totalDisplayWith = SurfaceDuo1_dimens.DUAL_SCREEN_WIDTH,
                    foldingFeatureWidth = SurfaceDuo1_dimens.HINGE_WIDTH
                )
            )
        )
    }
}
