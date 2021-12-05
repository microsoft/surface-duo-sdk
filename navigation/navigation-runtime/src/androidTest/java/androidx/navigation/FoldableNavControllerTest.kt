/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions Copyright (c) Microsoft Corporation
 */

package androidx.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation.test.R
import androidx.navigation.testutils.TestNavigator
import androidx.navigation.testutils.test
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.argThat
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

@MediumTest
@RunWith(AndroidJUnit4::class)
class FoldableNavControllerTest {

    companion object {
        private const val UNKNOWN_DESTINATION_ID = -1
        private const val TEST_ARG = "test"
        private const val TEST_ARG_VALUE = "value"
        private const val TEST_ARG_VALUE_INT = 123
        private const val TEST_OVERRIDDEN_VALUE_ARG = "test_overriden_value"
        private const val TEST_ACTION_OVERRIDDEN_VALUE_ARG = "test_action_overriden_value"
        private const val TEST_OVERRIDDEN_VALUE_ARG_VALUE = "override"
    }

    @UiThreadTest
    @Test
    fun testGetCurrentBackStackEntry() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_start_destination)
        assertEquals(R.id.start_test, navController.currentBackStackEntry?.destination?.id ?: 0)
    }

    @UiThreadTest
    @Test
    fun testGetCurrentBackStackEntryEmptyBackStack() {
        val navController = createNavController()
        assertThat(navController.currentBackStackEntry).isNull()
    }

    @UiThreadTest
    @Test
    fun testGetPreviousBackStackEntry() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.start_test, navController.previousBackStackEntry?.destination?.id ?: 0)
    }

    @UiThreadTest
    @Test
    fun testGetPreviousBackStackEntryEmptyBackStack() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        assertThat(navController.previousBackStackEntry).isNull()
    }

    @UiThreadTest
    @Test
    fun testStartDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_start_destination)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
    }

    @UiThreadTest
    @Test
    fun testSetGraphTwice() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_start_destination)
        val navigator = navController.navigatorProvider[TestNavigator::class]
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.start_test)
        assertThat(navigator.backStack.size)
            .isEqualTo(1)

        // Now set a new graph, overriding the first
        navController.setGraph(R.navigation.nav_nested_start_destination)
        assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.nested_test)
        assertThat(navigator.backStack.size)
            .isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testStartDestinationWithArgs() {
        val navController = createNavController()
        val args = Bundle().apply {
            putString(TEST_ARG, TEST_ARG_VALUE)
        }
        navController.setGraph(R.navigation.nav_start_destination, args)
        val navigator = navController.navigatorProvider[TestNavigator::class]
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
        val foundArgs = navigator.current.second
        assertNotNull(foundArgs)
        assertEquals(TEST_ARG_VALUE, foundArgs?.getString(TEST_ARG))
    }

    @UiThreadTest
    @Test(expected = IllegalArgumentException::class)
    fun testStartDestinationWithWrongArgs() {
        val navController = createNavController()
        val args = Bundle().apply {
            putInt(TEST_ARG, TEST_ARG_VALUE_INT)
        }
        navController.setGraph(R.navigation.nav_start_destination, args)
    }

    @UiThreadTest
    @Test
    fun testStartDestinationWithArgsProgrammatic() {
        val navController = createNavController()
        val args = Bundle().apply {
            putString(TEST_ARG, TEST_ARG_VALUE)
        }

        val navGraph = navController.navigatorProvider.navigation(
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, args)
        val navigator = navController.navigatorProvider[TestNavigator::class]
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
        val foundArgs = navigator.current.second
        assertNotNull(foundArgs)
        assertEquals(TEST_ARG_VALUE, foundArgs?.getString(TEST_ARG))
    }

    @UiThreadTest
    @Test(expected = IllegalStateException::class)
    fun testMissingStartDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_missing_start_destination)
    }

    @UiThreadTest
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidStartDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_invalid_start_destination)
    }

    @UiThreadTest
    @Test
    fun testNestedStartDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_nested_start_destination)
        assertEquals(R.id.nested_test, navController.currentDestination?.id ?: 0)
    }

    @UiThreadTest
    @Test
    fun testSetGraph() {
        val navController = createNavController()

        navController.setGraph(R.navigation.nav_start_destination)
        assertNotNull(navController.graph)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
    }

    @UiThreadTest
    @Test
    fun testGetGraphIllegalStateException() {
        val navController = createNavController()
        try {
            navController.graph
            fail("getGraph() should throw an IllegalStateException before setGraph()")
        } catch (expected: IllegalStateException) {
        }
    }

    @UiThreadTest
    @Test
    fun testNavigate() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testInvalidNavigateViaDeepLink() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val deepLinkRequest = FoldableNavDeepLinkRequest.Builder.fromUri(
            Uri.parse("android-app://androidx.navigation.test/invalid")
        ).build()

        try {
            navController.navigate(deepLinkRequest)
            fail("navController.navigate must throw")
        } catch (e: IllegalArgumentException) {
            assertThat(e)
                .hasMessageThat().contains(
                    "Navigation destination that matches request $deepLinkRequest cannot be " +
                        "found in the navigation graph ${navController.graph}"
                )
        }
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLink() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
        val intent = navigator.current.second?.getParcelable<Intent>(
            NavController.KEY_DEEP_LINK_INTENT
        )
        assertThat(intent?.data).isEqualTo(deepLink)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkDefaultArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(deepLink)

        val destination = navController.currentDestination
        assertThat(destination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
        assertThat(destination?.arguments?.get("defaultArg")?.defaultValue.toString())
            .isEqualTo("defaultValue")
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkAction() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val action = "test.action"
        val deepLink = FoldableNavDeepLinkRequest(null, action, null)

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
        val intent = navigator.current.second?.getParcelable<Intent>(
            NavController.KEY_DEEP_LINK_INTENT
        )
        assertThat(intent?.action).isEqualTo(action)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkActionDifferentURI() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink =
            FoldableNavDeepLinkRequest(Uri.parse("invalidDeepLink.com"), "test.action", null)

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkMimeTypeDifferentUri() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink =
            FoldableNavDeepLinkRequest(Uri.parse("invalidDeepLink.com"), null, "type/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkMimeType() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_deeplink)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val mimeType = "type/test"
        val deepLink = FoldableNavDeepLinkRequest(null, null, mimeType)

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.forth_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
        val intent = navigator.current.second?.getParcelable<Intent>(
            NavController.KEY_DEEP_LINK_INTENT
        )
        assertThat(intent?.type).isEqualTo(mimeType)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkMimeTypeWildCard() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_deeplink)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = FoldableNavDeepLinkRequest(null, null, "any/thing")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.first_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkMimeTypeWildCardSubtype() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_deeplink)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = FoldableNavDeepLinkRequest(null, null, "image/jpg")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaDeepLinkMimeTypeWildCardType() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_deeplink)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = FoldableNavDeepLinkRequest(null, null, "doesNotEvenMatter/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.third_test)
        assertThat(navigator.backStack.size).isEqualTo(2)
    }

    @UiThreadTest
    @Test
    fun testNavigationViaDeepLinkPopUpTo() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(
            deepLink,
            foldableNavOptions {
                popUpTo(R.id.nav_root) { inclusive = true }
            }
        )
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testNavigateToDifferentGraphViaDeepLink() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_multiple_navigation)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.deep_link_child_second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        val popped = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return true when popping a non-root destination")
            .that(popped)
            .isTrue()
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testNavigateToDifferentGraphViaDeepLink3x() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_multiple_navigation)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.deep_link_child_second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        navController.popBackStack(true)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        // repeat nav and pop 2 more times.
        navController.navigate(deepLink)
        navController.popBackStack(true)
        navController.navigate(deepLink)

        val popped = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return true when popping a non-root destination")
            .that(popped)
            .isTrue()
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testNavigateToDifferentGraphViaDeepLinkToGrandchild3x() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_multiple_navigation)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val deepLink = Uri.parse("android-app://androidx.navigation.test/grand_child_test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.deep_link_grandchild_start_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        navController.popBackStack(true)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        // repeat nav and pop 2 more times.
        navController.navigate(deepLink)
        navController.popBackStack(true)
        navController.navigate(deepLink)

        val popped = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return true when popping a non-root destination")
            .that(popped)
            .isTrue()
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)
    }

    @LargeTest
    @Test
    @SdkSuppress(minSdkVersion = 17)
    fun testNavigateViaImplicitDeepLink() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("android-app://androidx.navigation.test/test/argument1/argument2"),
            ApplicationProvider.getApplicationContext() as Context,
            TestActivity::class.java
        )

        Intents.init()

        with(ActivityScenario.launch<TestActivity>(intent)) {
            moveToState(Lifecycle.State.CREATED)
            onActivity { activity ->
                run {
                    val navController = activity.navController
                    navController.setGraph(R.navigation.nav_simple)

                    val navigator =
                        navController.navigatorProvider.getNavigator(TestNavigator::class.java)

                    assertThat(
                        navController.currentDestination!!.id
                    ).isEqualTo(R.id.second_test)

                    // Only the leaf destination should be on the stack.
                    assertThat(navigator.backStack.size).isEqualTo(1)
                    // The parent will be constructed in a new Activity after navigateUp()
                    navController.navigateUp()
                }
            }

            assertThat(this.state).isEqualTo(Lifecycle.State.DESTROYED)
        }

        // this relies on MonitoringInstrumentation.execStartActivity() which was added in API 17
        intended(
            allOf(
                toPackage((ApplicationProvider.getApplicationContext() as Context).packageName),
                not(hasData(anyString())), // The rethrow should not use the URI as primary target.
                hasExtra(FoldableNavController.KEY_DEEP_LINK_IDS, intArrayOf(R.id.nav_root)),
                hasExtra(
                    Matchers.`is`(FoldableNavController.KEY_DEEP_LINK_EXTRAS),
                    allOf(
                        BundleMatchers.hasEntry("arg1", "argument1"),
                        BundleMatchers.hasEntry("arg2", "argument2"),
                        BundleMatchers.hasEntry(
                            FoldableNavController.KEY_DEEP_LINK_INTENT,
                            allOf(
                                hasAction(intent.action),
                                hasData(intent.data),
                                hasComponent(intent.component)
                            )
                        )
                    )
                )
            )
        )

        Intents.release()
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreStateXml() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = SaveStateTestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)

        val savedState = navController.saveState()
        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        // Restore state doesn't recreate any graph
        navController.restoreState(savedState)
        assertNull(navController.currentDestination)

        // Explicitly setting a graph then restores the state
        navController.setGraph(R.navigation.nav_simple)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
        // Save state should be called on the navigator exactly once
        assertEquals(1, navigator.saveStateCount)
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreStateDestinationChanged() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = SaveStateTestNavigator()
        navController.navigatorProvider.addNavigator(navigator)

        navController.setGraph(R.navigation.nav_simple)

        val savedState = navController.saveState()
        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        // Restore state doesn't recreate any graph
        navController.restoreState(savedState)
        assertNull(navController.currentDestination)

        var destinationChangedCount = 0

        navController.addOnDestinationChangedListener { _, _, _ ->
            destinationChangedCount++
        }

        // Explicitly setting a graph then restores the state
        navController.setGraph(R.navigation.nav_simple)
        // Save state should be called on the navigator exactly once
        assertEquals(1, navigator.saveStateCount)
        // listener should have been fired again when state restored
        assertThat(destinationChangedCount).isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreStateProgrammatic() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = TestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        val graph = FoldableNavInflater(context, navController.navigatorProvider)
            .inflate(R.navigation.nav_simple)
        navController.graph = graph
        navController.navigate(R.id.second_test)

        val savedState = navController.saveState()
        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        // Restore state doesn't recreate any graph
        navController.restoreState(savedState)
        assertNull(navController.currentDestination)

        // Explicitly setting a graph then restores the state
        navController.graph = graph
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreStateBundleParceled() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = SaveStateTestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.nav_simple)

        navigator.customParcel = CustomTestParcelable(TEST_ARG_VALUE)

        val savedState = navController.saveState()

        val parcel = Parcel.obtain()
        savedState?.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val restoredState = Bundle.CREATOR.createFromParcel(parcel)

        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        navController.restoreState(restoredState)
        navController.setGraph(R.navigation.nav_simple)

        // Ensure custom parcelable is present and can be read
        assertThat(navigator.customParcel?.name).isEqualTo(TEST_ARG_VALUE)
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreAfterNavigateToDifferentNavGraph() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = SaveStateTestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.nav_multiple_navigation)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val deepLink = Uri.parse("android-app://androidx.navigation.test/test")

        navController.navigate(deepLink)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.deep_link_child_second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        navController.navigate(R.id.simple_child_start)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(3)

        val savedState = navController.saveState()
        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        // Restore state doesn't recreate any graph
        navController.restoreState(savedState)
        assertThat(navController.currentDestination).isNull()

        // Explicitly setting a graph then restores the state
        navController.setGraph(R.navigation.nav_multiple_navigation)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.simple_child_start_test)
        assertThat(navigator.backStack.size).isEqualTo(3)
        // Save state should be called on the navigator exactly once
        assertThat(navigator.saveStateCount).isEqualTo(1)
    }

    @UiThreadTest
    @Test
    fun testBackstackArgsBundleParceled() {
        val context = ApplicationProvider.getApplicationContext() as Context
        var navController = FoldableNavController(context)
        val navigator = SaveStateTestNavigator()
        navController.navigatorProvider.addNavigator(navigator)

        val backStackArg1 = Bundle()
        backStackArg1.putParcelable(TEST_ARG, CustomTestParcelable(TEST_ARG_VALUE))
        navController.setGraph(R.navigation.nav_arguments)
        navController.navigate(R.id.second_test, backStackArg1)

        val savedState = navController.saveState()

        val parcel = Parcel.obtain()
        savedState?.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val restoredState = Bundle.CREATOR.createFromParcel(parcel)

        navController = FoldableNavController(context)
        navController.navigatorProvider.addNavigator(navigator)

        navController.restoreState(restoredState)
        navController.setGraph(R.navigation.nav_arguments)

        navController.addOnDestinationChangedListener { _, _, arguments ->
            assertThat(arguments?.getParcelable<CustomTestParcelable>(TEST_ARG)?.name)
                .isEqualTo(TEST_ARG_VALUE)
        }
    }

    @UiThreadTest
    @Test
    fun testNavigateArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_arguments)

        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val returnedArgs = navigator.current.second
        assertThat(returnedArgs).isNotNull()
        assertThat(returnedArgs!!["test_start_default"])
            .isEqualTo("default")

        navController.addOnDestinationChangedListener { _, _, arguments ->
            assertThat(arguments).isNotNull()
            assertThat(arguments!!["test_start_default"])
                .isEqualTo("default")
        }
    }

    @UiThreadTest
    @Test
    fun testNavigateWithNoDefaultValue() {
        val returnedArgs = navigateWithArgs(null)

        // Test that arguments without a default value aren't passed through at all
        assertFalse(returnedArgs.containsKey("test_no_default_value"))
    }

    @UiThreadTest
    @Test
    fun testNavigateWithDefaultArgs() {
        val returnedArgs = navigateWithArgs(null)

        // Test that default values are passed through
        assertEquals("default", returnedArgs.getString("test_default_value"))
    }

    @UiThreadTest
    @Test
    fun testNavigateWithArgs() {
        val args = Bundle()
        args.putString(TEST_ARG, TEST_ARG_VALUE)
        val returnedArgs = navigateWithArgs(args)

        // Test that programmatically constructed arguments are passed through
        assertEquals(TEST_ARG_VALUE, returnedArgs.getString(TEST_ARG))
    }

    @UiThreadTest
    @Test
    fun testNavigateWithOverriddenDefaultArgs() {
        val args = Bundle()
        args.putString(TEST_OVERRIDDEN_VALUE_ARG, TEST_OVERRIDDEN_VALUE_ARG_VALUE)
        val returnedArgs = navigateWithArgs(args)

        // Test that default values can be overridden by programmatic values
        assertEquals(
            TEST_OVERRIDDEN_VALUE_ARG_VALUE,
            returnedArgs.getString(TEST_OVERRIDDEN_VALUE_ARG)
        )
    }

    private fun navigateWithArgs(args: Bundle?): Bundle {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_arguments)

        navController.navigate(R.id.second_test, args)

        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val returnedArgs = navigator.current.second
        assertNotNull(returnedArgs)

        return returnedArgs!!
    }

    @UiThreadTest
    @Test
    fun testPopRoot() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        val success = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return false when popping the root")
            .that(success)
            .isFalse()
        assertNull(navController.currentDestination)
        assertEquals(0, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testPopOnEmptyStack() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        val success = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return false when popping the root")
            .that(success)
            .isFalse()
        assertNull(navController.currentDestination)
        assertEquals(0, navigator.backStack.size)

        val popped = navController.popBackStack(true)
        assertWithMessage(
            "popBackStack should return false when there's nothing on the " +
                "back stack"
        )
            .that(popped)
            .isFalse()
    }

    @UiThreadTest
    @Test
    fun testNavigateThenPop() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)

        val popped = navController.popBackStack(true)
        assertWithMessage("FoldableNavController should return true when popping a non-root destination")
            .that(popped)
            .isTrue()
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateThenPopToUnknownDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)

        val popped = navController.popBackStack(true, UNKNOWN_DESTINATION_ID, false)
        assertWithMessage("Popping to an invalid destination should return false")
            .that(popped)
            .isFalse()
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateThenNavigateWithPop() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(
            R.id.second_test,
            null,
            foldableNavOptions {
                popUpTo(R.id.start_test) { inclusive = true }
            }
        )
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateThenNavigateWithPopRoot() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(
            R.id.second_test,
            null,
            foldableNavOptions {
                popUpTo(R.id.nav_root) { inclusive = true }
            }
        )
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateThenNavigateUp() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)

        // This should function identically to popBackStack()
        val success = navController.navigateUp()
        assertThat(success)
            .isTrue()
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateThenNavigateUpWithDefaultArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)

        navController.navigate(R.id.start_test_with_default_arg)
        assertEquals(R.id.start_test_with_default_arg, navController.currentDestination?.id ?: 0)
        assertEquals(3, navigator.backStack.size)

        // This should function identically to popBackStack()
        val success = navController.navigateUp()
        assertThat(success).isTrue()
        val destination = navController.currentDestination
        assertEquals(R.id.second_test, destination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
        assertThat(destination?.arguments?.get("defaultArg")?.defaultValue.toString())
            .isEqualTo("defaultValue")
    }

    @UiThreadTest
    @Test
    fun testNavigateViaAction() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(1, navigator.backStack.size)

        navController.navigate(R.id.second)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionSingleTop() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(2, navigator.backStack.size)

        navController.navigate(R.id.self)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        assertEquals(2, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionSingleTopNewArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navigator.backStack.size).isEqualTo(2)

        val args = Bundle()
        val testKey = "testKey"
        val testValue = "testValue"
        args.putString(testKey, testValue)

        var destinationListenerExecuted = false

        navController.navigate(R.id.self, args)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            destinationListenerExecuted = true
            assertThat(destination.id).isEqualTo(R.id.second_test)
            assertThat(arguments?.getString(testKey)).isEqualTo(testValue)
        }

        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        val returnedArgs = navigator.current.second
        assertThat(returnedArgs?.getString(testKey)).isEqualTo(testValue)
        assertThat(destinationListenerExecuted).isTrue()
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionSingleTopReplaceNullArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.start_test)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navigator.backStack.size).isEqualTo(1)
        assertThat(navigator.current.second).isNull()

        val args = Bundle()
        val testKey = "testKey"
        val testValue = "testValue"
        args.putString(testKey, testValue)

        var destinationListenerExecuted = false

        navController.navigate(
            R.id.start_test,
            args,
            foldableNavOptions {
                launchSingleTop = true
            }
        )

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            destinationListenerExecuted = true
            assertThat(destination.id).isEqualTo(R.id.start_test)
            assertThat(arguments?.getString(testKey)).isEqualTo(testValue)
        }

        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.start_test)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val returnedArgs = navigator.current.second
        assertThat(returnedArgs?.getString(testKey)).isEqualTo(testValue)
        assertThat(destinationListenerExecuted).isTrue()
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionSingleTopReplaceWithDefaultArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.start_test_with_default_arg)
        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.start_test_with_default_arg)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navigator.backStack.size).isEqualTo(2)
        assertThat(navigator.current.second).isNotNull()
        assertThat(navigator.current.second?.getBoolean("defaultArg", false)).isTrue()

        val args = Bundle()
        val testKey = "testKey"
        val testValue = "testValue"
        args.putString(testKey, testValue)

        var destinationListenerExecuted = false

        navController.navigate(
            R.id.start_test_with_default_arg,
            args,
            foldableNavOptions {
                launchSingleTop = true
            }
        )

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            destinationListenerExecuted = true
            assertThat(destination.id).isEqualTo(R.id.start_test_with_default_arg)
            assertThat(arguments?.getString(testKey)).isEqualTo(testValue)
            assertThat(arguments?.getBoolean("defaultArg", false)).isTrue()
        }

        assertThat(navController.currentDestination?.id ?: 0)
            .isEqualTo(R.id.start_test_with_default_arg)
        assertThat(navigator.backStack.size).isEqualTo(2)

        val returnedArgs = navigator.current.second
        assertThat(returnedArgs?.getString(testKey)).isEqualTo(testValue)
        assertThat(returnedArgs?.getBoolean("defaultArg", false)).isTrue()
        assertThat(destinationListenerExecuted).isTrue()
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionSingleTopNewArgsIgnore() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)

        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.start_test)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertThat(navigator.backStack.size).isEqualTo(1)

        val args = Bundle()
        val testKey = "testKey"
        val testValue = "testValue"
        args.putString(testKey, testValue)

        var destinationListenerExecuted = false

        navController.navigate(R.id.second_test, args)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            destinationListenerExecuted = true
            assertThat(destination.id).isEqualTo(R.id.second_test)
            assertThat(arguments?.getString(testKey)).isEqualTo(testValue)
        }

        assertThat(navController.currentDestination?.id ?: 0).isEqualTo(R.id.second_test)
        assertThat(navigator.backStack.size).isEqualTo(2)

        val returnedArgs = navigator.current.second
        assertThat(returnedArgs?.getString(testKey)).isEqualTo(testValue)
        assertThat(destinationListenerExecuted).isTrue()
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionPopUpToInAction() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(2, navigator.backStack.size)

        navController.navigate(R.id.finish)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateWithPopUpOptionsOnly() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(2, navigator.backStack.size)

        val navOptions = foldableNavOptions {
            popUpTo = R.id.start_test
        }
        // the same as to call .navigate(R.id.finish)
        navController.navigate(0, null, navOptions)

        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNoDestinationNoPopUpTo() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val options = foldableNavOptions {}
        try {
            navController.navigate(0, null, options)
            fail("navController.navigate must throw")
        } catch (e: IllegalArgumentException) {
            // expected exception
        }
    }

    @UiThreadTest
    @Test
    fun testNavigateOptionPopSelf() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.second_test, navController.currentDestination?.id ?: 0)
        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        assertEquals(2, navigator.backStack.size)

        navController.navigate(R.id.finish_self)
        assertEquals(R.id.start_test, navController.currentDestination?.id ?: 0)
        assertEquals(1, navigator.backStack.size)
    }

    @UiThreadTest
    @Test
    fun testNavigateViaActionWithArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_arguments)

        val args = Bundle()
        args.putString(TEST_ARG, TEST_ARG_VALUE)
        args.putString(TEST_OVERRIDDEN_VALUE_ARG, TEST_OVERRIDDEN_VALUE_ARG_VALUE)
        navController.navigate(R.id.second, args)

        val navigator = navController.navigatorProvider.getNavigator(TestNavigator::class.java)
        val returnedArgs = navigator.current.second
        assertNotNull(returnedArgs)

        // Test that arguments without a default value aren't passed through at all
        assertFalse(returnedArgs!!.containsKey("test_no_default_value"))
        // Test that default values are passed through
        assertEquals("default", returnedArgs.getString("test_default_value"))
        // Test that programmatically constructed arguments are passed through
        assertEquals(TEST_ARG_VALUE, returnedArgs.getString(TEST_ARG))
        // Test that default values can be overridden by programmatic values
        assertEquals(
            TEST_OVERRIDDEN_VALUE_ARG_VALUE,
            returnedArgs.getString(TEST_OVERRIDDEN_VALUE_ARG)
        )
        // Test that default values can be overridden by action default values
        assertEquals(
            TEST_OVERRIDDEN_VALUE_ARG_VALUE,
            returnedArgs.getString(TEST_ACTION_OVERRIDDEN_VALUE_ARG)
        )
    }

    @UiThreadTest
    @Test
    fun testDeepLinkFromNavGraph() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)

        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.second_test)
            .createTaskStackBuilder()
        assertNotNull(taskStackBuilder)
        assertEquals(1, taskStackBuilder.intentCount)
    }

    @UiThreadTest
    @Test
    fun testDeepLinkIntent() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)

        val args = Bundle()
        args.putString("test", "test")
        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.second_test)
            .setArguments(args)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        navController.handleDeepLink(intent)

        // The original Intent should be untouched and safely writable to a Parcel
        val p = Parcel.obtain()
        intent!!.writeToParcel(p, 0)
    }

    @UiThreadTest
    @Test
    fun testDeepLinkIntentWithDefaultArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)

        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.second_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        navController.handleDeepLink(intent)

        // The original Intent should be untouched and safely writable to a Parcel
        val p = Parcel.obtain()
        intent!!.writeToParcel(p, 0)

        val destination = navController.currentDestination
        assertEquals(R.id.second_test, destination?.id ?: 0)
        assertThat(destination?.arguments?.get("defaultArg")?.defaultValue.toString())
            .isEqualTo("defaultValue")
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkValid() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.start_test)),
            any()
        )

        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.second_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("FoldableNavController should handle deep links to its own graph")
            .that(navController.handleDeepLink(intent))
            .isTrue()

        // Verify that we navigated down to the deep link
        verify(onDestinationChangedListener, times(2)).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.start_test)),
            any()
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.second_test)),
            any()
        )
        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkNestedStartDestination() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_nested_start_destination)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        val startDestination = navController.findDestination(R.id.nested_test)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            any()
        )

        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.second_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("FoldableNavController should handle deep links to its own graph")
            .that(navController.handleDeepLink(intent))
            .isTrue()

        // Verify that we navigated down to the deep link
        verify(onDestinationChangedListener, times(2)).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            any()
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.second_test)),
            any()
        )
        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkMultipleDestinations() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_multiple_navigation)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        val startDestination = navController.findDestination(R.id.simple_child_start_test)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            any()
        )
        val childDestination = navController.findDestination(R.id.simple_child_second_test)

        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.simple_child_second_test)
            .addDestination(R.id.deep_link_child_second_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("FoldableNavController should handle deep links to its own graph")
            .that(navController.handleDeepLink(intent))
            .isTrue()

        // Verify that we navigated down to the deep link
        // First to the destination added via setDestination()
        verify(onDestinationChangedListener, times(2)).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            any()
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(childDestination),
            any()
        )
        // Then to the second destination added via addDestination()
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.deep_link_child_start_test)),
            any()
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.deep_link_child_second_test)),
            any()
        )
        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkMultipleDestinationsWithArgs() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_multiple_navigation)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        val startDestination = navController.findDestination(R.id.simple_child_start_test)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            any()
        )
        val childDestination = navController.findDestination(R.id.simple_child_second_test)

        val globalBundle = Bundle().apply {
            putString("global", "global")
        }
        val firstBundle = Bundle().apply {
            putString("test", "first")
        }
        val secondBundle = Bundle().apply {
            putString("global", "overridden")
            putString("test", "second")
        }
        val taskStackBuilder = navController.createDeepLink()
            .setDestination(R.id.simple_child_second_test, firstBundle)
            .addDestination(R.id.deep_link_child_second_test, secondBundle)
            .setArguments(globalBundle)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("FoldableNavController should handle deep links to its own graph")
            .that(navController.handleDeepLink(intent))
            .isTrue()

        // Verify that we navigated down to the deep link
        // First to the destination added via setDestination()
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(startDestination),
            argThat { args ->
                args?.getString("global").equals("global") &&
                    args?.getString("test").equals("first")
            }
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(childDestination),
            argThat { args ->
                args?.getString("global").equals("global") &&
                    args?.getString("test").equals("first")
            }
        )
        // Then to the second destination added via addDestination()
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.deep_link_child_start_test)),
            argThat { args ->
                args?.getString("global").equals("overridden") &&
                    args?.getString("test").equals("second")
            }
        )
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.deep_link_child_second_test)),
            argThat { args ->
                args?.getString("global").equals("overridden") &&
                    args?.getString("test").equals("second")
            }
        )
        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkInvalid() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.start_test)),
            any()
        )

        val taskStackBuilder = navController.createDeepLink()
            .setGraph(R.navigation.nav_nested_start_destination)
            .setDestination(R.id.nested_second_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("handleDeepLink should return false when passed an invalid deep link")
            .that(navController.handleDeepLink(intent))
            .isFalse()

        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testHandleDeepLinkToRootInvalid() {
        val navController = createNavController()
        navController.setGraph(R.navigation.nav_simple)
        val onDestinationChangedListener =
            mock(FoldableNavController.OnDestinationChangedListener::class.java)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        verify(onDestinationChangedListener).onDestinationChanged(
            eq(navController),
            eq(navController.findDestination(R.id.start_test)),
            any()
        )

        val taskStackBuilder = navController.createDeepLink()
            .setGraph(R.navigation.nav_nested_start_destination)
            .setDestination(R.id.nested_test)
            .createTaskStackBuilder()

        val intent = taskStackBuilder.editIntentAt(0)
        assertNotNull(intent)
        assertWithMessage("handleDeepLink should return false when passed an invalid deep link")
            .that(navController.handleDeepLink(intent))
            .isFalse()

        verifyNoMoreInteractions(onDestinationChangedListener)
    }

    @UiThreadTest
    @Test
    fun testSetOnBackPressedDispatcherOnNavBackStackEntry() {
        var backPressedIntercepted = false
        val navController = createNavController()
        val lifecycleOwner = TestLifecycleOwner()
        val dispatcher = OnBackPressedDispatcher()

        navController.setLifecycleOwner(lifecycleOwner)
        navController.setOnBackPressedDispatcher(dispatcher)

        navController.setGraph(R.navigation.nav_simple)
        navController.navigate(R.id.second_test)
        assertEquals(R.id.start_test, navController.previousBackStackEntry?.destination?.id ?: 0)

        dispatcher.addCallback(
            navController.currentBackStackEntry!!,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressedIntercepted = true
                }
            }
        )
        // Move to STOPPED
        lifecycleOwner.currentState = Lifecycle.State.CREATED
        // Move back up to RESUMED
        lifecycleOwner.currentState = Lifecycle.State.RESUMED

        dispatcher.onBackPressed()

        assertThat(backPressedIntercepted).isTrue()
    }

    private fun createNavController(): FoldableNavController {
        val navController = FoldableNavController(ApplicationProvider.getApplicationContext())
        val navigator = TestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        return navController
    }
}

class TestActivity : AppCompatActivity() {

    val navController: FoldableNavController = createNavController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(View(this))
    }

    private fun createNavController(activity: Activity): FoldableNavController {
        val navController = FoldableNavController(activity)
        val navigator = TestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        return navController
    }
}

/**
 * [TestNavigator] that helps with testing saving and restoring state.
 */
@FoldableNavigator.Name("test")
class SaveStateTestNavigator : TestNavigator() {

    companion object {
        private const val STATE_SAVED_COUNT = "saved_count"
        private const val TEST_PARCEL = "test_parcel"
    }

    var saveStateCount = 0
    var customParcel: CustomTestParcelable? = null

    override fun onSaveState(): Bundle? {
        saveStateCount += 1
        val state = Bundle()
        state.putInt(STATE_SAVED_COUNT, saveStateCount)
        state.putParcelable(TEST_PARCEL, customParcel)
        return state
    }

    override fun onRestoreState(savedState: Bundle) {
        saveStateCount = savedState.getInt(STATE_SAVED_COUNT)
        customParcel = savedState.getParcelable(TEST_PARCEL)
    }
}

/**
 * [CustomTestParcelable] that helps testing bundled custom parcels
 */
data class CustomTestParcelable(val name: String?) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CustomTestParcelable> {
        override fun createFromParcel(parcel: Parcel) = CustomTestParcelable(parcel)

        override fun newArray(size: Int): Array<CustomTestParcelable?> = arrayOfNulls(size)
    }
}
