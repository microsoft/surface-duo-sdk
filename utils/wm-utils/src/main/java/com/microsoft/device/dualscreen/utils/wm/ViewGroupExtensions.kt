/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat

/**
 * Create a drawable where 1/2 is transparent and 1/2 is the initial view background.
 * Used when there are no buttons on one screen.
 */
fun ViewGroup.createHalfTransparentBackground(
    initialBackground: Drawable? = null,
    displayPosition: DisplayPosition,
    hinge: Rect?,
    totalScreenWidth: Int
): Drawable? {
    hinge?.let {
        val transparentDrawable =
            ContextCompat.getDrawable(context, R.drawable.background_transparent)
        val finalBackground = LayerDrawable(arrayOf(initialBackground, transparentDrawable))

        if (displayPosition == DisplayPosition.START) {
            finalBackground.setLayerInset(0, 0, 0, totalScreenWidth - it.left, 0)
            finalBackground.setLayerInset(1, it.left, 0, 0, 0)
        }

        if (displayPosition == DisplayPosition.END) {
            finalBackground.setLayerInset(0, it.right, 0, 0, 0)
            finalBackground.setLayerInset(1, 0, 0, totalScreenWidth - it.left, 0)
        }
        return finalBackground
    }
    return initialBackground
}