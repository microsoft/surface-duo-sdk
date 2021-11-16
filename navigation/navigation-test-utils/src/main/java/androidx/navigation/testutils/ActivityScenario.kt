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

package androidx.navigation.testutils

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario

/**
 * Run [block] using [ActivityScenario.onActivity], returning the result of the block.
 */
inline fun <reified A : Activity, T : Any> ActivityScenario<A>.withActivity(
    crossinline block: A.() -> T
): T {
    lateinit var value: T
    var err: Throwable? = null
    onActivity { activity ->
        try {
            value = block(activity)
        } catch (t: Throwable) {
            err = t
        }
    }
    err?.let { throw it }
    return value
}

/**
 * Run [block] using [ActivityScenario.onActivity], returning the result of the block.
 */
inline fun <reified A : FragmentActivity, T : Any> ActivityScenario<A>.withFragmentManager(
    backStackListener: FragmentManager.OnBackStackChangedListener,
    crossinline block: FragmentManager.() -> T
): T {
    lateinit var value: T
    var err: Throwable? = null
    onActivity { activity ->
        try {
            activity.supportFragmentManager.addOnBackStackChangedListener(backStackListener)
            value = block(activity.supportFragmentManager)
            activity.supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
        } catch (t: Throwable) {
            err = t
        }
    }
    err?.let { throw it }
    return value
}