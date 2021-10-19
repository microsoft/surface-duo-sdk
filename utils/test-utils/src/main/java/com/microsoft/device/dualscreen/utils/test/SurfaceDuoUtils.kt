/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.test

import android.graphics.Rect
import android.view.Surface
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

val START_SCREEN_RECT = Rect(0, 0, 1350, 1800)
val END_SCREEN_RECT = Rect(1434, 0, 2784, 1800)

val SINGLE_SCREEN_WINDOW_RECT = START_SCREEN_RECT
val DUAL_SCREEN_WINDOW_RECT = Rect(0, 0, 2784, 1800)

val SINGLE_SCREEN_HINGE_RECT = Rect()
val DUAL_SCREEN_HINGE_RECT = Rect(1350, 0, 1434, 1800)

const val SINGLE_SCREEN_WIDTH = 1350
const val HINGE_WIDTH = 84
const val SCREEN_COUNT = 2
const val DUAL_SCREEN_WIDTH = SINGLE_SCREEN_WIDTH * SCREEN_COUNT + HINGE_WIDTH

/**
 * Utility function that switches the  application from single screen mode to dual screen on Surface Duo devices.
 */
fun switchFromSingleToDualScreen() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    when (device.displayRotation) {
        Surface.ROTATION_0 -> device.swipe(675, 1780, 1350, 900, 400)
        Surface.ROTATION_270 -> device.swipe(1780, 675, 900, 1350, 400)
        Surface.ROTATION_90 -> device.swipe(1780, 2109, 900, 1400, 400)
    }
}

/**
 * Utility function that switches the  application from dual screen mode to single screen on Surface Duo devices.
 */
fun switchFromDualToSingleScreen() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    when (device.displayRotation) {
        Surface.ROTATION_0 -> device.swipe(1500, 1780, 650, 900, 400)
        Surface.ROTATION_270 -> device.swipe(1780, 1500, 900, 650, 400)
        Surface.ROTATION_90 -> device.swipe(1780, 1250, 900, 1500, 400)
    }
}