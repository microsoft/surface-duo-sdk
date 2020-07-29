/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import org.junit.Assert.assertEquals
import org.junit.Test

class HingeColorTest {

    @Test
    fun `test from id`() {
        assertEquals(HingeColor.fromId(0), HingeColor.BLACK)
        assertEquals(HingeColor.fromId(1), HingeColor.WHITE)
        assertEquals(HingeColor.fromId(1), HingeColor.WHITE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test from illegal id`() {
        HingeColor.fromId(-1)
    }
}