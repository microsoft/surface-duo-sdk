/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class ExampleUnitTest {
    @Test
    fun testPenInfoCreate() {
        val x = 200.toFloat()
        val y = 200.toFloat()
        val pressure = 0.5.toFloat()
        val tilt = 0.5.toFloat()
        val orientation = 10.toFloat()

        val motionEvent = Mockito.mock(MotionEvent::class.java)
        Mockito.`when`(motionEvent.x).thenReturn(x)
        Mockito.`when`(motionEvent.y).thenReturn(y)
        Mockito.`when`(motionEvent.getToolType(0)).thenReturn(MotionEvent.TOOL_TYPE_MOUSE)
        Mockito.`when`(motionEvent.pressure).thenReturn(pressure)
        Mockito.`when`(motionEvent.getAxisValue(MotionEvent.AXIS_TILT)).thenReturn(tilt)
        Mockito.`when`(motionEvent.orientation).thenReturn(orientation)
        Mockito.`when`(motionEvent.buttonState).thenReturn(MotionEvent.BUTTON_PRIMARY)

        val penInfo = InputManager.PenInfo.createFromEvent(motionEvent)
        assertEquals(x, penInfo.x)
        assertEquals(y, penInfo.y)
        assertEquals(tilt, penInfo.tilt)
        assertEquals(orientation, penInfo.orientation)
        assertEquals(InputManager.PointerType.MOUSE, penInfo.pointerType)
        assertEquals(pressure, penInfo.pressure)
        assertEquals(true, penInfo.primaryButtonState)
        assertEquals(false, penInfo.secondaryButtonState)
    }
}