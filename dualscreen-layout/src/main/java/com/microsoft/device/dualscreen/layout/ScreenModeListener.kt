/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

/**
 * Interface with two functions that will be called by the SurfaceDuoManager object
 * depending on the screen mode.
 */
interface ScreenModeListener {
    fun onSwitchToSingleScreenMode()
    fun onSwitchToDualScreenMode()
}
