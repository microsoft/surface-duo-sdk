/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import androidx.lifecycle.ViewModel
import androidx.window.layout.WindowLayoutInfo

/**
 * Internal ViewModel implementation used to store the screen info across application lifecycle
 */
internal class FoldableLayoutViewModel : ViewModel() {

    /**
     * Application screen state.
     */
    var windowLayoutInfo: WindowLayoutInfo? = null

    /**
     * Current [FoldableLayout] configuration
     */
    var layoutConfig: FoldableLayout.Config? = null
}