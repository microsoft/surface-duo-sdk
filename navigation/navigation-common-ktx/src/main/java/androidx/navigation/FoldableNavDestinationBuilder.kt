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
import androidx.core.os.bundleOf

/**
 * DSL for constructing a new [FoldableNavDestination]
 */
@NavDestinationDsl
public open class FoldableNavDestinationBuilder<out D : FoldableNavDestination>(
    protected val navigator: FoldableNavigator<out D>,
    @IdRes public val id: Int
) {
    /**
     * The descriptive label of the destination
     */
    public var label: CharSequence? = null
    private var arguments = mutableMapOf<String, NavArgument>()

    /**
     * Add a [NavArgument] to this destination.
     */
    public fun argument(name: String, argumentBuilder: NavArgumentBuilder.() -> Unit) {
        arguments[name] = NavArgumentBuilder().apply(argumentBuilder).build()
    }

    private var deepLinks = mutableListOf<NavDeepLink>()

    /**
     * Add a deep link to this destination.
     *
     * In addition to a direct Uri match, the following features are supported:
     *
     * *    Uris without a scheme are assumed as http and https. For example,
     *      `www.example.com` will match `http://www.example.com` and
     *      `https://www.example.com`.
     * *    Placeholders in the form of `{placeholder_name}` matches 1 or more
     *      characters. The String value of the placeholder will be available in the arguments
     *      [Bundle] with a key of the same name. For example,
     *      `http://www.example.com/users/{id}` will match
     *      `http://www.example.com/users/4`.
     * *    The `.*` wildcard can be used to match 0 or more characters.
     *
     * @param uriPattern The uri pattern to add as a deep link
     * @see deepLink
     */
    public fun deepLink(uriPattern: String) {
        deepLinks.add(NavDeepLink(uriPattern))
    }

    /**
     * Add a deep link to this destination.
     *
     * In addition to a direct Uri match, the following features are supported:
     *
     * *    Uris without a scheme are assumed as http and https. For example,
     *      `www.example.com` will match `http://www.example.com` and
     *      `https://www.example.com`.
     * *    Placeholders in the form of `{placeholder_name}` matches 1 or more
     *      characters. The String value of the placeholder will be available in the arguments
     *      [Bundle] with a key of the same name. For example,
     *      `http://www.example.com/users/{id}` will match
     *      `http://www.example.com/users/4`.
     * *    The `.*` wildcard can be used to match 0 or more characters.
     *
     * @param navDeepLink the NavDeepLink to be added to this destination
     */
    public fun deepLink(navDeepLink: FoldableNavDeepLinkDslBuilder.() -> Unit) {
        deepLinks.add(FoldableNavDeepLinkDslBuilder().apply(navDeepLink).build())
    }

    private var actions = mutableMapOf<Int, FoldableNavAction>()

    /**
     * Adds a new [NavAction] to the destination
     */
    public fun action(actionId: Int, actionBuilder: FoldableNavActionBuilder.() -> Unit) {
        actions[actionId] = FoldableNavActionBuilder().apply(actionBuilder).build()
    }

    /**
     * Build the FoldableNavDestination by calling [Navigator.createDestination].
     */
    public open fun build(): D {
        return navigator.createDestination().also { destination ->
            destination.id = id
            destination.label = label
            arguments.forEach { (name, argument) ->
                destination.addArgument(name, argument)
            }
            deepLinks.forEach { deepLink ->
                destination.addDeepLink(deepLink)
            }
            actions.forEach { (actionId, action) ->
                destination.putAction(actionId, action)
            }
        }
    }
}

/**
 * DSL for building a [NavAction].
 */
@NavDestinationDsl
public class FoldableNavActionBuilder {
    /**
     * The ID of the destination that should be navigated to when this action is used
     */
    public var destinationId: Int = 0

    /**
     * The set of default arguments that should be passed to the destination. The keys
     * used here should be the same as those used on the [NavDestinationBuilder.argument]
     * for the destination.
     *
     * All values added here should be able to be added to a [android.os.Bundle].
     *
     * @see NavAction.getDefaultArguments
     */
    public val defaultArguments: MutableMap<String, Any?> = mutableMapOf()
    private var navOptions: FoldableNavOptions? = null

    /**
     * Sets the [NavOptions] for this action that should be used by default
     */
    public fun navOptions(optionsBuilder: FoldableNavOptionsBuilder.() -> Unit) {
        navOptions = FoldableNavOptionsBuilder().apply(optionsBuilder).build()
    }

    internal fun build() = FoldableNavAction(
        destinationId,
        navOptions,
        if (defaultArguments.isEmpty())
            null
        else
            bundleOf(*defaultArguments.toList().toTypedArray())
    )
}