/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScreenModeTests {
    @Test
    fun testValues() {
        assertThat(ScreenMode.fromId(0)).isEqualTo(ScreenMode.SINGLE_SCREEN)
        assertThat(ScreenMode.fromId(1)).isEqualTo(ScreenMode.DUAL_SCREEN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testException() {
        ScreenMode.fromId(2)
    }
}