/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.fragmentshandler.utils.FragmentManagerState
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Test class for FragmentHandler library using AppCompat version 1.3.0
 */
@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FragmentManagerStateWrapperTestForNewAppCompatLibrary {
    private val fragmentManagerState = FragmentManagerStateWrapper()
    private val singleScreenFragmentState = bundleOf("android:support:fragments" to FragmentManagerState())
    private val dualScreenFragmentState = bundleOf("android:support:fragments" to FragmentManagerState())

    @Test
    fun testSwap() {
        // Launch on single screen
        val bundle = bundleOf(FragmentManagerStateWrapper.BUNDLE_SAVED_STATE_REGISTRY_KEY to singleScreenFragmentState)

        // Switch to dual screen
        fragmentManagerState.swapFirstToSecond(bundle)
        assertThat(fragmentManagerState.firstFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.secondFragmentManagerState).isNull()
        assertThat(bundle.fragmentManagerState).isEqualTo(null)

        // Switch to single screen
        bundle.putParcelable(FragmentManagerStateWrapper.BUNDLE_SAVED_STATE_REGISTRY_KEY, dualScreenFragmentState)
        fragmentManagerState.swapSecondToFirst(bundle)
        assertThat(fragmentManagerState.firstFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.secondFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(singleScreenFragmentState)

        // Switch to dual screen
        fragmentManagerState.swapFirstToSecond(bundle)
        assertThat(fragmentManagerState.firstFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.secondFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(dualScreenFragmentState)

        // Switch to single screen
        fragmentManagerState.swapSecondToFirst(bundle)
        assertThat(fragmentManagerState.firstFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.secondFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(singleScreenFragmentState)
    }
}

private val Bundle.fragmentManagerState: Bundle?
    get() = getBundle(FragmentManagerStateWrapper.BUNDLE_SAVED_STATE_REGISTRY_KEY)
