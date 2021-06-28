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
 * Test class for FragmentHandler library using AppCompat version 1.1.0
 */
@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FragmentManagerStateWrapperTestForOldAppCompatLibrary {
    private val fragmentManagerState = FragmentManagerStateWrapper()
    private val singleScreenFragmentState = FragmentManagerState()
    private val dualScreenFragmentState = FragmentManagerState()

    @Test
    fun testSwap() {
        // Launch on single screen
        val bundle = bundleOf(FragmentManagerStateWrapper.SUPPORT_FRAGMENTS_KEY to singleScreenFragmentState)

        // Switch to dual screen
        fragmentManagerState.swapSingleToDual(bundle)
        assertThat(fragmentManagerState.singleScreenFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.dualScreenFragmentManagerState).isNull()
        assertThat(bundle.fragmentManagerState).isEqualTo(null)

        // Switch to single screen
        bundle.putParcelable(FragmentManagerStateWrapper.SUPPORT_FRAGMENTS_KEY, dualScreenFragmentState)
        fragmentManagerState.swapDualToSingle(bundle)
        assertThat(fragmentManagerState.singleScreenFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.dualScreenFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(singleScreenFragmentState)

        // Switch to dual screen
        fragmentManagerState.swapSingleToDual(bundle)
        assertThat(fragmentManagerState.singleScreenFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.dualScreenFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(dualScreenFragmentState)

        // Switch to single screen
        fragmentManagerState.swapDualToSingle(bundle)
        assertThat(fragmentManagerState.singleScreenFragmentManagerState).isEqualTo(singleScreenFragmentState)
        assertThat(fragmentManagerState.dualScreenFragmentManagerState).isEqualTo(dualScreenFragmentState)
        assertThat(bundle.fragmentManagerState).isEqualTo(singleScreenFragmentState)
    }
}

private val Bundle.fragmentManagerState: FragmentManagerState?
    get() = getParcelable(FragmentManagerStateWrapper.SUPPORT_FRAGMENTS_KEY) as? FragmentManagerState