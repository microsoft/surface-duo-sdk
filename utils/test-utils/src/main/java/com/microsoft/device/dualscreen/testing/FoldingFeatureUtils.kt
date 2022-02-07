/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import androidx.activity.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import org.junit.rules.TestRule

/**
 * These functions can be used in foldable UI tests to simulate the present of vertical and
 * horizontal foldingFeatures(folds/hinges). The foldingFeatures are simulated using TestWindowLayoutInfo.
 * We have added as well specific mock-FoldingFeatures that mirror Surface Duo and other well known foldable devices'
 * folding features.
 *
 * Most of this code has been taken from our Foldable Jetpack Compose testing library to make it available to this
 * library too without the need for you to add Jetpack Compose specific dependencies.
 * If you are looking for Jetpack Compose specific FoldingFeature (and other) utilities, please have a look at
 * our Jetpack Compose Testing library: https://github.com/microsoft/surface-duo-compose-sdk/tree/main/ComposeTesting
 */

/**
 * Create a vertical foldingFeature
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 */
fun <A : ComponentActivity> TestRule.createVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    createFoldingFeature(activityRule, center, size, state, FoldingFeature.Orientation.VERTICAL)
}

/**
 * Create a horizontal foldingFeature
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 */
fun <A : ComponentActivity> TestRule.createHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    createFoldingFeature(activityRule, center, size, state, FoldingFeature.Orientation.HORIZONTAL)
}

/**
 * Create a foldingFeature with the given properties
 *
 * @param activityRule: test activity rule
 * @param center: location of center of foldingFeature
 * @param size: size of foldingFeature
 * @param state: state of foldingFeature
 * @param orientation: orientation of foldingFeature
 */
private fun <A : ComponentActivity> TestRule.createFoldingFeature(
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

//----- Market devices FoldingFeature recreation -----//

/**
 * Create Surface Duo 1 folding feature
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is FLAT.
 */
fun <A : ComponentActivity> TestRule.createSurfaceDuo1FoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT
) {
    createFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 84,
        state = state,
        orientation = FoldingFeature.Orientation.VERTICAL
    )
}

/**
 * Create Surface Duo 2 folding feature
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is FLAT.
 */
fun <A : ComponentActivity> TestRule.createSurfaceDuo2FoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT
) {
    createFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 66,
        state = state,
        orientation = FoldingFeature.Orientation.VERTICAL
    )
}

/**
 * Create Market released foldable folding feature with a vertical FoldingFeature of 0px.
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is HALF_OPENED.
 */
fun <A : ComponentActivity> TestRule.createFoldWithVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    createFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 0,
        state = state,
        orientation = FoldingFeature.Orientation.VERTICAL
    )
}

/**
 * Simulate Market released foldable folding feature with an horizontal FoldingFeature of 0px.
 *
 * @param activityRule :  Activity scenario rule
 * @param state : FLAT or HALF_OPENED. Default value is HALF_OPENED.
 */
fun <A : ComponentActivity> TestRule.createFoldWithHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED
) {
    createFoldingFeature(
        activityRule = activityRule,
        center = -1,
        size = 0,
        state = state,
        orientation = FoldingFeature.Orientation.VERTICAL
    )
}