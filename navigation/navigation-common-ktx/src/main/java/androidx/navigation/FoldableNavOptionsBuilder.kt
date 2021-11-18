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
import com.microsoft.device.dualscreen.navigation.LaunchScreen

/**
 * Construct a new [FoldableNavOptions]
 */
fun foldableNavOptions(optionsBuilder: FoldableNavOptionsBuilder.() -> Unit): FoldableNavOptions =
    FoldableNavOptionsBuilder().apply(optionsBuilder).build()

/**
 * DSL for constructing a new [FoldableNavOptions]
 */
@NavOptionsDsl
class FoldableNavOptionsBuilder {
    private val builder = FoldableNavOptions.Builder()

    /**
     * Whether this navigation action should launch as single-top (i.e., there will be at most
     * one copy of a given destination on the top of the back stack).
     *
     * This functions similarly to how [android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP]
     * works with activites.
     */
    var launchSingleTop = false

    /**
     * Pop up to a given destination before navigating. This pops all non-matching destinations
     * from the back stack until this destination is found.
     */
    @IdRes
    var popUpTo: Int = -1
        set(value) {
            field = value
            inclusive = false
        }
    private var inclusive = false

    /**
     * Pop up to a given destination before navigating. This pops all non-matching destinations
     * from the back stack until this destination is found.
     */
    fun popUpTo(@IdRes id: Int, popUpToBuilder: PopUpToBuilder.() -> Unit) {
        popUpTo = id
        inclusive = PopUpToBuilder().apply(popUpToBuilder).inclusive
    }

    /**
     * Sets any custom Animation or Animator resources that should be used.
     *
     * Note: Animator resources are not supported for navigating to a new Activity
     */
    fun anim(animBuilder: AnimBuilder.() -> Unit) {
        AnimBuilder().apply(animBuilder).run {
            this@FoldableNavOptionsBuilder.builder.setEnterAnim(enter)
                .setExitAnim(exit)
                .setPopEnterAnim(popEnter)
                .setPopExitAnim(popExit)
        }
    }

    fun launchScreen(launchScreenBuilder: LaunchScreenBuilder.() -> Unit) {
        LaunchScreenBuilder().apply(launchScreenBuilder).run {
            this@FoldableNavOptionsBuilder.builder.setLaunchScreen(launchScreen)
        }
    }

    internal fun build() = builder.apply {
        setLaunchSingleTop(launchSingleTop)
        setPopUpTo(popUpTo, inclusive)
    }.build()
}

/**
 * DSL for customizing [FoldableNavOptionsBuilder.launchSingleTop] operations.
 */
@NavOptionsDsl
class LaunchScreenBuilder {
    /**
     * Whether the `popUpTo` destination should be popped from the back stack.
     */
    var launchScreen = LaunchScreen.DEFAULT
}
