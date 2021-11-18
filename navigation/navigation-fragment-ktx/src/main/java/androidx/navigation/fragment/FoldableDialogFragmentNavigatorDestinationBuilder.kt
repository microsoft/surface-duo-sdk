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
package androidx.navigation.fragment
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.navigation.FoldableNavDestinationBuilder
import androidx.navigation.FoldableNavGraphBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.get
import kotlin.reflect.KClass

/**
 * Construct a new [FoldableDialogFragmentNavigator.Destination]
 */
public inline fun <reified F : DialogFragment> FoldableNavGraphBuilder.dialog(
    @IdRes id: Int
): Unit = dialog<F>(id) {}
/**
 * Construct a new [FoldableDialogFragmentNavigator.Destination]
 */
public inline fun <reified F : DialogFragment> FoldableNavGraphBuilder.dialog(
    @IdRes id: Int,
    builder: FoldableDialogFragmentNavigatorDestinationBuilder.() -> Unit
): Unit = destination(
    FoldableDialogFragmentNavigatorDestinationBuilder(
        provider[FoldableDialogFragmentNavigator::class],
        id,
        F::class
    ).apply(builder)
)
/**
 * DSL for constructing a new [FoldableDialogFragmentNavigator.Destination]
 */
@NavDestinationDsl
public class FoldableDialogFragmentNavigatorDestinationBuilder(
    navigator: FoldableDialogFragmentNavigator,
    @IdRes id: Int,
    private val fragmentClass: KClass<out DialogFragment>
) : FoldableNavDestinationBuilder<FoldableDialogFragmentNavigator.Destination>(navigator, id) {
    override fun build(): FoldableDialogFragmentNavigator.Destination =
        super.build().also { destination ->
            destination.className = fragmentClass.java.name
        }
}