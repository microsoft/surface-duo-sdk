/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect

/**
 * Returns a [Rect] representing the total space the application is covering.
 */
fun Context.getWindowRect(): Rect {
    val windowRect = Rect()
    val activity = getActivityFromContext()
    activity?.windowManager?.defaultDisplay?.getRectSize(windowRect)
    return windowRect
}

/**
 *
 */
private fun Context.getActivityFromContext(): Activity? {
    var contextBuffer = this
    while (contextBuffer is ContextWrapper) {
        if (contextBuffer is Activity) {
            return contextBuffer
        }
        contextBuffer = contextBuffer.baseContext
    }
    return null
}