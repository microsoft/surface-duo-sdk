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
    var firstFragmentManagerState: Any? = null

    @VisibleForTesting
    var secondFragmentManagerState: Any? = null
    private var usefirstFragmentManagerState = true

    fun swap(bundle: Bundle) {
        when {
            usefirstFragmentManagerState -> swapFirstToSecond(bundle)
            else -> swapSecondToFirst(bundle)
        }

        usefirstFragmentManagerState = usefirstFragmentManagerState.not()
    }

    /**
     * Swap single screen mode FragmentManagerState with dual screen mode FragmentManagerState
     */
    @VisibleForTesting
    fun swapFirstToSecond(bundle: Bundle) {
        when {
            // AppCompat 1.1.0
            bundle.containsKey(SUPPORT_FRAGMENTS_KEY) -> {
                bundle.getParcelable<Parcelable>(SUPPORT_FRAGMENTS_KEY)?.let {
                    firstFragmentManagerState = it
                }
                bundle.putParcelable(
                    SUPPORT_FRAGMENTS_KEY,
                    secondFragmentManagerState as? Parcelable
                )
            }

            // AppCompat 1.3.0
            bundle.containsKey(BUNDLE_SAVED_STATE_REGISTRY_KEY) -> {
                bundle.getBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY)?.let {
                    firstFragmentManagerState = it
                }
                bundle.putBundle(
                    BUNDLE_SAVED_STATE_REGISTRY_KEY,
                    secondFragmentManagerState as? Bundle
                )
            }
        }
    }

    /**
     * Swap dual screen mode FragmentManagerState with single screen mode FragmentManagerState
     */
    @VisibleForTesting
    fun swapSecondToFirst(bundle: Bundle) {
        when {
            // AppCompat 1.1.0
            bundle.containsKey(SUPPORT_FRAGMENTS_KEY) -> {
                bundle.getParcelable<Parcelable>(SUPPORT_FRAGMENTS_KEY)?.let {
                    secondFragmentManagerState = it
                }
                bundle.putParcelable(
                    SUPPORT_FRAGMENTS_KEY,
                    firstFragmentManagerState as? Parcelable
                )
            }

            // AppCompat 1.3.0
            bundle.containsKey(BUNDLE_SAVED_STATE_REGISTRY_KEY) -> {
                bundle.getBundle(BUNDLE_SAVED_STATE_REGISTRY_KEY)?.let {
                    secondFragmentManagerState = it
                }
                bundle.putBundle(
                    BUNDLE_SAVED_STATE_REGISTRY_KEY,
                    firstFragmentManagerState as? Bundle
                )
            }
        }
    }

    companion object {
        @VisibleForTesting
        const val BUNDLE_SAVED_STATE_REGISTRY_KEY =
            "androidx.lifecycle.BundlableSavedStateRegistry.key"

        @VisibleForTesting
        const val SUPPORT_FRAGMENTS_KEY = "android:support:fragments"
    }
}