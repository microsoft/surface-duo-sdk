/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.res.Configuration
import android.view.Surface
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.screenmanager.tests.utils.SampleActivity
import com.microsoft.device.dualscreen.test.utils.DUAL_SCREEN_HINGE_RECT
import com.microsoft.device.dualscreen.test.utils.DUAL_SCREEN_WINDOW_RECT
import com.microsoft.device.dualscreen.test.utils.END_SCREEN_RECT
import com.microsoft.device.dualscreen.test.utils.START_SCREEN_RECT
import com.microsoft.device.dualscreen.test.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.test.utils.any
import com.microsoft.device.dualscreen.test.utils.setOrientationLeft
import com.microsoft.device.dualscreen.test.utils.setOrientationRight
import com.microsoft.device.dualscreen.test.utils.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.test.utils.unfreezeRotation
import org.junit.After
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenManagerOnDualScreenTest {
    @Rule
    @JvmField
    var rule: ActivityTestRule<SampleActivity> = ActivityTestRule(SampleActivity::class.java, false, false)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @After
    fun tearDown() {
        ScreenManagerProvider.getScreenManager().clear()
        screenInfoListener.resetScreenInfo()
    }

    @Test
    fun testScreenInfo() {
        assertThat(screenInfoListener.screenInfo).isNull()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)

        rule.launchActivity(null)
        getInstrumentation().waitForIdleSync()
        switchFromSingleToDualScreen()

        assertThat(screenInfoListener.screenInfo).isNotNull()
        assertThat(screenInfoListener.screenInfo?.isSurfaceDuoDevice()).isTrue()
        assertThat(screenInfoListener.screenInfo?.isDualMode()).isTrue()
        assertThat(screenInfoListener.screenInfo?.getHinge()).isEqualTo(DUAL_SCREEN_HINGE_RECT)

        val screenRectangles = screenInfoListener.screenInfo?.getScreenRectangles()
        assertThat(screenRectangles?.size).isEqualTo(2)
        assertThat(screenRectangles?.get(0)).isEqualTo(START_SCREEN_RECT)
        assertThat(screenRectangles?.get(1)).isEqualTo(END_SCREEN_RECT)

        assertThat(screenInfoListener.screenInfo?.getWindowRect()).isEqualTo(DUAL_SCREEN_WINDOW_RECT)
    }

    @Test
    fun testScreenRotation() {
        assertThat(screenInfoListener.screenInfo).isNull()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)

        rule.launchActivity(null)
        getInstrumentation().waitForIdleSync()
        switchFromSingleToDualScreen()

        screenInfoListener.waitForScreenInfoChanges()
        assertThat(screenInfoListener.screenInfo).isNotNull()
        assertThat(screenInfoListener.screenInfo?.getScreenRotation()).isEqualTo(Surface.ROTATION_0)
        screenInfoListener.resetScreenInfoCounter()

        setOrientationLeft()
        screenInfoListener.waitForScreenInfoChanges()
        assertThat(screenInfoListener.screenInfo?.getScreenRotation()).isEqualTo(Surface.ROTATION_90)
        screenInfoListener.resetScreenInfoCounter()

        setOrientationRight()
        screenInfoListener.waitForScreenInfoChanges()
        assertThat(screenInfoListener.screenInfo?.getScreenRotation()).isEqualTo(Surface.ROTATION_270)
        screenInfoListener.resetScreenInfoCounter()

        unfreezeRotation()
    }

    @Test
    fun testOnConfigurationChanged() {
        assertThat(screenInfoListener.screenInfo).isNull()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)

        rule.launchActivity(null)
        getInstrumentation().waitForIdleSync()
        switchFromSingleToDualScreen()
        rule.runOnUiThread {
            rule.activity.onConfigurationChanged(Configuration())
        }

        assertThat(screenInfoListener.screenInfo).isNotNull()
    }

    @Test
    fun testMultipleListeners() {
        val screenManager = ScreenManagerProvider.getScreenManager()

        val screenInfo1 = Mockito.mock(ScreenInfoListener::class.java)
        doNothing().`when`(screenInfo1).onScreenInfoChanged(any())
        screenManager.addScreenInfoListener(screenInfo1)

        val screenInfo2 = Mockito.mock(ScreenInfoListener::class.java)
        doNothing().`when`(screenInfo2).onScreenInfoChanged(any())
        screenManager.addScreenInfoListener(screenInfo2)

        val screenInfo3 = Mockito.mock(ScreenInfoListener::class.java)
        doNothing().`when`(screenInfo3).onScreenInfoChanged(any())
        screenManager.addScreenInfoListener(screenInfo3)

        screenManager.addScreenInfoListener(screenInfoListener)

        rule.launchActivity(null)
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        // Activity is started, then is moved to dual screen, then it's recreated
        verify(screenInfo1, times(2)).onScreenInfoChanged(any())
        verify(screenInfo2, times(2)).onScreenInfoChanged(any())
        verify(screenInfo3, times(2)).onScreenInfoChanged(any())

        screenManager.removeScreenInfoListener(screenInfo1)
        screenManager.removeScreenInfoListener(screenInfo2)
        screenManager.removeScreenInfoListener(screenInfo3)
    }
}