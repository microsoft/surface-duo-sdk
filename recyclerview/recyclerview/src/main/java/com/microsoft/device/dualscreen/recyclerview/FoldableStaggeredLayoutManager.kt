/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.dualscreen.utils.wm.isInDualMode

/**
 * Class that provides a [LinearLayoutManager] when the device is in single screen mode and a [StaggeredGridLayoutManager] when the device is in dual mode.
 * Should be used together with the [FoldableStaggeredItemDecoration]
 */
class FoldableStaggeredLayoutManager(context: Context, windowLayoutInfo: WindowLayoutInfo) {

    private var layoutManager: RecyclerView.LayoutManager? = null
    fun get(): RecyclerView.LayoutManager? {
        return layoutManager
    }

    init {
        layoutManager =
            if (windowLayoutInfo.isInDualMode() && windowLayoutInfo.isFoldingFeatureVertical()) {
                StaggeredGridLayoutManager(
                    com.microsoft.device.dualscreen.utils.wm.ScreenPosition.values().size,
                    StaggeredGridLayoutManager.VERTICAL
                )
            } else {
                LinearLayoutManager(context)
            }
    }
}
