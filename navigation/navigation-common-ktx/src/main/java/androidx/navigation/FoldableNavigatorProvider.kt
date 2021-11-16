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

@file:Suppress("NOTHING_TO_INLINE")

package androidx.navigation

import kotlin.reflect.KClass

/**
 * Retrieves a registered [FoldableNavigator] by name.
 *
 * @throws IllegalStateException if the FoldableNavigator has not been added
 */
inline operator fun <T : FoldableNavigator<out FoldableNavDestination>> FoldableNavigatorProvider.get(name: String): T =
    getNavigator(name)

/**
 * Retrieves a registered [FoldableNavigator] using the name provided by the
 * [FoldableNavigator.Name annotation][FoldableNavigator.Name].
 *
 * @throws IllegalStateException if the FoldableNavigator has not been added
 */
inline operator fun <T : FoldableNavigator<out FoldableNavDestination>> FoldableNavigatorProvider.get(
    clazz: KClass<T>
): T = getNavigator(clazz.java)

/**
 * Register a [FoldableNavigator] by name. If a navigator by this name is already
 * registered, this new navigator will replace it.
 *
 * @return the previously added [FoldableNavigator] for the given name, if any
 */
inline operator fun FoldableNavigatorProvider.set(
    name: String,
    navigator: FoldableNavigator<out FoldableNavDestination>
) = addNavigator(name, navigator)

/**
 * Register a navigator using the name provided by the
 * [FoldableNavigator.Name annotation][FoldableNavigator.Name].
 */
inline operator fun FoldableNavigatorProvider.plusAssign(navigator: FoldableNavigator<out FoldableNavDestination>) {
    addNavigator(navigator)
}