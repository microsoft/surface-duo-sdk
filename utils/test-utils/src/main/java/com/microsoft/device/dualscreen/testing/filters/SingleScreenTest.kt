/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.filters

import org.junit.Test

/**
 * Methods annotated with @[Test] that are also annotated with @SingleScreenTest,
 * will keep the app visible only on the first display area
 * and will rotate the device or emulator depending on the given orientation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleScreenTest(
    val orientation: Int = Int.MIN_VALUE
)