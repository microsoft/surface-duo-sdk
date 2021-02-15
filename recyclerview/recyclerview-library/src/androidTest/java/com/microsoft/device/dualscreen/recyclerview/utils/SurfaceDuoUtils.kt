/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

const val SCREEN_WIDTH = 1350
const val HINGE_WIDTH = 84
const val SCREEN_COUNT = 2
const val TOTAL_WIDTH = SCREEN_WIDTH * SCREEN_COUNT + HINGE_WIDTH

fun spanApplication() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.swipe(675, 1780, 1350, 900, 400)
}

fun unSpanApplicationToStart() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.swipe(1300, 1780, 675, 900, 400)
}

fun unSpanApplicationToEnd() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.swipe(1300, 1780, 1700, 900, 400)
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
 * Simulates orienting the device to the right and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationRight() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationRight()
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

fun resetOrientation() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationNatural()
    device.unfreezeRotation()
}
