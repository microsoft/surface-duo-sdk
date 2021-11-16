/*
 * Copyright 2019 The Android Open Source Project
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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.test.R
import androidx.navigation.testutils.TestNavigator
import androidx.navigation.testutils.test
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class FoldableNavBackStackEntryTest {

    @UiThreadTest
    @Test
    fun testGetViewModelStoreOwner() {
        val navController = createNavController()
        navController.setViewModelStore(ViewModelStore())
        val navGraph = navController.navigatorProvider.navigation(
            id = 1,
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, null)

        val owner = navController.getViewModelStoreOwner(navGraph.id)
        assertThat(owner).isNotNull()
        val store = owner.viewModelStore
        assertThat(store).isNotNull()
    }

    @UiThreadTest
    @Test
    fun testGetViewModelStoreOwnerAndroidViewModel() {
        val navController = createNavController()
        navController.setViewModelStore(ViewModelStore())
        val navGraph = navController.navigatorProvider.navigation(
            id = 1,
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, null)

        val owner = navController.getViewModelStoreOwner(navGraph.id)
        assertThat(owner).isNotNull()
        val viewModelProvider = ViewModelProvider(owner)
        val viewModel = viewModelProvider[TestAndroidViewModel::class.java]
        assertThat(viewModel).isNotNull()
    }

    @UiThreadTest
    @Test
    fun testGetViewModelStoreOwnerSavedStateViewModel() {
        val hostStore = ViewModelStore()
        val navController = createNavController()
        navController.setViewModelStore(hostStore)
        val navGraph = navController.navigatorProvider.navigation(
            id = 1,
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, null)

        val owner = navController.getViewModelStoreOwner(navGraph.id)
        assertThat(owner).isNotNull()
        val viewModelProvider = ViewModelProvider(owner)
        val viewModel = viewModelProvider[TestSavedStateViewModel::class.java]
        assertThat(viewModel).isNotNull()
        viewModel.savedStateHandle.set("test", "test")

        val savedState = navController.saveState()
        val restoredNavController = createNavController()
        restoredNavController.setViewModelStore(hostStore)
        restoredNavController.restoreState(savedState)
        restoredNavController.graph = navGraph

        val restoredOwner = navController.getViewModelStoreOwner(navGraph.id)
        val restoredViewModel = ViewModelProvider(
            restoredOwner
        )[TestSavedStateViewModel::class.java]
        val restoredState: String? = restoredViewModel.savedStateHandle.get("test")
        assertThat(restoredState).isEqualTo("test")
    }

    @UiThreadTest
    @Test
    fun testSaveRestoreGetViewModelStoreOwner() {
        val hostStore = ViewModelStore()
        val navController = createNavController()
        navController.setViewModelStore(hostStore)
        val navGraph = navController.navigatorProvider.navigation(
            id = 1,
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, null)

        val store = navController.getViewModelStoreOwner(navGraph.id).viewModelStore
        assertThat(store).isNotNull()

        val savedState = navController.saveState()
        val restoredNavController = createNavController()
        restoredNavController.setViewModelStore(hostStore)
        restoredNavController.restoreState(savedState)
        restoredNavController.graph = navGraph

        assertWithMessage("Restored FoldableNavController should return the same ViewModelStore")
            .that(restoredNavController.getViewModelStoreOwner(navGraph.id).viewModelStore)
            .isSameInstanceAs(store)
    }

    @UiThreadTest
    @Test
    fun testGetViewModelStoreOwnerNoGraph() {
        val navController = createNavController()
        navController.setViewModelStore(ViewModelStore())
        val navGraphId = 1

        try {
            navController.getViewModelStoreOwner(navGraphId)
            fail(
                "Attempting to get ViewModelStoreOwner for navGraph not on back stack should " +
                    "throw IllegalArgumentException"
            )
        } catch (e: IllegalArgumentException) {
            assertThat(e)
                .hasMessageThat().contains(
                    "No destination with ID $navGraphId is on the FoldableNavController's back stack"
                )
        }
    }

    @UiThreadTest
    @Test
    fun testGetViewModelStoreOwnerSameGraph() {
        val navController = createNavController()
        navController.setViewModelStore(ViewModelStore())
        val provider = navController.navigatorProvider
        val graph = provider.navigation(1, startDestination = 2) {
            navigation(2, startDestination = 3) {
                test(3)
            }
        }

        navController.setGraph(graph, null)
        val owner = navController.getViewModelStoreOwner(graph.id)
        assertThat(owner).isNotNull()
        val viewStore = owner.viewModelStore
        assertThat(viewStore).isNotNull()

        val sameGraphOwner = navController.getViewModelStoreOwner(graph.id)
        assertThat(sameGraphOwner).isSameInstanceAs(owner)
        assertThat(sameGraphOwner.viewModelStore).isSameInstanceAs(viewStore)
    }

    @UiThreadTest
    @Test
    fun testGetSavedStateHandleRestored() {
        val hostStore = ViewModelStore()
        val navController = createNavController()
        navController.setViewModelStore(ViewModelStore())
        val navGraph = navController.navigatorProvider.navigation(
            id = 1,
            startDestination = R.id.start_test
        ) {
            test(R.id.start_test)
        }
        navController.setGraph(navGraph, null)

        val key = "test"
        val result = "success"
        navController.currentBackStackEntry?.savedStateHandle?.set(key, result)

        val savedState = navController.saveState()
        val restoredNavController = createNavController()
        restoredNavController.setViewModelStore(hostStore)
        restoredNavController.restoreState(savedState)
        restoredNavController.graph = navGraph

        val restoredSavedStateHandle = restoredNavController.currentBackStackEntry?.savedStateHandle
        val restoredResult: String? = restoredSavedStateHandle?.get(key)
        assertWithMessage("Restored SavedStateHandle should still have the result")
            .that(restoredResult).isEqualTo(result)
    }

    @UiThreadTest
    @Test
    fun testGetSavedStateHandle() {
        val entry = FoldableNavBackStackEntry(
            ApplicationProvider.getApplicationContext(),
            FoldableNavDestination(TestNavigator()),
            null,
            null,
            NavControllerViewModel()
        )

        assertThat(entry.savedStateHandle).isNotNull()
    }

    @UiThreadTest
    @Test
    fun testGetSavedStateHandleNoViewModelSet() {
        val entry = FoldableNavBackStackEntry(
            ApplicationProvider.getApplicationContext(),
            FoldableNavDestination(TestNavigator()),
            null,
            null,
            null
        )

        try {
            entry.savedStateHandle
            fail(
                "Attempting to get SavedStateHandle for back stack entry without " +
                    "navControllerViewModel set should throw IllegalStateException"
            )
        } catch (e: IllegalStateException) {
            assertThat(e)
                .hasMessageThat().contains(
                    "You must call setViewModelStore() on your FoldableNavHostController before " +
                        "accessing the ViewModelStore of a navigation graph."
                )
        }
    }

    private fun createNavController(): FoldableNavController {
        val navController = FoldableNavController(ApplicationProvider.getApplicationContext())
        val navigator = TestNavigator()
        navController.navigatorProvider.addNavigator(navigator)
        return navController
    }
}

class TestAndroidViewModel(application: Application) : AndroidViewModel(application)

class TestSavedStateViewModel(val savedStateHandle: SavedStateHandle) : ViewModel()
