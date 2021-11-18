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

@file:Suppress("NOTHING_TO_INLINE")

package androidx.navigation.test

import androidx.annotation.IdRes
import androidx.navigation.FloatingWindow
import androidx.navigation.FoldableNavDestinationBuilder
import androidx.navigation.FoldableNavGraphBuilder
import androidx.navigation.FoldableNavigator
import androidx.navigation.NavDestinationDsl
import androidx.navigation.get
import androidx.navigation.testutils.TestNavigator

@FoldableNavigator.Name("dialog")
class FloatingTestNavigator : TestNavigator() {
    override fun createDestination(): Destination {
        return FloatingDestination(this)
    }

    class FloatingDestination(navigator: TestNavigator) :
        Destination(navigator),
        FloatingWindow
}

/**
 * Construct a new [TestNavigator.Destination] from a [FloatingTestNavigator].
 */
inline fun FoldableNavGraphBuilder.dialog(@IdRes id: Int) = dialog(id) {}

/**
 * Construct a new [TestNavigator.Destination] from a [FloatingTestNavigator].
 */
inline fun FoldableNavGraphBuilder.dialog(
    @IdRes id: Int,
    builder: FloatingTestNavigatorDestinationBuilder.() -> Unit
) = destination(
    FloatingTestNavigatorDestinationBuilder(
        provider[FloatingTestNavigator::class],
        id
    ).apply(builder)
)

/**
 * DSL for constructing a new [TestNavigator.Destination] from a [FloatingTestNavigator].
 */
@NavDestinationDsl
class FloatingTestNavigatorDestinationBuilder(
    navigator: FloatingTestNavigator,
    @IdRes id: Int
) : FoldableNavDestinationBuilder<TestNavigator.Destination>(navigator, id)
