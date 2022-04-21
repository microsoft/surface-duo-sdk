/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar

import android.app.UiAutomation
import androidx.test.filters.MediumTest
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.BOTH
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.END
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.START
import com.microsoft.device.dualscreen.snackbar.test.R
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.getDeviceModel
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(FoldableJUnit4ClassRunner::class)
class SnackbarContainerLandscapeTests : SnackbarContainerTests() {
    @Test
    @SingleScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    fun testPositionOnSingleScreen() {
        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_end, END, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }

    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_270)
    fun testPositionOnDualScreen() {
        val margin = SnackbarContainer.COORDINATOR_LAYOUT_MARGIN
        val bottomMargin = with(uiDevice.getDeviceModel()) {
            paneWidth + foldWidth + margin
        }
        testMargins(TestParams(R.string.snackbar_to_start, START, margin, 0, margin, bottomMargin))
        testMargins(TestParams(R.string.snackbar_to_end, END, margin, 0, margin, margin))
        testMargins(TestParams(R.string.snackbar_to_both, BOTH, margin, 0, margin, margin))
    }
}
