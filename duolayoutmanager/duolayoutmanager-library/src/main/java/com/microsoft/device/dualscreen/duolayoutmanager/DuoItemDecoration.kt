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
import com.microsoft.device.dualscreen.duolayoutmanager.DuoLayoutManager.Companion.SPAN_COUNT

/**
 * An item decorator that adds spacing for the cells to cover the device hinge when the application is in spanned mode.
 */
class DuoItemDecoration : RecyclerView.ItemDecoration() {
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

            if (column == ScreenPosition.START_SCREEN.index) {
                outRect.right += hingeWidth / 2
            }
            if (column == ScreenPosition.END_SCREEN.index) {
                outRect.left += hingeWidth / 2
            }
        }
    }
}

enum class ScreenPosition(val index: Int) {
    START_SCREEN(0),
    END_SCREEN(1)
}