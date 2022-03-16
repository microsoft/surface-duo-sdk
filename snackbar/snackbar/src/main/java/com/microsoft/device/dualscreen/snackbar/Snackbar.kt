/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar

/**
 * Display a [Snackbar] inside the [SnackbarContainer] on the desired [SnackbarPosition] position
 *
 * @param container The container used to display the [Snackbar]
 * @param position The desired position.
 * Can be [SnackbarPosition.START] meaning that the [Snackbar] will be displayed on the first display area,
 * [SnackbarPosition.END] will display the [Snackbar] on the second display area
 * and [SnackbarPosition.BOTH] will display the [Snackbar] on the entire display area.
 */
fun Snackbar.show(container: SnackbarContainer, position: SnackbarPosition): Snackbar {
    view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
        width = MATCH_PARENT
    }
    container.updatePosition(position)
    show()
    return this
}

enum class SnackbarPosition {
    START, END, BOTH
}
