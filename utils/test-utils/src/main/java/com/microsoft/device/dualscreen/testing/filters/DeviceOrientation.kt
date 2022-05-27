/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.filters

/**
 * Test classes or test methods annotated with @DeviceOrientation,
 * will rotate the device or emulator depending on the given orientation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeviceOrientation(
    val orientation: Int = Int.MIN_VALUE
)
