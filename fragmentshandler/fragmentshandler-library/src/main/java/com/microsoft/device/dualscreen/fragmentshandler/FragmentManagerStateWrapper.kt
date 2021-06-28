/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting

/**
 * Swaps FragmentManagerState depending on the device's screen mode
 */
internal class FragmentManagerStateWrapper {
    @VisibleForTesting
    var singleScreenFragmentManagerState: Any? = null

    @VisibleForTesting
    var dualScreenFragmentManagerState: Any? = null

    /**
     * Swap single screen mode FragmentManagerState with dual screen mode FragmentManagerState
     */
    fun swapSingleToDual(bundle: Bundle) {
        when {
            // AppCompat 1.1.0
            bundle.containsKey(SUPPORT_FRAGMENTS_KEY) -> {
                bundle.getParcelable<Parcelable>(SUPPORT_FRAGMENTS_KEY)?.let {
                    singleScreenFragmentManagerState = it
                }
                bundle.putParcelable(SUPPORT_FRAGMENTS_KEY, dualScreenFragmentManagerState as? Parcelable)
            }

            // AppCompat 1.3.0
            bundle.containsKey(BUNDLE_SAVED_STATE_REGISTRY_KEY) -> {
                bundle.getBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY)?.let {
                    singleScreenFragmentManagerState = it
                }
                bundle.putBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY, dualScreenFragmentManagerState as? Bundle)
            }
        }
    }

    /**
     * Swap dual screen mode FragmentManagerState with single screen mode FragmentManagerState
     */
    fun swapDualToSingle(bundle: Bundle) {
        when {
            // AppCompat 1.1.0
            bundle.containsKey(SUPPORT_FRAGMENTS_KEY) -> {
                bundle.getParcelable<Parcelable>(SUPPORT_FRAGMENTS_KEY)?.let {
                    dualScreenFragmentManagerState = it
                }
                bundle.putParcelable(SUPPORT_FRAGMENTS_KEY, singleScreenFragmentManagerState as? Parcelable)
            }

            // AppCompat 1.3.0
            bundle.containsKey(BUNDLE_SAVED_STATE_REGISTRY_KEY) -> {
                bundle.getBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY)?.let {
                    dualScreenFragmentManagerState = it
                }
                bundle.putBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY, singleScreenFragmentManagerState as? Bundle)
            }
        }
    }

    companion object {
        @VisibleForTesting
        const val BUNDLE_SAVED_STATE_REGISTRY_KEY = "androidx.lifecycle.BundlableSavedStateRegistry.key"

        @VisibleForTesting
        const val SUPPORT_FRAGMENTS_KEY = "android:support:fragments"
    }
}