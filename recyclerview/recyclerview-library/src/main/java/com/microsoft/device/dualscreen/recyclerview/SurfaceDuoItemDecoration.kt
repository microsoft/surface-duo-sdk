/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.isSurfaceDuoInDualMode
import com.microsoft.device.dualscreen.recyclerview.SurfaceDuoLayoutManager.Companion.SPAN_COUNT

/**
 * An item decorator that adds spacing for the cells to cover the device hinge when the application is in spanned mode.
 */
class SurfaceDuoItemDecoration(private val screenInfo: ScreenInfo) : RecyclerView.ItemDecoration() {
    enum class ScreenPosition(val index: Int) {
        START_SCREEN(0),
        END_SCREEN(1)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (!isSurfaceDuoInDualMode(screenInfo)) {
            return
        }

        screenInfo.getHinge()?.let {
            val hingeWidth = it.right - it.left
            val position = parent.getChildAdapterPosition(view)
            val column = position % SPAN_COUNT

            if (column == ScreenPosition.START_SCREEN.index) {
                outRect.right += hingeWidth / 2
            }
            if (column == ScreenPosition.END_SCREEN.index) {
                outRect.left += hingeWidth / 2
            }
        }
    }
}