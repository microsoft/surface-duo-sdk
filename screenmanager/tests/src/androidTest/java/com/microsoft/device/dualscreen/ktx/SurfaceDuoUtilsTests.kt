/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.ktx

import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.SampleActivity
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.isSurfaceDuoInDualMode
import com.microsoft.device.dualscreen.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.utils.switchFromSingleToDualScreen
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SurfaceDuoUtilsTests {
    @Rule
    @JvmField
    var rule: ActivityTestRule<SampleActivity> = ActivityTestRule(SampleActivity::class.java, false, false)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @After
    fun tearDown() {
        val screenManager = ScreenManagerProvider.getScreenManager()
        screenManager.removeScreenInfoListener(screenInfoListener)
        screenInfoListener.resetScreenInfo()
    }

    @Test
    fun isSurfaceDuoInDualModeTest() {
        assertThat(screenInfoListener.screenInfo).isNull()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)

        rule.launchActivity(null)
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(screenInfoListener.screenInfo).isNotNull()
        assertThat(isSurfaceDuoInDualMode(screenInfoListener.screenInfo!!)).isFalse()
        screenInfoListener.resetScreenInfoCounter()
        screenInfoListener.resetScreenInfo()

        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(screenInfoListener.screenInfo).isNotNull()
        assertThat(isSurfaceDuoInDualMode(screenInfoListener.screenInfo!!)).isTrue()
        screenInfoListener.resetScreenInfoCounter()
        screenInfoListener.resetScreenInfo()
    }
}