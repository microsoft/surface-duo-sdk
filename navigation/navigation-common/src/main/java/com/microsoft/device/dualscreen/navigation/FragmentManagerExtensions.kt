/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * Last added fragment from fragment manager.
 */
val FragmentManager.topFragment: Fragment?
    get() {
        return if (fragments.isNotEmpty()) fragments.last() else null
    }

/**
 * Removes fragments from fragment manager until predicate condition will be true.
 * @param predicate the predicate condition.
 */
fun FragmentManager.popBackStackUntil(predicate: (Fragment?) -> Boolean) {
    while (!predicate(topFragment)) {
        popBackStackImmediate()
    }
}

/**
 * Fragment tag used by fragment manager when fragments are added to stack.
 */
val Fragment.TAG: String
    get() {
        return this.javaClass.name
    }

/**
 * Runs a block of code inside [FragmentTransaction]
 */
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
    val fragmentTransaction = beginTransaction()
    fragmentTransaction.func()
    fragmentTransaction.commit()
}

/**
 * @return [true] if the pop operation is possible on dual screen mode, [false] otherwise
 */
internal fun FragmentManager.isPopOnDualScreenPossible(): Boolean = fragments.size >= 2