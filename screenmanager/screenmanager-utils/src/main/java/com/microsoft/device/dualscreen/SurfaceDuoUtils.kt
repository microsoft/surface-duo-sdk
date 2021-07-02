/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

/**
 * Utility function that checks if the application
 * is running on a SurfaceDuo device and it is spanned on both screens.
 */
fun isSurfaceDuoInDualMode(screenInfo: ScreenInfo): Boolean {
    return screenInfo.isSurfaceDuoDevice() && screenInfo.isDualMode()
}