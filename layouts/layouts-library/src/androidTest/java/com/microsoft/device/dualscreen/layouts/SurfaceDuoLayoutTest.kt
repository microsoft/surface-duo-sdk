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
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.layouts.test.R
import com.microsoft.device.dualscreen.layouts.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.layouts.utils.SimpleDuoLayoutActivity
import com.microsoft.device.dualscreen.layouts.utils.changeOrientation
import com.microsoft.device.dualscreen.layouts.utils.isViewOnScreen
import com.microsoft.device.dualscreen.layouts.utils.resetOrientation
import com.microsoft.device.dualscreen.layouts.utils.spanApplication
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoLayoutTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleDuoLayoutActivity::class.java)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun reset() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        resetOrientation()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
    }

    @After
    fun after() {
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(screenInfoListener)
        screenInfoListener.resetScreenInfoCounter()
    }

    @Test
    fun testLayoutSingleScreen() {
        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutSingleScreenLandscape() {
        changeOrientation()
        screenInfoListener.waitForScreenInfoChanges()

        onView(withId(R.id.textViewSingle)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSingle)).check(matches(withText(R.string.single_screen_mode)))
    }

    @Test
    fun testLayoutDualScreenLandscape() {
        spanApplication()
        screenInfoListener.waitForScreenInfoChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_portrait_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(isViewOnScreen(DisplayPosition.START, ORIENTATION_LANDSCAPE))
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_portrait_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(isViewOnScreen(DisplayPosition.END, ORIENTATION_LANDSCAPE))
        )
    }

    @Test
    fun testLayoutDualScreenPortrait() {
        spanApplication()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        changeOrientation()
        screenInfoListener.waitForScreenInfoChanges()

        onView(withId(R.id.textViewDualStart)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualStart)).check(
            matches(withText(R.string.dual_landscape_start))
        )
        onView(withId(R.id.textViewDualStart)).check(
            matches(isViewOnScreen(DisplayPosition.START, ORIENTATION_PORTRAIT))
        )

        onView(withId(R.id.textViewDualEnd)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDualEnd)).check(matches(withText(R.string.dual_landscape_end)))
        onView(withId(R.id.textViewDualEnd)).check(
            matches(isViewOnScreen(DisplayPosition.END, ORIENTATION_PORTRAIT))
        )
    }
}
