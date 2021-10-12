/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo

/**
 * Checks whether there are any display features attached
 * @return false if display features list is empty, true otherwise
 */
fun WindowLayoutInfo?.isInDualMode(): Boolean = this != null && displayFeatures.isNotEmpty()

/**
 * Checks whether the orientation of the folding feature is vertical
 * @return true if the folding feature exists and is vertically oriented, false otherwise
 */
fun WindowLayoutInfo?.isFoldingFeatureVertical(): Boolean =
    this != null &&
        (displayFeatures.firstOrNull() as? FoldingFeature)?.orientation == FoldingFeature.Orientation.VERTICAL

/**
 * Returns the first [FoldingFeature] from the [WindowLayoutInfo] or null if no [FoldingFeature] exists.
 * @return The first [FoldingFeature] if it exists
 */
fun WindowLayoutInfo?.getFoldingFeature(): FoldingFeature? {
    return if (this == null || displayFeatures.isEmpty()) {
        null
    } else {
        displayFeatures
            .firstOrNull { feature -> feature is FoldingFeature } as? FoldingFeature
    }
}

/**
 * Returns coordinates for folding feature location
 * @return [Rect] object with folding feature coordinates or empty Rect if no display features are found
 */
fun WindowLayoutInfo?.extractFoldingFeatureRect(): Rect =
    if (this == null || displayFeatures.isEmpty()) {
        Rect(0, 0, 0, 0)
    } else {
        displayFeatures.first().bounds
    }
