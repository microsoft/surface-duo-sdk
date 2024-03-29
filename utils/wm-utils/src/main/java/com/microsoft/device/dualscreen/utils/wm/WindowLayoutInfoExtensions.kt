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
fun WindowLayoutInfo?.isInDualMode(): Boolean = this != null && displayFeatures.isNotEmpty()

/**
 * Checks whether the orientation of the folding feature is vertical
 * @return true if the folding feature exists and is vertically oriented, false otherwise
 */
fun WindowLayoutInfo?.isFoldingFeatureVertical(): Boolean =
    this != null &&
        (displayFeatures.firstOrNull() as? FoldingFeature)?.orientation == FoldingFeature.Orientation.VERTICAL

/**
 * Checks whether the orientation of the folding feature is horizontal
 * @return true if the folding feature exists and is horizontal oriented, false otherwise
 */
fun WindowLayoutInfo?.isFoldingFeatureHorizontal(): Boolean =
    this != null &&
        (displayFeatures.firstOrNull() as? FoldingFeature)?.orientation == FoldingFeature.Orientation.HORIZONTAL

/**
 * Checks whether the folding feature is causing the window to be split into multiple physical areas.
 * If tru the UI  may be split to avoid overlapping the folding feature.
 * @return true if the folding feature isSeparating
 */
fun WindowLayoutInfo?.isSeparating(): Boolean =
    this != null &&
        (displayFeatures.firstOrNull() as? FoldingFeature)?.isSeparating == true

/**
 * Returns the first [FoldingFeature] from the [WindowLayoutInfo] or null if no [FoldingFeature] exists.
 * @return The first [FoldingFeature] if it exists
 */
fun WindowLayoutInfo?.getFoldingFeature(): FoldingFeature? =
    if (this == null || displayFeatures.isEmpty()) {
        null
    } else {
        displayFeatures
            .firstOrNull { feature -> feature is FoldingFeature } as? FoldingFeature
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

/**
 * Returns [ScreenMode.DUAL_SCREEN] if there is any [DisplayFeature],
 * [ScreenMode.SINGLE_SCREEN] otherwise
 */
val WindowLayoutInfo.screenMode: ScreenMode
    get() {
        return if (displayFeatures.isNotEmpty()) {
            ScreenMode.DUAL_SCREEN
        } else {
            ScreenMode.SINGLE_SCREEN
        }
    }
