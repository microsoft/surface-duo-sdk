/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.res.Configuration
import android.view.View

/**
 * Checks if the view is spanned in dual screen. If the view is in edit mode,
 * then this method will return [true] only when spanningMode param is [ScreenMode.DUAL_SCREEN]
 * @return [true] if the view is spanned to dual screen, otherwise [false]
 */
fun View.isSpannedInDualScreen(screenMode: ScreenMode, screenInfo: ScreenInfo): Boolean {
    return if (isInEditMode) {
        screenMode == ScreenMode.DUAL_SCREEN
    } else {
        isSurfaceDuoInDualMode(screenInfo)
    }
}

/**
 * Checks if some view has portrait orientation
 * @return [true], if the view has portrait orientation, [false] otherwise
 */
fun View.isPortrait() =
    resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT