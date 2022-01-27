/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.test

import org.mockito.Mockito

fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T