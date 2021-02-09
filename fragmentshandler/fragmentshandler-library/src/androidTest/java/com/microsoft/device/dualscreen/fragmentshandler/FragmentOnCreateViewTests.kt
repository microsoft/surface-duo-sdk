/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.fragmentshandler.utils.SampleActivity
import com.microsoft.device.dualscreen.fragmentshandler.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.fragmentshandler.utils.setOrientationLeft
import com.microsoft.device.dualscreen.fragmentshandler.utils.setOrientationRight
import com.microsoft.device.dualscreen.fragmentshandler.utils.switchFromDualToSingleScreen
import com.microsoft.device.dualscreen.fragmentshandler.utils.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.fragmentshandler.utils.unfreezeRotation
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FragmentOnCreateViewTests {
    @Rule
    @JvmField
    var rule: ActivityTestRule<SampleActivity> = ActivityTestRule(SampleActivity::class.java, false, false)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun before() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        rule.launchActivity(null)
    }

    @After
    fun after() {
        unfreezeRotation()
        ScreenManagerProvider.getScreenManager().clear()
        screenInfoListener.resetScreenInfo()
        screenInfoListener.resetScreenInfoCounter()
        rule.finishActivity()
        FragmentManagerStateHandler.instance?.clear()
    }

    @Test
    fun testOnCreateView() {
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.activity.singleScreenFragment.onCreateViewWasCalled).isTrue()
        assertThat(rule.activity.dualScreenStartFragment.onCreateViewWasCalled).isFalse()
        assertThat(rule.activity.dualScreenEndFragment.onCreateViewWasCalled).isFalse()

        rule.activity.resetFragments()
        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.activity.singleScreenFragment.onCreateViewWasCalled).isFalse()
        assertThat(rule.activity.dualScreenStartFragment.onCreateViewWasCalled).isTrue()
        assertThat(rule.activity.dualScreenEndFragment.onCreateViewWasCalled).isTrue()

        rule.activity.resetFragments()
        screenInfoListener.resetScreenInfoCounter()
        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.activity.singleScreenFragment.onCreateViewWasCalled).isTrue()
        assertThat(rule.activity.dualScreenStartFragment.onCreateViewWasCalled).isFalse()
        assertThat(rule.activity.dualScreenEndFragment.onCreateViewWasCalled).isFalse()
    }

    @Test
    fun testOnCreateViewWithRotation270() {
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        rule.activity.resetFragments()
        setOrientationRight()
        testOnCreateView()
    }

    @Test
    fun testOnCreateViewWithRotation90() {
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()
        setOrientationLeft()
        testOnCreateView()
    }
}