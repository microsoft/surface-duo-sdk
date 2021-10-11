/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class PenInfoTest {
    private lateinit var motionEvent: MotionEvent
    private val x = 200.toFloat()
    private val y = 200.toFloat()
    private val pressure = 0.5.toFloat()
    private val tilt = 0.5.toFloat()
    private val orientation = 10.toFloat()
    private val stroke = InputManager.ExtendedStroke()

    @Before
    fun setup() {
        motionEvent = mock(MotionEvent::class.java)
        org.mockito.Mockito.`when`(motionEvent.x).thenReturn(x)
        org.mockito.Mockito.`when`(motionEvent.y).thenReturn(y)
        org.mockito.Mockito.`when`(motionEvent.getToolType(0)).thenReturn(MotionEvent.TOOL_TYPE_MOUSE)
        org.mockito.Mockito.`when`(motionEvent.pressure).thenReturn(pressure)
        org.mockito.Mockito.`when`(motionEvent.getAxisValue(MotionEvent.AXIS_TILT)).thenReturn(tilt)
        org.mockito.Mockito.`when`(motionEvent.orientation).thenReturn(orientation)
        org.mockito.Mockito.`when`(motionEvent.buttonState).thenReturn(MotionEvent.BUTTON_PRIMARY)
    }

    @Test
    fun testPenInfoCreate() {
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

    @Test
    fun testAddPoint() {
        addPointToStroke()

        val points = stroke.getPoints()
        assertEquals(1, points.count())

        val point = points.first()
        assertEquals(x, point.x)
        assertEquals(y, point.y)

        val penInfo = stroke.getPenInfo(point)
        assertNotNull(penInfo)
        assertEquals(x, penInfo?.x)
        assertEquals(y, penInfo?.y)
    }

    @Test
    fun testReset() {
        addPointToStroke()
        stroke.reset()

        val points = stroke.getPoints()
        assertEquals(0, points.count())

        val point = InputManager.Point(x, y)
        val penInfo = stroke.getPenInfo(point)
        assertNull(penInfo)
    }

    private fun addPointToStroke() {
        val originalPenInfo = InputManager.PenInfo.createFromEvent(motionEvent)
        stroke.addPoint(originalPenInfo)
    }
}
