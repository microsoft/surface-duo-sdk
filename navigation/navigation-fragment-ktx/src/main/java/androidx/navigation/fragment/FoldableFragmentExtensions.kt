/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package androidx.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.FoldableNavController

/**
 * Find a [FoldableNavController] associated with a [Fragment].
 *
 * Calling this on a View not within a [FoldableNavHost] will result in an
 * [IllegalStateException]
 *
 * @throws [IllegalStateException]
 */
fun Fragment.findFoldableNavController(): FoldableNavController =
    FoldableNavHostFragment.findNavController(this)