/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.filters

import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner

/**
 * Test classes or test methods annotated with @TargetDevice will run only on the specified device.
 *
 * For example, if one test method is annotated with @TargetDevice(devices = [[DeviceModel.SurfaceDuo]]),
 * then that test will run only on SurfaceDuo devices and emulators, otherwise will be skipped.
 *
 * You can ignore some devices using @TargetDevice(ignoreDevices = [[DeviceModel.SurfaceDuo]]).
 * This means that test will run on all devices except SurfaceDuo device or emulator.
 *
 * You cannot use these annotation with devices and ignoreDevices filled in the same time.
 *
 * This annotation works only together with [FoldableJUnit4ClassRunner].
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TargetDevices(
    /**
     * Devices that can be used to run the test.
     */
    val devices: Array<DeviceModel> = [],

    /**
     * When some tests will run on these devices, these tests will be skipped.
     */
    val ignoreDevices: Array<DeviceModel> = []
)
