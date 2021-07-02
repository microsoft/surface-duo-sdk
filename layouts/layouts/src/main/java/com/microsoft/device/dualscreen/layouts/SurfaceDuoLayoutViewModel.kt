/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.lifecycle.ViewModel

/**
 * Internal ViewModel implementation used to store the screen info across application lifecycle
 */
internal class SurfaceDuoLayoutViewModel : ViewModel() {
    /**
     * Application screen state.
     */
    var screenState = ScreenSavedState()
}