/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach

/**
 * Performs the given action when root view is attached to a window. If the root view is already
 * attached to a window the action will be performed immediately, otherwise the
 * action will be performed after the root view is next attached.
 *
 * The action will only be invoked once, and any listeners will then be removed.
 *
 * @see doOnDetach
 */
inline fun Activity?.doOnAttach(crossinline action: (activity: Activity) -> Unit) {
    this?.run {
        val rootView = window?.decorView?.rootView

        rootView?.doOnAttach {
            action.invoke(this)
        }
    }
}

val Activity.isAttachedToWindow: Boolean
    get() = window?.decorView?.rootView?.isAttachedToWindow == true

val Activity.isTopActivity: Boolean
    get() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val taskInfo = activityManager.getRunningTasks(1)
        val topActivity = taskInfo[0].topActivity
        return topActivity?.className == javaClass.canonicalName
    }