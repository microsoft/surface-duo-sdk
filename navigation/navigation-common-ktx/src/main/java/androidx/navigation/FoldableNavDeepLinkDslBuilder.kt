/*
 * Copyright 2020 The Android Open Source Project
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

/**
 * Construct a new [NavDeepLink]
 */
fun foldableNavDeepLink(deepLinkBuilder: FoldableNavDeepLinkDslBuilder.() -> Unit): NavDeepLink =
    FoldableNavDeepLinkDslBuilder().apply(deepLinkBuilder).build()

/**
 * DSL for constructing a new [NavDeepLink]
 */
@NavDeepLinkDsl
class FoldableNavDeepLinkDslBuilder {
    private val builder = NavDeepLink.Builder()

    /**
     * The uri pattern of the deep link
     */
    var uriPattern: String? = null

    /**
     * Intent action for the deep link
     *
     * @throws IllegalArgumentException if attempting to set to empty.
     */
    var action: String? = null
        set(p) {
            if (p != null && p.isEmpty()) {
                throw IllegalArgumentException("The NavDeepLink cannot have an empty action.")
            }
            field = p
        }

    /**
     * MimeType for the deep link
     */
    var mimeType: String? = null

    internal fun build() = builder.apply {
        check(!(uriPattern == null && action == null && mimeType == null)) {
            ("The NavDeepLink must have an uri, action, and/or mimeType.")
        }
        uriPattern?.let { setUriPattern(it) }
        action?.let { setAction(it) }
        mimeType?.let { setMimeType(it) }
    }.build()
}