/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.navigation.foldableNavOptions
import androidx.navigation.testutils.EmptyFragment
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.navigation.utils.SimpleFragmentBackStackListener
import com.microsoft.device.dualscreen.navigation.utils.SurfaceDuoSimpleActivity
import com.microsoft.device.dualscreen.navigation.utils.runWithBackStackListener
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate
import com.microsoft.device.dualscreen.testing.WindowLayoutInfoConsumer
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.screenMode
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class FragmentManagerExtensionsTests {
    @get:Rule
    val activityScenarioRule = activityScenarioRule<SurfaceDuoSimpleActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

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
        windowLayoutInfoConsumer.unregister()
    }

    @Test
    fun testIsTransitionToDualScreenPossible() {
        fragmentBackStackListener.resetCounter(2)

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            assertThat(foldableFragmentManager.isTransitionToDualScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            assertThat(foldableFragmentManager.isTransitionToDualScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isTransitionToDualScreenPossible()).isTrue()
        }
    }

    @Test
    fun testIsTransitionToSingleScreenPossible() {
        fragmentBackStackListener.resetCounter(2)
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isFalse()

            val fragmentOne = Fragment()
            var fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentOne, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentOne, "backStack1", navOptions)
            fragmentTransaction.commit()
            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isFalse()

            val fragmentTwo = Fragment()
            fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentTwo, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentTwo, "backStack12", navOptions)
            fragmentTransaction.commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isTrue()
        }
    }

    @Test
    fun testIsPopOnDualScreenPossible() {
        fragmentBackStackListener.resetCounter(2)
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            assertThat(foldableFragmentManager.isPopOnDualScreenPossible()).isFalse()

            val fragmentOne = Fragment()
            var fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentOne, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentOne, "backStack1", navOptions)
            fragmentTransaction.commit()
            assertThat(foldableFragmentManager.isPopOnDualScreenPossible()).isFalse()

            val fragmentTwo = Fragment()
            fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentTwo, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentTwo, "backStack12", navOptions)
            fragmentTransaction.commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isPopOnDualScreenPossible()).isTrue()
        }
    }

    @Test
    fun testTopFragment() {
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this).apply {
                screenMode = windowLayoutInfoConsumer.windowLayoutInfo?.screenMode ?: ScreenMode.SINGLE_SCREEN
            }

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            fragmentBackStackListener.resetCounter(2)

            val fragmentOne = EmptyFragment()
            var fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentOne, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentOne, "backStack1", navOptions)
            fragmentTransaction.commit()

            val fragmentTwo = EmptyFragment()
            fragmentTransaction = foldableFragmentManager.beginTransaction(fragmentTwo, navOptions)
            foldableFragmentManager.addToBackStack(fragmentTransaction, fragmentTwo, "backStack2", navOptions)
            fragmentTransaction.commit()

            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.topFragment).isSameInstanceAs(
                fragmentTwo
            )

            fragmentBackStackListener.resetCounter(1)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                foldableFragmentManager.popBackStack(false, "backStack2", POP_BACK_STACK_INCLUSIVE)
            }
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.topFragment).isSameInstanceAs(
                fragmentOne
            )

            fragmentBackStackListener.resetCounter(1)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                foldableFragmentManager.popBackStack(false, "backStack1", POP_BACK_STACK_INCLUSIVE)
            }
            fragmentBackStackListener.waitForChanges()
            fragmentBackStackListener.resetCounter(1)
            assertThat(foldableFragmentManager.topFragment).isNull()
        }
    }
}
