/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.screenmanager.tests.R
import com.microsoft.device.dualscreen.screenmanager.tests.utils.SimpleFirstActivity
import com.microsoft.device.dualscreen.test.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.test.utils.forceClick
import com.microsoft.device.dualscreen.test.utils.resetOrientation
import com.microsoft.device.dualscreen.test.utils.setOrientationLeft
import com.microsoft.device.dualscreen.test.utils.setOrientationNatural
import com.microsoft.device.dualscreen.test.utils.switchFromSingleToDualScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class ScreenInfoOnActivityRotatedTest {
    @get:Rule
    val activityTestRule = ActivityTestRule(SimpleFirstActivity::class.java)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun before() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        resetOrientation()
    }

    @After
    fun after() {
        ScreenManagerProvider.getScreenManager().clear()
    }

    @Test
    fun testLastKnownScreenInfoOnSingleScreen() {
        val firstScreenInfo = screenInfoListener.screenInfo
        assertThat(firstScreenInfo).isNotNull()
        assertThat(firstScreenInfo?.isDualMode()).isFalse()

        setOrientationLeft()

        val secondScreenInfo = screenInfoListener.screenInfo
        assertThat(secondScreenInfo).isNotNull()
        assertThat(secondScreenInfo?.isDualMode()).isFalse()

        onView(withId(R.id.start_button)).perform(forceClick())
        onView(withId(R.id.hello_text)).check(matches(isDisplayed()))

        val thirdScreenInfo = screenInfoListener.screenInfo
        assertThat(thirdScreenInfo).isNotNull()
        assertThat(thirdScreenInfo?.isDualMode()).isFalse()
        assertThat(thirdScreenInfo).isNotEqualTo(secondScreenInfo)

        setOrientationNatural()

        val forthScreenInfo = screenInfoListener.screenInfo
        assertThat(forthScreenInfo).isNotNull()
        assertThat(forthScreenInfo?.isDualMode()).isFalse()
        assertThat(forthScreenInfo).isNotEqualTo(thirdScreenInfo)
    }

    @Test
    fun testLastKnownScreenInfoOnDualScreen() {
        val firstScreenInfo = screenInfoListener.screenInfo
        assertThat(firstScreenInfo).isNotNull()
        assertThat(firstScreenInfo?.isDualMode()).isFalse()

        switchFromSingleToDualScreen()

        val secondScreenInfo = screenInfoListener.screenInfo
        assertThat(secondScreenInfo).isNotNull()
        assertThat(secondScreenInfo?.isDualMode()).isTrue()
        assertThat(secondScreenInfo).isNotEqualTo(firstScreenInfo)

        setOrientationLeft()

        onView(withId(R.id.start_button)).perform(forceClick())
        onView(withId(R.id.hello_text)).check(matches(isDisplayed()))

        val thirdScreenInfo = screenInfoListener.screenInfo
        assertThat(thirdScreenInfo).isNotNull()
        assertThat(thirdScreenInfo?.isDualMode()).isTrue()
        assertThat(thirdScreenInfo).isNotEqualTo(secondScreenInfo)

        pressBack()
        onView(withId(R.id.start_button)).check(matches(isDisplayed()))

        val fourthScreenInfo = screenInfoListener.screenInfo
        assertThat(fourthScreenInfo).isNotNull()
        assertThat(fourthScreenInfo?.isDualMode()).isTrue()
        assertThat(fourthScreenInfo).isNotEqualTo(thirdScreenInfo)

        setOrientationNatural()

        val fifthScreenInfo = screenInfoListener.screenInfo
        assertThat(fifthScreenInfo).isNotNull()
        assertThat(fifthScreenInfo?.isDualMode()).isTrue()
        assertThat(fifthScreenInfo).isNotEqualTo(fourthScreenInfo)
    }
}