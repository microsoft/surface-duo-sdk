/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation.utils

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.microsoft.device.dualscreen.testing.CurrentActivityDelegate

inline fun FragmentActivity.runWithBackStackListener(
    backStackListener: FragmentManager.OnBackStackChangedListener,
    testBlock: FragmentManager.() -> Unit
) {
    supportFragmentManager.addOnBackStackChangedListener(backStackListener)
    testBlock.invoke(supportFragmentManager)
    supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
}

inline fun CurrentActivityDelegate.runWithBackStackListener(
    backStackListener: FragmentManager.OnBackStackChangedListener,
    crossinline testBlock: FragmentManager.() -> Unit
) {
    currentActivity?.runWithBackStackListener(backStackListener, testBlock)
}