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
 * Retrieve the overall visible display size in which the window this view is attached to has been positioned in.
 * This takes into account screen decorations above the window,
 * for both cases where the window itself is being position inside of them or the window is being placed
 * under then and covered insets are used for the window to position its content inside.
 * In effect, this tells you the available area where content can be placed and remain visible to users.
 */
fun Context.getWindowVisibleDisplayFrame(): Rect {
    val activity = getActivityFromContext()
    return activity?.let {
        val visibleFrame = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(visibleFrame)
        visibleFrame
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