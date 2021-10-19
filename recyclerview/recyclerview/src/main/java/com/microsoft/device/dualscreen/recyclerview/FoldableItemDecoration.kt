/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.utils.wm.ScreenPosition
import com.microsoft.device.dualscreen.utils.wm.extractFoldingFeatureRect
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.dualscreen.utils.wm.isInDualMode

/**
 * An [RecyclerView.ItemDecoration] that adds spacing for the cells to cover the device folding feature when the application is in dual mode.
 * Should be used together with the [FoldableLayoutManager]
 */
class FoldableItemDecoration(
    private val windowLayoutInfo: WindowLayoutInfo
) : RecyclerView.ItemDecoration() {

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
            val position = parent.getChildAdapterPosition(view)

            when (position % ScreenPosition.values().size) {
                ScreenPosition.START_SCREEN.index -> outRect.right += hingeWidth / 2
                ScreenPosition.END_SCREEN.index -> outRect.left += hingeWidth / 2
            }
        }
    }
}
