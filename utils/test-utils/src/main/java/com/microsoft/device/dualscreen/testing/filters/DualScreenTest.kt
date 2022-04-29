/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.filters

import org.junit.Test

/**
 * Methods annotated with @[Test] that are also annotated with @DualScreenTest,
 * will span the app to the entire display area
 * and will rotate the device or emulator depending on the given orientation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DualScreenTest(
    /**
     * The requested device orientation.
     */
    val orientation: Int = Int.MIN_VALUE
)
