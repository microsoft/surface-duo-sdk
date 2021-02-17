/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.test.utils

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
 * Switches application from single screen mode to dual screen mode
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
 * Switches application from dual screen mode to single screen
 */
fun switchFromDualToSingleScreen() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    when (device.displayRotation) {
        Surface.ROTATION_0 -> device.swipe(1500, 1780, 650, 900, 400)
        Surface.ROTATION_270 -> device.swipe(1780, 1500, 900, 650, 400)
        Surface.ROTATION_90 -> device.swipe(1780, 1250, 900, 1500, 400)
    }
}

/**
 * Re-enables the sensors and un-freezes the device rotation allowing its contents
 * to rotate with the device physical rotation. During a test execution, it is best to
 * keep the device frozen in a specific orientation until the test case execution has completed.
 */
fun unfreezeRotation() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.unfreezeRotation()
}

/**
 * Simulates orienting the device to the left and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationLeft() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationLeft()
}

/**
 * Simulates orienting the device into its natural orientation and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationNatural() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationNatural()
}

/**
 * Simulates orienting the device to the right and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationRight() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationRight()
}

/**
 * Simulates orienting the device into its natural orientation,
 * re-enables the sensors and un-freezes the device rotation
 */
fun resetOrientation() {
    setOrientationNatural()
    unfreezeRotation()
}