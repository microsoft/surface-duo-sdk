/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.samples.utils

import com.microsoft.device.dualscreen.layout.ScreenMode
import org.junit.Assert
import org.junit.Test

class ScreenModeTest {

    @Test
    fun `test from id`() {
        Assert.assertEquals(ScreenMode.fromId(0), ScreenMode.SINGLE_SCREEN)
        Assert.assertEquals(ScreenMode.fromId(1), ScreenMode.DUAL_SCREEN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test from illegal id`() {
        ScreenMode.fromId(-1)
    }

}