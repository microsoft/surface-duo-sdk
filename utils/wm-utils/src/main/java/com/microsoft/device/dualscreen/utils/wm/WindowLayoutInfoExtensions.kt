/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.graphics.Rect
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo

/**
 * Checks whether there are any display features attached
 * @return false if display features list is empty, true otherwise
 */
fun WindowLayoutInfo.isInDualMode(): Boolean = displayFeatures.isNotEmpty()

/**
 * Checks whether the orientation of the folding feature is vertical
 * @return true if the folding feature exists and is vertically oriented, false otherwise
 */
fun WindowLayoutInfo.isFoldingFeatureVertical(): Boolean =
    (displayFeatures.firstOrNull() as? FoldingFeature)?.orientation == FoldingFeature.Orientation.VERTICAL

/**
 * Returns coordinates for folding feature location
 * @return [Rect] object with folding feature coordinates or empty Rect if no display features are found
 */
fun WindowLayoutInfo.extractFoldingFeature(): Rect =
    if (displayFeatures.isEmpty()) {
        Rect(0, 0, 0, 0)
    } else {
        displayFeatures.first().bounds
    }

/**
 * Returns coordinates for folding feature location
 * @return [Rect] object with folding feature coordinates or empty Rect if no display features are found
 */
fun WindowLayoutInfo.getFoldingFeature(): DisplayFeature? =
    displayFeatures.firstOrNull()