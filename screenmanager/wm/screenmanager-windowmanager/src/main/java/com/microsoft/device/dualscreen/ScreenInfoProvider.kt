/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.Context

/**
 * Utility class used to retrieve the current screen information.
 */
object ScreenInfoProvider {
    val version = Version.WindowManager

    /**
     * This method must be called after the view is attached to window.
     * @return the current screen info.
     */
    @JvmStatic
    fun getScreenInfo(context: Context): ScreenInfo {
        return ScreenInfoImpl(context)
    }
}