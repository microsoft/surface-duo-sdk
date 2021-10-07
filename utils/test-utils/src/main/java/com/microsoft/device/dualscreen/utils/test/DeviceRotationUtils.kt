package com.microsoft.device.dualscreen.utils.test

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice


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