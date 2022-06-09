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
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.MockFoldingFeature
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.getDeviceModel
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod

/**
 * A specialized [AndroidJUnit4ClassRunner] that can be used together with the
 * [SingleScreenTest], [DualScreenTest] and [TargetDevices] annotations
 * to filter the tests that will run on the specified devices.
 * For example, if a test method is annotated with @TargetDevice(device = DeviceModel.SurfaceDuo),
 * that test method will run only on SurfaceDuo device or emulator, otherwise will be skipped.
 */
class FoldableJUnit4ClassRunner : AndroidJUnit4ClassRunner {
    constructor(klass: Class<*>?) : super(klass)

    constructor(klass: Class<*>?, runnerParams: AndroidRunnerParams) : super(klass, runnerParams)

    override fun runChild(method: FrameworkMethod?, notifier: RunNotifier?) {
        val description = describeChild(method)
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val currentDeviceModel = uiDevice.getDeviceModel()
        val targetDevices: TargetDevices? = getAnnotation(description, TargetDevices::class.java)
        if (targetDevices == null ||
            currentDeviceIsOnTargetDevices(currentDeviceModel, targetDevices.devices) ||
            currentDeviceNotInIgnoredDevices(currentDeviceModel, targetDevices.ignoreDevices)
        ) {
            runLeaf(methodBlock(method), description, notifier)
        } else {
            notifier?.fireTestIgnored(description)
        }
    }

    private fun currentDeviceIsOnTargetDevices(currentDevice: DeviceModel, targetDevices: Array<DeviceModel>): Boolean =
        targetDevices.isNotEmpty() && targetDevices.any { it == currentDevice }

    private fun currentDeviceNotInIgnoredDevices(currentDevice: DeviceModel, ignoreDevices: Array<DeviceModel>): Boolean =
        ignoreDevices.isNotEmpty() && !ignoreDevices.any { it == currentDevice }

    override fun validateTestMethods(errors: MutableList<Throwable>?) {
        super.validateTestMethods(errors)
        val methods = testClass.getAnnotatedMethods(Test::class.java)
        methods.forEach { method ->
            method.validateFoldableTestAnnotations(errors)
            method.validateTargetAnnotation(errors)
            method.validateMockFoldingFeatureAnnotation(errors)
        }
    }

    private fun FrameworkMethod.validateFoldableTestAnnotations(errors: MutableList<Throwable>?) {
        val singleScreenTestAnnotation = method.getAnnotation(SingleScreenTest::class.java)
        val dualScreenTestAnnotation = method.getAnnotation(DualScreenTest::class.java)
        if (singleScreenTestAnnotation != null && dualScreenTestAnnotation != null) {
            errors?.add(
                Exception(
                    "Method " + method.name + " should be annotated with only " +
                        "@${SingleScreenTest::class.java.simpleName} or @${DualScreenTest::class.java.simpleName}."
                )
            )
        }
    }

    private fun FrameworkMethod.validateTargetAnnotation(errors: MutableList<Throwable>?) {
        method.getAnnotation(TargetDevices::class.java)?.let { targetDevicesAnnotation ->
            if (targetDevicesAnnotation.devices.isNotEmpty() && targetDevicesAnnotation.ignoreDevices.isNotEmpty()) {
                errors?.add(
                    Exception(
                        "Method " + method.name + ": You cannot have both @TargetDevices.devices and @TargetDevices.ignoreDevices non empty arrays."
                    )
                )
            }
        }
    }

    private fun FrameworkMethod.validateMockFoldingFeatureAnnotation(errors: MutableList<Throwable>?) {
        method.getAnnotation(MockFoldingFeature::class.java)?.let {
            if (it.windowBounds.size != 4) {
                errors?.add(
                    Exception(
                        "Method " + method.name + ": @MockFoldingFeature.windowBounds should be an array with four coordinates " +
                            "in he following order: [left, top, right, bottom]"
                    )
                )
            }
        }
    }

    private fun <T : Annotation> getAnnotation(description: Description, annotationClass: Class<T>): T? {
        return description.testClass.getAnnotation(annotationClass)
            ?: description.getAnnotation(annotationClass)
    }
}