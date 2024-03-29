/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.recyclerview.utils.DeltaCalculator
import com.microsoft.device.dualscreen.utils.wm.ScreenPosition
import com.microsoft.device.dualscreen.utils.wm.extractFoldingFeatureRect
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.dualscreen.utils.wm.isInDualMode

/**
 * An [RecyclerView.ItemDecoration] that adds spacing for the cells to cover the device folding feature when the application is in dual mode.
 * Should be used together with the [FoldableStaggeredLayoutManager]
 */
class FoldableStaggeredItemDecoration(
    private val windowLayoutInfo: WindowLayoutInfo
) : RecyclerView.ItemDecoration() {
    private val deltaCalculator = DeltaCalculator()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (!windowLayoutInfo.isInDualMode() || !windowLayoutInfo.isFoldingFeatureVertical()) {
            return
        }

        windowLayoutInfo.extractFoldingFeatureRect().let {
            val hingeWidth = it.right - it.left
            val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

            when (layoutParams.spanIndex) {
                ScreenPosition.START_SCREEN.index ->
                    outRect.right += hingeWidth / 2 - deltaCalculator.delta(parent)
                ScreenPosition.END_SCREEN.index ->
                    outRect.left += hingeWidth / 2 + deltaCalculator.delta(parent)
            }
        }
    }
}
