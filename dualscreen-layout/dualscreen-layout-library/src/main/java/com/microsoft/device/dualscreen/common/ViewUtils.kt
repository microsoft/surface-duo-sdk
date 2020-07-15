/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.common

import android.content.res.Configuration
import android.view.View
import com.microsoft.device.dualscreen.layout.ScreenHelper

fun View.isSpannedInDualScreen(screenMode: ScreenMode): Boolean {
    return if (isInEditMode) {
        screenMode == ScreenMode.DUAL_SCREEN
    } else {
        ScreenHelper.isDeviceSurfaceDuo(context) && ScreenHelper.isDualMode(context)
    }
}

fun View.isPortrait() =
    resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
