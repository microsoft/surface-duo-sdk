/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

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
class FragmentExtensionsTests {
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
    fun testIsOnStartContainer() {
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

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
    fun testIsOnEndContainer() {
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

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
