/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.core

import android.content.Context

/**
 * Extension function for the Context class that will check if the application is running on a SurfaceDuo device and it is spanned on both screens.
 */
fun Context.isSurfaceDuoInDualMode(): Boolean {
    return ScreenHelper.isDeviceSurfaceDuo(this) && ScreenHelper.isDualMode(this)
}