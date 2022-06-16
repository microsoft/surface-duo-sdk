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

fun <A : Activity> foldableRuleChain(
    activityScenarioRule: ActivityScenarioRule<A>,
    foldableTestRule: FoldableTestRule,
    vararg aroundRules: TestRule
): RuleChain {
    val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    var ruleChain = if (uiDevice.isSurfaceDuo()) {
        RuleChain.outerRule(activityScenarioRule).around(foldableTestRule)
    } else {
        RuleChain.outerRule(foldableTestRule).around(activityScenarioRule)
    }

    aroundRules.forEach {
        ruleChain = ruleChain.around(it)
    }
    return ruleChain
}