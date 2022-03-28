/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar.utils

import android.view.View
import android.widget.FrameLayout
import androidx.test.espresso.matcher.BoundedMatcher
import com.microsoft.device.dualscreen.snackbar.SnackbarContainer
import org.hamcrest.Description

class MarginsMatcher(
    private val left: Int,
    private val top: Int,
    private val right: Int,
    private val bottom: Int
) : BoundedMatcher<View, SnackbarContainer>(SnackbarContainer::class.java) {

    private var failedCheckDescription = ""

    override fun describeTo(description: Description?) {
        description?.appendText(failedCheckDescription)
    }

    override fun matchesSafely(item: SnackbarContainer?): Boolean {
        return item?.let { view ->
            (view.coordinatorLayout.layoutParams as? FrameLayout.LayoutParams)?.let { layoutParams ->
                with(layoutParams) {
                    if (left == leftMargin &&
                        top == topMargin &&
                        right == rightMargin &&
                        bottom == bottomMargin
                    ) {
                        true
                    } else {
                        failedCheckDescription = "actual margins [$leftMargin, $topMargin, $rightMargin, $bottomMargin] " +
                            "are different than expected [$left, $top, $right, $bottom]"
                        false
                    }
                }
            } ?: false
        } ?: false
    }
}