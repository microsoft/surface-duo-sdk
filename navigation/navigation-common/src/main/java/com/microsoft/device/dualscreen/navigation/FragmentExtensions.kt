/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.common.R

/**
 * @return [true] if the view or parent view is null
 * or if the parent container id is the same with the start container id ([R.id.first_container_id]),
 * [false] otherwise.
 */
fun Fragment.isOnStartContainer(): Boolean {
    val containerId = (view?.parent as? ViewGroup)?.id
    return containerId?.let {
        it == R.id.first_container_id
    } ?: true
}

/**
 * @return [true] if the view or parent view is null
 * or if the parent container id is the same with the end container id ([R.id.second_container_id]),
 * [false] otherwise.
 */
fun Fragment.isOnEndContainer(): Boolean {
    val containerId = (view?.parent as? ViewGroup)?.id
    return containerId?.let {
        it == R.id.second_container_id
    } ?: true
}