/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.content.Context

/**
 * Check if the device is SurfaceDuo
 * @return [true] if the device is SurfaceDuo, false otherwise
 */
fun Context.isSurfaceDuoDevice(): Boolean {
    val feature = "com.microsoft.device.display.displaymask"
    return packageManager.hasSystemFeature(feature)
}