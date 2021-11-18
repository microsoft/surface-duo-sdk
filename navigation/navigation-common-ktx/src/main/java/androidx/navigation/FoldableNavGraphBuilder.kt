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

/**
 * Construct a new [FoldableNavGraph]
 */
public inline fun FoldableNavigatorProvider.navigation(
    @IdRes id: Int = 0,
    @IdRes startDestination: Int,
    builder: FoldableNavGraphBuilder.() -> Unit
): FoldableNavGraph = FoldableNavGraphBuilder(this, id, startDestination).apply(builder).build()

/**
 * Construct a nested [FoldableNavGraph]
 */
public inline fun FoldableNavGraphBuilder.navigation(
    @IdRes id: Int,
    @IdRes startDestination: Int,
    builder: FoldableNavGraphBuilder.() -> Unit
): Unit = destination(FoldableNavGraphBuilder(provider, id, startDestination).apply(builder))

/**
 * DSL for constructing a new [FoldableNavGraph]
 */
@NavDestinationDsl
public open class FoldableNavGraphBuilder(
    public val provider: FoldableNavigatorProvider,
    @IdRes id: Int,
    @IdRes private var startDestination: Int
) : FoldableNavDestinationBuilder<FoldableNavGraph>(provider[FoldableNavGraphNavigator::class], id) {
    private val destinations = mutableListOf<FoldableNavDestination>()

    /**
     * Build and add a new destination to the [FoldableNavGraphBuilder]
     */
    public fun <D : FoldableNavDestination> destination(navDestination: FoldableNavDestinationBuilder<D>) {
        destinations += navDestination.build()
    }

    /**
     * Adds this destination to the [FoldableNavGraphBuilder]
     */
    public operator fun FoldableNavDestination.unaryPlus() {
        addDestination(this)
    }

    /**
     * Add the destination to the [FoldableNavGraphBuilder]
     */
    public fun addDestination(destination: FoldableNavDestination) {
        destinations += destination
    }

    override fun build(): FoldableNavGraph = super.build().also { navGraph ->
        navGraph.addDestinations(destinations)
        if (startDestination == 0) {
            throw IllegalStateException("You must set a startDestination")
        }
        navGraph.startDestination = startDestination
    }
}