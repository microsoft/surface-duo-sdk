/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import androidx.core.app.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import org.junit.rules.TestRule

/**
 * FOLDINGFEATURE HELPER
 * -----------------------------------------------------------------------------------------------
 * These functions can be used in foldable UI tests to simulate the present of vertical and
 * horizontal foldingFeatures(folds/hinges). The foldingFeatures are simulated using TestWindowLayoutInfo.
 */

/**
 * Return WindowLayoutInfoPublisherRule which allows you to push through different WindowLayoutInfo
 * values on demand from Window.testing to test
 *
 */
fun createWindowLayoutInfoPublisherRule(): TestRule {
    return WindowLayoutInfoPublisherRule()
}

/**
 * Simulate a vertical foldingFeature
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 */
fun <A : ComponentActivity> TestRule.simulateVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    simulateFoldingFeature(activityRule, center, size, state, FoldingFeature.Orientation.VERTICAL)
}

/**
 * Simulate a horizontal foldingFeature
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 */
fun <A : ComponentActivity> TestRule.simulateHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    simulateFoldingFeature(activityRule, center, size, state, FoldingFeature.Orientation.HORIZONTAL)
}

/**
 * Simulate a foldingFeature with the given properties
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 * @param orientation: orientation of foldingFeature
 */
private fun <A : ComponentActivity> TestRule.simulateFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int,
    size: Int,
    state: FoldingFeature.State,
    orientation: FoldingFeature.Orientation,
) {
    this as? WindowLayoutInfoPublisherRule
        ?: throw ClassCastException("Test rule is not of type WindowLayoutInfoPublisherRule")

    activityRule.scenario.onActivity { activity ->
        val foldingFeature = FoldingFeature(
            activity = activity,
            center = center,
            state = state,
            size = size,
            orientation = orientation
        )
        val windowLayoutInfo = TestWindowLayoutInfo(listOf(foldingFeature))
        overrideWindowLayoutInfo(windowLayoutInfo)
    }
}

// ----- Market devices FoldingFeature simulation -----//

/**
 * Simulate Surface Duo 1 folding feature
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is FLAT.
 * @param orientation : VERTICAL or HORIZONTAL (dual-portrait or dual-landscape)
 */
fun <A : ComponentActivity> TestRule.simulateSurfaceDuo1(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT,
    orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL
) {
    simulateFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = DeviceModel.SurfaceDuo.foldWidth,
        state = state,
        orientation = orientation
    )
}

/**
 * Simulate Surface Duo 2 folding feature
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is FLAT.
 * @param orientation : VERTICAL or HORIZONTAL (dual-portrait or dual-landscape)
 */
fun <A : ComponentActivity> TestRule.simulateSurfaceDuo2(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT,
    orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL
) {
    simulateFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = DeviceModel.SurfaceDuo2.foldWidth,
        state = state,
        orientation = orientation
    )
}

/**
 * Simulate Fold device with a vertical FoldingFeature of 0px.
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is HALF_OPENED.
 */
fun <A : ComponentActivity> TestRule.simulateFoldDevice(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    simulateFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 0,
        state = state,
        orientation = FoldingFeature.Orientation.VERTICAL
    )
}

/**
 * Simulate Flip device with an horizontal FoldingFeature of 0px.
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is HALF_OPENED.
 */
fun <A : ComponentActivity> TestRule.simulateFlipDevice(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    simulateFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 0,
        state = state,
        orientation = FoldingFeature.Orientation.HORIZONTAL
    )
}