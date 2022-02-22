/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package androidx.navigation

import android.view.View

/**
 * Find a [FoldableNavController] associated with a [View].
 *
 * Calling this on a View not within a [FoldableNavHost] will result in an
 * [IllegalStateException]
 *
 * @throws [IllegalStateException]
 */
fun View.findFoldableNavController(): FoldableNavController =
    FoldableNavigation.findNavController(this)
