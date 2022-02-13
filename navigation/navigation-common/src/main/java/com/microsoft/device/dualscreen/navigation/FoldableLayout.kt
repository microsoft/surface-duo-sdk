/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.navigation

import com.microsoft.device.dualscreen.layouts.FoldableLayout
import com.microsoft.device.dualscreen.utils.wm.ScreenMode

/**
 * Change the configuration using the given [LaunchScreen] and [ScreenMode]
 * @param params used to determine the new configuration
 */
fun FoldableLayout.changeConfiguration(params: RequestConfigParams) {
    val hasSingleContainer = params.launchScreen == LaunchScreen.BOTH
    if (params.screenMode == ScreenMode.DUAL_SCREEN && hasSingleContainer) {
        if (this.hasSingleContainer) {
            return
        }

        updateConfigCreator().apply {
            isDualLandscapeSingleContainer(hasSingleContainer)
            isDualPortraitSingleContainer(hasSingleContainer)
        }.reInflate()
    } else {
        if (isInScreenMode(params.screenMode)) {
            return
        }

        updateConfigCreator().apply {
            isDualLandscapeSingleContainer(false)
            isDualPortraitSingleContainer(false)
        }.reInflate()
    }
}

/**
 * Returns the current [ScreenMode]
 */
private val FoldableLayout.screenMode: ScreenMode
    get() = when (childCount) {
        3 -> ScreenMode.DUAL_SCREEN
        else -> ScreenMode.SINGLE_SCREEN
    }

/**
 * Check if the [FoldableLayout] has the same [ScreenMode] as the given param
 */
private fun FoldableLayout.isInScreenMode(screenMode: ScreenMode): Boolean {
    return screenMode == this.screenMode
}

/**
 * Returns [true] if the current configuration has only one container, [false] otherwise
 */
private val FoldableLayout.hasSingleContainer: Boolean
    get() = currentConfiguration.isDualLandscapeSingleContainer &&
        currentConfiguration.isDualPortraitSingleContainer
