/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

/**
 * Interface for watching screen changes.
 */
interface ScreenInfoListener {
    /**
     * Called whenever the screen info was changed.
     * @param screenInfo object used to retrieve screen information
     */
    fun onScreenInfoChanged(screenInfo: ScreenInfo)
}