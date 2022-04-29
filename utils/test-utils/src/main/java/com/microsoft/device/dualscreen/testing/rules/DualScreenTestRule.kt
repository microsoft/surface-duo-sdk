/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.rules

import android.app.UiAutomation.ROTATION_FREEZE_0
import android.app.UiAutomation.ROTATION_FREEZE_270
import android.app.UiAutomation.ROTATION_FREEZE_90
import android.graphics.Rect
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.window.core.ExperimentalWindowApi
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.isSurfaceDuo
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.spanFromStart
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Custom [TestRule] that can be used together with [SingleScreenTest] and [DualScreenTest]
 * in order to run tests on the specified posture.
 * Test methods annotated with [SingleScreenTest] will keep or unspan the app to first display area,
 * and methods annotated with [DualScreenTest] will span the app to entire display area.
 */
@OptIn(ExperimentalWindowApi::class)
class DualScreenTestRule : TestRule {
    private val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val flow = MutableSharedFlow<WindowLayoutInfo>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val overrideServices = PublishWindowInfoTrackerDecorator(flow)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                before(description)
                try {
                    base.evaluate()
                } finally {
                    after()
                }
            }
        }
    }

    private fun before(description: Description?) {
        val dualScreenTest = description?.getAnnotation(DualScreenTest::class.java)
        val singleScreenTest = description?.getAnnotation(SingleScreenTest::class.java)
        val requestedDeviceOrientation = dualScreenTest?.orientation ?: singleScreenTest?.orientation ?: ROTATION_FREEZE_0

        val runDualScreenTest = dualScreenTest != null
        if (uiDevice.isSurfaceDuo()) {
            if (runDualScreenTest) {
                uiDevice.spanFromStart()
            }
        } else {
            WindowInfoTracker.overrideDecorator(overrideServices)
            mockFoldingFeature(requestedDeviceOrientation, runDualScreenTest)
        }

        rotateDevice(requestedDeviceOrientation)
    }

    private fun rotateDevice(requestedDeviceOrientation: Int) {
        when (requestedDeviceOrientation) {
            ROTATION_FREEZE_270 -> uiDevice.setOrientationRight()
            ROTATION_FREEZE_90 -> uiDevice.setOrientationLeft()
            ROTATION_FREEZE_0 -> uiDevice.setOrientationNatural()
        }
    }

    private fun mockFoldingFeature(deviceOrientation: Int, forDualScreenTest: Boolean) {
        val displayFeatures = if (forDualScreenTest) {
            val foldingFeature = FoldingFeature(
                windowBounds = Rect(0, 0, uiDevice.displayWidth, uiDevice.displayHeight),
                state = FoldingFeature.State.FLAT,
                size = 0,
                orientation = getFoldingFeatureOrientation(deviceOrientation)
            )
            listOf(foldingFeature)
        } else {
            emptyList()
        }
        val windowLayoutInfo = TestWindowLayoutInfo(displayFeatures)
        overrideWindowLayoutInfo(windowLayoutInfo)
    }

    private fun getFoldingFeatureOrientation(deviceOrientation: Int): FoldingFeature.Orientation {
        return when (deviceOrientation) {
            ROTATION_FREEZE_270,
            ROTATION_FREEZE_90 -> FoldingFeature.Orientation.HORIZONTAL
            else -> FoldingFeature.Orientation.VERTICAL
        }
    }

    private fun after() {
        uiDevice.resetOrientation()
        WindowInfoTracker.reset()
    }

    private fun overrideWindowLayoutInfo(info: WindowLayoutInfo) {
        flow.tryEmit(info)
    }
}