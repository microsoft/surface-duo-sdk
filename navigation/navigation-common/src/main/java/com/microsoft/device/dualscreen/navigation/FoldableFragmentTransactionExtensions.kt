/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.fragment.app.FragmentTransaction
import androidx.navigation.FoldableNavOptions

/**
 * Utility function that applies animations to a [FragmentTransaction]
 * @param navOptions structure that contains all animations
 */
internal fun FragmentTransaction.applyAnimations(navOptions: FoldableNavOptions?) {
    val enterAnim = navOptions?.enterAnim?.takeIf { it != -1 } ?: 0
    val exitAnim = navOptions?.exitAnim?.takeIf { it != -1 } ?: 0
    val popEnterAnim = navOptions?.popEnterAnim?.takeIf { it != -1 } ?: 0
    val popExitAnim = navOptions?.popExitAnim?.takeIf { it != -1 } ?: 0
    if (enterAnim != 0 || exitAnim != 0 || popEnterAnim != 0 || popExitAnim != 0) {
        setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
    }
}