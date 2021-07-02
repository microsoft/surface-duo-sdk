/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.isSurfaceDuoInDualMode

/**
 * Class that provides a LinearLayoutManager when the device is in single screen mode and a GridLayoutManager when the device is in spanned mode.
 */
class SurfaceDuoLayoutManager(context: Context, screenInfo: ScreenInfo) {
    companion object {
        const val SPAN_COUNT = 2
    }

    private var layoutManager: RecyclerView.LayoutManager? = null
    fun get(): RecyclerView.LayoutManager? {
        return layoutManager
    }

    init {
        layoutManager = if (isSurfaceDuoInDualMode(screenInfo) && screenInfo.isDeviceInLandscape()) {
            GridLayoutManager(context, SPAN_COUNT)
        } else {
            LinearLayoutManager(context)
        }
    }
}
