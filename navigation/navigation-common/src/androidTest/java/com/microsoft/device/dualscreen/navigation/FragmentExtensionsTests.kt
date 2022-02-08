/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.navigation.foldableNavOptions
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.navigation.utils.SimpleFragmentBackStackListener
import com.microsoft.device.dualscreen.navigation.utils.SurfaceDuoSimpleActivity
import com.microsoft.device.dualscreen.navigation.utils.runWithBackStackListener
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate
import com.microsoft.device.dualscreen.testing.SurfaceDuo1
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class FragmentExtensionsTests {
    @get:Rule
    val activityScenarioRule = activityScenarioRule<SurfaceDuoSimpleActivity>()
    private val fragmentBackStackListener = SimpleFragmentBackStackListener()
    private val currentActivityDelegate = CurrentActivityDelegate()
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()

    @Before
    fun setup() {
        currentActivityDelegate.setup(activityScenarioRule)
        activityScenarioRule.scenario.onActivity {
            windowLayoutInfoConsumer.register(it)
        }
    }

    @After
    fun clear() {
        currentActivityDelegate.clear(activityScenarioRule)
        windowLayoutInfoConsumer.reset()
    }

    @Test
    fun testIsOnStartContainer() {
        windowLayoutInfoConsumer.reset()
        SurfaceDuo1.switchFromSingleToDualScreen()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            fragmentBackStackListener.resetCounter(1)
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.START
                }
            }

            val fragment = androidx.navigation.testutils.EmptyFragment()
            foldableFragmentManager.beginTransaction(fragment, navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(fragment.isOnStartContainer()).isTrue()
        }
    }

    @Test
    fun testIsOnEndContainer() {
        windowLayoutInfoConsumer.reset()
        SurfaceDuo1.switchFromSingleToDualScreen()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            fragmentBackStackListener.resetCounter(1)
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.END
                }
            }

            foldableFragmentManager.beginTransaction(
                androidx.navigation.testutils.EmptyFragment(),
                navOptions
            ).commit()
            val fragment = androidx.navigation.testutils.EmptyFragment()
            foldableFragmentManager.beginTransaction(fragment, navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(fragment.isOnEndContainer()).isTrue()
        }
    }
}
