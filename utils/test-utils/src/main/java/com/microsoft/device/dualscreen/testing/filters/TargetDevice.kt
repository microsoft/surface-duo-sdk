/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.filters

import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner

/**
 * Test classes or test methods annotated with @TargetDevice will run only on the specified device.
 * For example, if one test method is annotated with @TargetDevice(device = DeviceModel.SurfaceDuo),
 * then that test will run only on SurfaceDuo devices and emulators, otherwise will be skipped.
 *
 * This annotation works only together with [FoldableJUnit4ClassRunner].
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TargetDevice(
    /**
     * The requested device.
     */
    val device: DeviceModel
)
