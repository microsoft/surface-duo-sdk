/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.navigation

import com.microsoft.device.dualscreen.utils.wm.ScreenMode

/**
 * Interface used by [FoldableFragmentManagerWrapper] when it needs to change the configuration
 * for the [FoldableLayout] used by [FoldableNavHostFragment] as a container view
 */
interface RequestConfigListener {
    /**
     * Called when the configuration for [FoldableLayout] should be changed
     * @param params used to determine the new configuration
     */
    fun changeConfiguration(params: RequestConfigParams)
}

/**
 * [FoldableLayout] configurations params
 */
data class RequestConfigParams(
    val launchScreen: LaunchScreen,
    val screenMode: ScreenMode
)