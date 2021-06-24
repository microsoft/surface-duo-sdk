/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.screenmanager.tests.utils.SampleActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4ClassRunner::class)
class ScreenInfoProviderTest {
    @Rule
    @JvmField
    var rule: ActivityTestRule<SampleActivity> = ActivityTestRule(SampleActivity::class.java, false, false)

    @Test
    fun getInstance() {
        rule.launchActivity(null)
        getInstrumentation().waitForIdleSync()

        val screenInfo1 = ScreenInfoProvider.getScreenInfo(rule.activity)
        val screenInfo2 = ScreenInfoProvider.getScreenInfo(rule.activity)

        assertThat(screenInfo1).isNotNull()
        assertThat(screenInfo2).isNotNull()
        assertThat(screenInfo1).isNotEqualTo(screenInfo2)
    }
}