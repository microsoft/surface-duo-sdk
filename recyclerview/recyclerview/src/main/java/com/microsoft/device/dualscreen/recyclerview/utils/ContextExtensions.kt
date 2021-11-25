package com.microsoft.device.dualscreen.recyclerview.utils

import android.content.Context
import com.microsoft.device.dualscreen.utils.wm.getWindowRect
import com.microsoft.device.dualscreen.utils.wm.getWindowVisibleDisplayFrame

/**
 * Returns the navigation bar height
 */
internal val Context.navBarHeight: Int
    get() {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (hasNavBar && resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

/**
 * Returns [true] if the navigation bar is visible, [false] otherwise
 */
internal val Context.hasNavBar: Boolean
    get() {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

/**
 * Returns [true] if the navigation bar is at the bottom of the display area, [false] otherwise
 */
internal val Context.hasBottomNavBar: Boolean
    get() {
        val windowRect = getWindowRect()
        val windowVisibleDisplayFrame = getWindowVisibleDisplayFrame()
        return windowRect.width() == windowVisibleDisplayFrame.right
    }

/**
 * Returns [true] if the navigation bar is on the right side of the display area, [false] otherwise
 */
internal val Context.hasRightNavBar: Boolean
    get() {
        val windowRect = getWindowRect()
        val windowVisibleDisplayFrame = getWindowVisibleDisplayFrame()
        return windowRect.height() == windowVisibleDisplayFrame.bottom
    }