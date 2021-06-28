/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.screenmanager.tests.utils.SampleApplication
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenManagerProviderTest {
    @Test(expected = IllegalStateException::class)
    fun getInstanceWithError() {
        ScreenManagerProvider.getScreenManager()
    }

    @Test
    fun getInstanceWithSuccess() {
        ScreenManagerProvider.init(SampleApplication())
        val screenManager = ScreenManagerProvider.getScreenManager()
        assertThat(screenManager).isNotNull()
    }
}