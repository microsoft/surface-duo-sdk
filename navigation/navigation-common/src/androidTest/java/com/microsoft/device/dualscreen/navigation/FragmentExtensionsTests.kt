/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.navigation.foldableNavOptions
import androidx.navigation.testutils.EmptyFragment
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.navigation.utils.SimpleFragmentBackStackListener
import com.microsoft.device.dualscreen.navigation.utils.SurfaceDuoSimpleActivity
import com.microsoft.device.dualscreen.navigation.utils.runWithBackStackListener
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.rules.FoldableTestRule
import com.microsoft.device.dualscreen.testing.rules.foldableRuleChain
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.screenMode
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@MediumTest
@RunWith(FoldableJUnit4ClassRunner::class)
class FragmentExtensionsTests {
    private val activityScenarioRule = activityScenarioRule<SurfaceDuoSimpleActivity>()
    private val foldableTestRule = FoldableTestRule()
    private val windowLayoutInfoConsumer = WindowLayoutInfoConsumer()
    private val fragmentBackStackListener = SimpleFragmentBackStackListener()
    private val currentActivityDelegate = CurrentActivityDelegate()

    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)

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
        windowLayoutInfoConsumer.unregister()
    }

    @Test
    @DualScreenTest
    fun testIsOnStartContainer() {
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            fragmentBackStackListener.resetCounter(1)
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.START
                }
            }

            val fragment = EmptyFragment()
            val fragmentTransaction = foldableFragmentManager.beginTransaction(fragment, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragment, "backStack", navOptions)
            fragmentTransaction.commit()

            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isOnStartContainer(fragment.TAG + "-" + 0)).isTrue()
        }
    }

    @Test
    @DualScreenTest
    fun testIsOnEndContainer() {
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            fragmentBackStackListener.resetCounter(1)
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.END
                }
            }

            val firstFragment = EmptyFragment()
            var fragmentTransaction = foldableFragmentManager.beginTransaction(firstFragment, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, firstFragment, "backStack1", navOptions)
            fragmentTransaction.commit()

            val secondFragment = EmptyFragment()
            fragmentTransaction = foldableFragmentManager.beginTransaction(secondFragment, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, secondFragment, "backStack2", navOptions)
            fragmentTransaction.commit()

            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isOnEndContainer(secondFragment.TAG + "-" + 1)).isTrue()
        }
    }
}
