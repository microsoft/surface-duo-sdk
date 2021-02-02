/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.os.SystemClock
import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun testPenInfoCreate() {
    // MotionEvent parameters
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val action = MotionEvent.ACTION_DOWN
        val x = 200
        val y = 200
        val metaState = 0
        val pressure = 0.5.toFloat()
        val size = 0.5.toFloat()
        val xPrecision = 0.5.toFloat()
        val yPrecision = 0.5.toFloat()
        val deviceId = 0
        val edgeFlags = 0
        val motionEvent = MotionEvent.obtain(downTime, eventTime, action, x.toFloat(), y.toFloat(), pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags)
        val penInfo = InputManager.PenInfo.createFromEvent(motionEvent)
        assertEquals(x, penInfo.x)
        assertEquals(y, penInfo.y)
        assertEquals(InputManager.PointerType.UNKNOWN, penInfo.pointerType)
    }
}