/*
 * Copyright 2018 The Android Open Source Project
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
import androidx.annotation.IdRes
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
@RunWith(JUnit4::class)
class FoldableNavGraphNavigatorTest {
    companion object {
        @IdRes
        private const val FIRST_DESTINATION_ID = 1
        @IdRes
        private const val SECOND_DESTINATION_ID = 2
    }
    private lateinit var provider: FoldableNavigatorProvider
    private lateinit var noOpNavigator: FoldableNoOpNavigator
    private lateinit var navGraphNavigator: FoldableNavGraphNavigator
    @Before
    fun setup() {
        provider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNoOpNavigator().also { noOpNavigator = it })
            addNavigator(
                FoldableNavGraphNavigator(this).also {
                    navGraphNavigator = it
                }
            )
        }
    }
    private fun createFirstDestination() = noOpNavigator.createDestination().apply {
        id = FIRST_DESTINATION_ID
    }
    private fun createSecondDestination() = noOpNavigator.createDestination().apply {
        id = SECOND_DESTINATION_ID
    }
    private fun createGraphWithDestination(
        destination: FoldableNavDestination,
        startId: Int = destination.id
    ) = navGraphNavigator.createDestination().apply {
        addDestination(destination)
        startDestination = startId
    }
    @Test(expected = IllegalStateException::class)
    fun navigateWithoutStartDestination() {
        val destination = createFirstDestination()
        val graph = navGraphNavigator.createDestination().apply {
            addDestination(destination)
            id = 2 // can't match id of first destination or the start destination
            startDestination = 0
        }
        navGraphNavigator.navigate(graph, null, null, null)
    }
    @Test
    fun navigate() {
        val destination = createFirstDestination()
        val graph = createGraphWithDestination(destination)
        assertThat(navGraphNavigator.navigate(graph, null, null, null))
            .isEqualTo(destination)
    }
    @Test
    fun navigateThenPop() {
        val destination = createFirstDestination()
        val graph = createGraphWithDestination(destination)
        assertThat(navGraphNavigator.navigate(graph, null, null, null))
            .isEqualTo(destination)
        val success = navGraphNavigator.popBackStack(true)
        assertWithMessage("popBackStack should return true")
            .that(success)
            .isTrue()
    }
    @Test
    fun navigateSingleTopOnEmptyStack() {
        val destination = createFirstDestination()
        val graph = createGraphWithDestination(destination)
        // singleTop should still show as added on an empty stack
        assertThat(
            navGraphNavigator.navigate(
                graph,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        ).isEqualTo(destination)
    }
    @Test
    fun navigateSingleTop() {
        val destination = createFirstDestination()
        val graph = createGraphWithDestination(destination)
        assertThat(navGraphNavigator.navigate(graph, null, null, null))
            .isEqualTo(destination)
        assertThat(
            navGraphNavigator.navigate(
                graph,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        ).isEqualTo(destination)
    }
    @Test
    fun navigateSingleTopNotTop() {
        val destination = createFirstDestination()
        val graph = createGraphWithDestination(destination)
        val secondDestination = createSecondDestination()
        val secondGraph = createGraphWithDestination(secondDestination).apply {
            id = SECOND_DESTINATION_ID
        }
        assertThat(navGraphNavigator.navigate(graph, null, null, null))
            .isEqualTo(destination)
        assertThat(
            navGraphNavigator.navigate(
                secondGraph,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        ).isEqualTo(secondDestination)
    }
    @Test
    fun navigateSingleTopNested() {
        val destination = createFirstDestination()
        val nestedGraph = createGraphWithDestination(destination).apply {
            id = FIRST_DESTINATION_ID
        }
        val graph = createGraphWithDestination(nestedGraph)
        assertThat(navGraphNavigator.navigate(graph, null, null, null))
            .isEqualTo(destination)
        assertThat(
            navGraphNavigator.navigate(
                graph,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        ).isEqualTo(destination)
    }
}