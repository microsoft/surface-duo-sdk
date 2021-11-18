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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
@SmallTest
@RunWith(AndroidJUnit4::class)
class FoldableNavGraphBuilderTest {
    private val provider = FoldableNavigatorProvider().apply {
        addNavigator(FoldableNavGraphNavigator(this))
        addNavigator(FoldableNoOpNavigator())
    }
    @Test
    fun navigation() {
        val graph = provider.navigation(startDestination = DESTINATION_ID) {
            navDestination(DESTINATION_ID) {}
        }
        assertWithMessage("Destination should be added to the graph")
            .that(DESTINATION_ID in graph)
            .isTrue()
    }
    @Test
    fun navigationUnaryPlus() {
        val graph = provider.navigation(startDestination = DESTINATION_ID) {
            +provider[FoldableNoOpNavigator::class].createDestination().apply {
                id = DESTINATION_ID
            }
        }
        assertWithMessage("Destination should be added to the graph")
            .that(DESTINATION_ID in graph)
            .isTrue()
    }
    @Test
    fun navigationAddDestination() {
        val graph = provider.navigation(startDestination = DESTINATION_ID) {
            val destination = provider[FoldableNoOpNavigator::class].createDestination().apply {
                id = DESTINATION_ID
            }
            addDestination(destination)
        }
        assertWithMessage("Destination should be added to the graph")
            .that(DESTINATION_ID in graph)
            .isTrue()
    }
    @Test(expected = IllegalStateException::class)
    fun navigationMissingStartDestination() {
        provider.navigation(startDestination = 0) {
            navDestination(DESTINATION_ID) {}
        }
        fail("NavGraph should throw IllegalStateException if startDestination is zero")
    }
    @Test
    fun navigationNested() {
        val graph = provider.navigation(startDestination = DESTINATION_ID) {
            navigation(DESTINATION_ID, startDestination = SECOND_DESTINATION_ID) {
                navDestination(SECOND_DESTINATION_ID) {}
            }
        }
        assertWithMessage("Destination should be added to the graph")
            .that(DESTINATION_ID in graph)
            .isTrue()
    }
}
private const val DESTINATION_ID = 1
private const val SECOND_DESTINATION_ID = 2
/**
 * Create a base NavDestination. Generally, only subtypes of NavDestination should be
 * added to a NavGraph (hence why this is not in the common-ktx library)
 */
fun FoldableNavGraphBuilder.navDestination(
    @IdRes id: Int,
    builder: FoldableNavDestinationBuilder<FoldableNavDestination>.() -> Unit
) = destination(FoldableNavDestinationBuilder(provider[FoldableNoOpNavigator::class], id).apply(builder))
