/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.runner

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.internal.util.AndroidRunnerParams
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.testing.filters.TargetDevice
import com.microsoft.device.dualscreen.testing.isSurfaceDuo
import org.junit.runners.model.FrameworkMethod

/**
 * A specialized [AndroidJUnit4ClassRunner] that can be used together with the [TargetDevice] annotation
 * to filter the tests that will run on the specified devices.
 * For example, if a test method is annotated with @TargetDevice(device = DeviceModel.SurfaceDuo),
 * that test method will run only on SurfaceDuo device or emulator, otherwise will be skipped.
 */
class FoldableJUnit4ClassRunner : AndroidJUnit4ClassRunner {
    constructor(klass: Class<*>?) : super(klass)

    constructor(klass: Class<*>?, runnerParams: AndroidRunnerParams) : super(klass, runnerParams)

    override fun computeTestMethods(): MutableList<FrameworkMethod> {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val testMethods: MutableList<FrameworkMethod> = super.computeTestMethods()
        return testMethods.filter { method ->
            val targetDevice: TargetDevice? = method.getAnnotation(TargetDevice::class.java)
            (targetDevice == null) ||
                (!targetDevice.device.isSurfaceDuo && !uiDevice.isSurfaceDuo()) ||
                (targetDevice.device.isSurfaceDuo && uiDevice.isSurfaceDuo())
        }.toMutableList()
    }

    private val DeviceModel?.isSurfaceDuo: Boolean
        get() = this == DeviceModel.SurfaceDuo || this == DeviceModel.SurfaceDuo2
}