/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.graphics.Rect
import androidx.window.layout.FoldingFeature

/**
 * On a foldable device will return the hinge position as a [Rect].
 * If the device is not foldable or it doesn't have a hinge, will return null.
 */
fun FoldingFeature.getHinge(): Rect {
    return bounds
}

/**
 * Checks if the application is spanned on both screens.
 * @return true if is spanned or false if is not spanned or the device doesn't have to displays
 */
fun FoldingFeature?.isSpanned(): Boolean {
    return this?.let { feature ->
        feature.state == FoldingFeature.State.FLAT && feature.isSeparating
    } ?: kotlin.run { false }
}

/**
 * Checks the position of the device.
 * @return true if the device is in the default portrait position with the screens side by side.
 * If the device is rotated and the displays are one on top of the other but the orientation is locked on portrait, it will still return true.
 * If the device is not foldable will return false.
 */
fun FoldingFeature?.areScreensSideBySide(): Boolean {
    return this?.let {
        bounds.height() > bounds.width()
    } ?: kotlin.run { false }
}

/**
 * Checks if the application is spanned and the displays are side by side.
 * @return true if the application is spanned on both screens and the displays are side by side or false otherwise.
 * If the device is rotated and the displays are one on top of the other but the orientation is locked on portrait, it will still return true.
 * If the device is not foldable will return false.
 */
fun FoldingFeature?.isSpannedHorizontally(): Boolean {
    return isSpanned() && areScreensSideBySide()
}
