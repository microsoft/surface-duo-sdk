package com.microsoft.device.dualscreen.layout

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