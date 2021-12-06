/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.utils

import android.view.View
import com.microsoft.device.dualscreen.utils.wm.getWindowRect

/**
 * Utility class used to calculate the RecyclerView item offset when it's not displayed on the entire display area,
 * for example when the navigation bar is displayed on the left or right,
 * or the device is in landscape and has the frontal camera inside the screen
 */
internal class DeltaCalculator {
    private var _delta = Int.MIN_VALUE
    fun delta(parent: View): Int {
        if (_delta == Int.MIN_VALUE) {
            _delta = with(parent.context) {
                val hasDifferentWidth = getWindowRect().width() != parent.measuredWidth
                when {
                    hasDifferentWidth && hasRightNavBar ->
                        2 * navBarHeight - getWindowRect().width() + parent.measuredWidth
                    hasDifferentWidth && hasBottomNavBar ->
                        parent.measuredWidth - getWindowRect().width()
                    else -> 0
                }
            } / 2
        }

        return _delta
    }
}