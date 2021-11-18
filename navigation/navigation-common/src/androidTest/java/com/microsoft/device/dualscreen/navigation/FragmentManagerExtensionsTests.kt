/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.foldableNavOptions
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.navigation.utils.CurrentActivityDelegate
import com.microsoft.device.dualscreen.navigation.utils.SimpleFragmentBackStackListener
import com.microsoft.device.dualscreen.navigation.utils.SurfaceDuoSimpleActivity
import com.microsoft.device.dualscreen.navigation.utils.runWithBackStackListener
import com.microsoft.device.dualscreen.utils.test.switchFromSingleToDualScreen
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
    private val fragmentBackStackListener = SimpleFragmentBackStackListener()
    private val currentActivityDelegate = CurrentActivityDelegate()

    @Before
    fun setup() {
        currentActivityDelegate.setup(activityScenarioRule)
    }

    @After
    fun clear() {
        currentActivityDelegate.clear(activityScenarioRule)
    }

    @Test
    fun testIsTransitionToDualScreenPossible() {
        fragmentBackStackListener.resetCounter(2)

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

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

        switchFromSingleToDualScreen()
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.isTransitionToSingleScreenPossible()).isTrue()
        }
    }

    @Test
    fun testIsPopOnDualScreenPossible() {
        fragmentBackStackListener.resetCounter(2)

        switchFromSingleToDualScreen()
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            assertThat(foldableFragmentManager.fragmentManager.isPopOnDualScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            assertThat(foldableFragmentManager.fragmentManager.isPopOnDualScreenPossible()).isFalse()

            foldableFragmentManager.beginTransaction(Fragment(), navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.fragmentManager.isPopOnDualScreenPossible()).isTrue()
        }
    }

    @Test
    fun testTopFragment() {
        switchFromSingleToDualScreen()
        assertThat(currentActivityDelegate.currentActivity).isNotNull()

        currentActivityDelegate.runWithBackStackListener(fragmentBackStackListener) {
            val foldableFragmentManager = FoldableFragmentManagerWrapper(this)

            val navOptions = foldableNavOptions {
                launchScreen {
                    launchScreen = LaunchScreen.DEFAULT
                }
            }

            fragmentBackStackListener.resetCounter(2)
            val fragmentOne = androidx.navigation.testutils.EmptyFragment()
            val fragmentTwo = androidx.navigation.testutils.EmptyFragment()
            foldableFragmentManager.beginTransaction(fragmentOne, navOptions).commit()
            foldableFragmentManager.beginTransaction(fragmentTwo, navOptions).commit()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.fragmentManager.topFragment).isSameInstanceAs(fragmentTwo)

            fragmentBackStackListener.resetCounter(1)
            foldableFragmentManager.fragmentManager.popBackStack()
            fragmentBackStackListener.waitForChanges()
            assertThat(foldableFragmentManager.fragmentManager.topFragment).isSameInstanceAs(fragmentOne)

            fragmentBackStackListener.resetCounter(1)
            foldableFragmentManager.fragmentManager.popBackStack()
            fragmentBackStackListener.waitForChanges()
            fragmentBackStackListener.resetCounter(1)
            assertThat(foldableFragmentManager.fragmentManager.topFragment).isNull()
        }
    }
}
