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
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.getDeviceModel
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
class SnackbarContainerPortraitTests : SnackbarContainerTests() {

    @Test
    @SingleScreenTest
    fun testPositionOnSingleScreen() {
        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_end, END, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }

    @Test
    @DualScreenTest
    fun testPositionOnDualScreen() {
        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        val rightMargin = with(uiDevice.getDeviceModel()) {
            paneWidth - margin
        }
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, rightMargin, margin))
        val leftMargin = with(uiDevice.getDeviceModel()) {
            paneWidth + foldWidth + margin
        }
        testMargins(TestParams(R.string.snackbar_to_end, END, leftMargin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }
}