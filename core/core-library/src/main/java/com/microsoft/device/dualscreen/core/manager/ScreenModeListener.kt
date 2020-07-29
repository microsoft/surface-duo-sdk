/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.core.manager

interface ScreenModeListener {
    fun onSwitchToSingleScreen()
    fun onSwitchToDualScreen()
}