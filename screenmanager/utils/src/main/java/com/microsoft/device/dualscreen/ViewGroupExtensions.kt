/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.microsoft.device.dualscreen.utils.R

/**
 * Create a drawable where 1/2 is transparent and 1/2 is the initial view background.
 * Used when there are no buttons on one screen.
 */
fun ViewGroup.createHalfTransparentBackground(
    displayPosition: DisplayPosition,
    screenInfo: ScreenInfo?,
    initialBackground: Drawable? = null
): Drawable? {
    screenInfo?.getHinge()?.let {
        val singleScreenWidth = it.left
        val hingeWidth = it.right - it.left

        val transparentDrawable =
            ContextCompat.getDrawable(context, R.drawable.background_transparent)
        val finalBackground = LayerDrawable(arrayOf(initialBackground, transparentDrawable))

        if (displayPosition == DisplayPosition.START) {
            finalBackground.setLayerInset(0, 0, 0, singleScreenWidth + hingeWidth, 0)
            finalBackground.setLayerInset(1, singleScreenWidth, 0, 0, 0)
        }

        if (displayPosition == DisplayPosition.END) {
            finalBackground.setLayerInset(0, singleScreenWidth + hingeWidth, 0, 0, 0)
            finalBackground.setLayerInset(1, 0, 0, singleScreenWidth, 0)
        }
        return finalBackground
    }
    return initialBackground
}