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
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.FrameLayoutActivity
import com.microsoft.device.dualscreen.layouts.utils.changeDisplayPosition
import com.microsoft.device.dualscreen.layouts.utils.isFrameLayoutOnScreen
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FrameLayoutTestForSurfaceDuo {

    @get:Rule
    val activityTestRule = ActivityTestRule(FrameLayoutActivity::class.java)

    @Test
    fun testDisplayPositionFromLayout() {
        switchFromSingleToDualScreen()
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionEnd() {
        switchFromSingleToDualScreen()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.END))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.END)))
    }

    @Test
    fun testDisplayPositionDual() {
        switchFromSingleToDualScreen()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.DUAL))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.DUAL)))
    }

    @Test
    fun testDisplayPositionStart() {
        switchFromSingleToDualScreen()
        onView(withId(R.id.duo_wrapper))
            .perform(changeDisplayPosition(DisplayPosition.START))
        onView(withId(R.id.duo_wrapper))
            .check(matches(isFrameLayoutOnScreen(DisplayPosition.START)))
    }
}
