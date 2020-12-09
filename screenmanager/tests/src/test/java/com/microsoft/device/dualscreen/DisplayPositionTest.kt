/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayPositionTest {
    @Test
    fun testValues() {
        assertThat(DisplayPosition.fromId(0)).isEqualTo(DisplayPosition.DUAL)
        assertThat(DisplayPosition.fromId(1)).isEqualTo(DisplayPosition.START)
        assertThat(DisplayPosition.fromId(2)).isEqualTo(DisplayPosition.END)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testException() {
        DisplayPosition.fromId(3)
    }
}