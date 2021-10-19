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
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FoldableLayoutDualScreenActivity
import com.microsoft.device.dualscreen.utils.test.resetOrientation
import com.microsoft.device.dualscreen.utils.test.setOrientationRight
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class FoldableLayoutDualScreenTestForSurfaceDuo {

    @get:Rule
    val activityTestRule = ActivityTestRule(FoldableLayoutDualScreenActivity::class.java)

    @Before
    fun before() {
        resetOrientation()
    }

    @Test
    fun testLayoutSingleScreen() {
        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutSingleScreenLandscape() {
        setOrientationRight()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        switchFromSingleToDualScreen()

        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_portrait)))
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        switchFromSingleToDualScreen()

        setOrientationRight()

        onView(withId(R.id.textViewDual)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDual)).check(matches(withText(R.string.dual_landscape)))
    }
}
