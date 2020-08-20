/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.duolayoutmanager

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.core.ScreenHelper.Companion.isDeviceSurfaceDuo
import com.microsoft.device.dualscreen.core.ScreenHelper.Companion.isDualMode

/**
 * Class that provides a LinearLayoutManager when the device is in single screen mode and a GridLayoutManager when the device is in spanned mode.
 */
class DuoLayoutManager(activity: AppCompatActivity) {
    companion object {
        const val SPAN_COUNT = 2
    }

    private var layoutManager: RecyclerView.LayoutManager? = null
    fun get(): RecyclerView.LayoutManager? {
        return layoutManager
    }

    init {
        layoutManager = if (isDeviceSurfaceDuo(activity) && isDualMode(activity)) {
            GridLayoutManager(activity, SPAN_COUNT)
        } else {
            LinearLayoutManager(activity)
        }
    }
}
