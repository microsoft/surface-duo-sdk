/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.duolayoutmanager

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.isDualMode

/**
 * An item decorator that adds spacing for the cells to cover the device hinge when the application is in spanned mode.
 */
class DuoItemDecoration(
    val innerSpacing: Int = 0
) : RecyclerView.ItemDecoration() {
    private val SPAN_COUNT: Int = 2

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (!view.isDualMode()) {
            return
        }

        ScreenHelper.getHinge(view.context)?.let {
            val hingeWidth = it.right - it.left
            val position = parent.getChildAdapterPosition(view)
            val column = position % SPAN_COUNT

            if (column == 0) {
                outRect.right += hingeWidth / 2
            }
            if (column == 1) {
                outRect.left += hingeWidth / 2
            }
        }
    }
}