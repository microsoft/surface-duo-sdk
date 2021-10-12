/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.utils

import androidx.recyclerview.widget.RecyclerView

/**
 * Utility function which replaces the [RecyclerView.ItemDecoration] at the specific index
 */
fun RecyclerView.replaceItemDecorationAt(decor: RecyclerView.ItemDecoration, index: Int = 0) {
    if (itemDecorationCount >= 1) {
        removeItemDecorationAt(index)
    }
    addItemDecoration(decor, index)
}
