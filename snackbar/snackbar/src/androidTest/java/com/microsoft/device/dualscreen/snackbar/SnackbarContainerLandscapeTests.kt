/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar

import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.BOTH
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.END
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.START
import com.microsoft.device.dualscreen.snackbar.test.R
import com.microsoft.device.dualscreen.testing.getDeviceModel
import com.microsoft.device.dualscreen.testing.resetOrientation
import com.microsoft.device.dualscreen.testing.spanFromStart
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SnackbarContainerLandscapeTests : SnackbarContainerTests() {
    @Before
    fun setOrientation() {
        uiDevice.setOrientationRight()
    }

    @After
    fun resetOrientation() {
        uiDevice.resetOrientation()
    }

    @Test
    fun testPositionOnSingleScreen() {
        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_end, END, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }

    @Test
    fun testPositionOnDualScreen() {
        windowLayoutInfoConsumer.reset()
        uiDevice.spanFromStart()
        windowLayoutInfoConsumer.waitForWindowInfoLayoutChanges()

        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        val bottomMargin = with(uiDevice.getDeviceModel()) {
            paneWidth + foldWidth + margin
        }
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, margin, bottomMargin))
        testMargins(TestParams(R.string.snackbar_to_end, END, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }
}
