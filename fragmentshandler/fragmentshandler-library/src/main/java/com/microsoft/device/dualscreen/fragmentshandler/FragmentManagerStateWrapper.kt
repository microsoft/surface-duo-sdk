/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.os.Bundle
import android.os.Parcelable

/**
 * Swaps FragmentManagerState depending on the device's screen mode
 */
internal class FragmentManagerStateWrapper {
    private var singleScreenFragmentManagerState: Parcelable? = null
    private var dualScreenFragmentManagerState: Parcelable? = null

    /**
     * Swap single screen mode FragmentManagerState with dual screen mode FragmentManagerState
     */
    fun swapSingleToDual(bundle: Bundle) {
        bundle.getParcelable<Parcelable>(FM_STATE_KEY)?.let {
            singleScreenFragmentManagerState = it
        }
        bundle.putParcelable(FM_STATE_KEY, dualScreenFragmentManagerState)
    }

    /**
     * Swap dual screen mode FragmentManagerState with single screen mode FragmentManagerState
     */
    fun swapDualToSingle(bundle: Bundle) {
        bundle.getParcelable<Parcelable>(FM_STATE_KEY)?.let {
            dualScreenFragmentManagerState = it
        }
        bundle.putParcelable(FM_STATE_KEY, singleScreenFragmentManagerState)
    }

    companion object {
        private const val FM_STATE_KEY = "android:support:fragments"
    }
}