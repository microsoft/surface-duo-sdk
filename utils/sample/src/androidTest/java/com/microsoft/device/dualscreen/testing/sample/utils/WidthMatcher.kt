/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.sample.utils

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class WidthMatcher(
    private val width: Int
) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("View with width: ${width}px")
    }

    override fun matchesSafely(item: View): Boolean {
        return item.measuredWidth == width
    }
}