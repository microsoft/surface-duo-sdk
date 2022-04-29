/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.rules

import android.app.Activity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.testing.isSurfaceDuo
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

fun <A : Activity> foldableTestRule(
    activityScenarioRule: ActivityScenarioRule<A>,
    dualScreenTestRule: DualScreenTestRule,
    vararg aroundRules: TestRule
): TestRule {
    val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    var ruleChain = if (uiDevice.isSurfaceDuo()) {
        RuleChain.outerRule(activityScenarioRule).around(dualScreenTestRule)
    } else {
        RuleChain.outerRule(dualScreenTestRule).around(activityScenarioRule)
    }

    aroundRules.forEach {
        ruleChain = ruleChain.around(it)
    }
    return ruleChain
}