/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.layouts.test.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoFrameLayoutTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleDuoFrameLayoutActivity::class.java)

    @Test
    fun testDisplayPositionFromLayout() {
        spanApplication()
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testDisplayPositionEnd() {
        spanApplication()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.END))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testDisplayPositionDual() {
        spanApplication()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.DUAL))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        spanApplication()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.START))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.START)))
    }
}
