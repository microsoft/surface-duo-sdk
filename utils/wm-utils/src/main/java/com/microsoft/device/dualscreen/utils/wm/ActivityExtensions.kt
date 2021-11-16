/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import androidx.window.layout.WindowMetricsCalculator

/**
 * Returns a [Rect] representing the total space the application is covering.
 */
fun Context.getWindowRect(): Rect {
    val activity = getActivityFromContext()
    return activity?.let {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(it)
        return windowMetrics.bounds
    } ?: Rect(0, 0, 0, 0)
}

/**
 * Casts a [Context] to an [Activity] if it is possible.
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