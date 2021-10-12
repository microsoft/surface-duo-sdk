/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.utils.wm.ScreenPosition
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.dualscreen.utils.wm.isInDualMode

/**
 * Class that provides a [LinearLayoutManager] when the device is in single screen mode and a [GridLayoutManager] when the device is in dual mode.
 * Should be used together with the [FoldableItemDecoration]
 */
class FoldableLayoutManager(context: Context, windowLayoutInfo: WindowLayoutInfo) {

    private var layoutManager: RecyclerView.LayoutManager? = null
    fun get(): RecyclerView.LayoutManager? {
        return layoutManager
    }

    init {
        layoutManager =
            if (windowLayoutInfo.isInDualMode() && windowLayoutInfo.isFoldingFeatureVertical()) {
                GridLayoutManager(
                    context,
                    ScreenPosition.values().size
                )
            } else {
                LinearLayoutManager(context)
            }
    }
}
