/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

package androidx.navigation

import androidx.annotation.IdRes

/**
 * Returns the destination with `id`.
 *
 * @throws IllegalArgumentException if no destination is found with that id.
 */
public inline operator fun FoldableNavGraph.get(@IdRes id: Int): FoldableNavDestination =
    findNode(id) ?: throw IllegalArgumentException("No destination for $id was found in $this")

/** Returns `true` if a destination with `id` is found in this navigation graph. */
public operator fun FoldableNavGraph.contains(@IdRes id: Int): Boolean = findNode(id) != null

/**
 * Adds a destination to this NavGraph. The destination must have an
 * [id][NavDestination.getId] set.
 *
 * The destination must not have a [parent][NavDestination.getParent] set. If
 * the destination is already part of a [NavGraph], call
 * [NavGraph.remove] before calling this method.</p>
 *
 * @param node destination to add
 */
public inline operator fun FoldableNavGraph.plusAssign(node: FoldableNavDestination) {
    addDestination(node)
}

/**
 * Add all destinations from another collection to this one. As each destination has at most
 * one parent, the destinations will be removed from the given NavGraph.
 *
 * @param other collection of destinations to add. All destinations will be removed from the
 * parameter graph after being added to this graph.
 */
public inline operator fun FoldableNavGraph.plusAssign(other: FoldableNavGraph) {
    addAll(other)
}

/** Removes `node` from this navigation graph. */
public inline operator fun FoldableNavGraph.minusAssign(node: FoldableNavDestination) {
    remove(node)
}