/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.fragmentshandler.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

/**
 * Simple Fragment used for testing purpose
 */
class SampleFragment : Fragment() {
    private var _onCreateViewWasCalled = false

    /**
     * This flag should be [true] if the [SampleFragment.onCreateView] was called, [false] otherwise.
     */
    val onCreateViewWasCalled: Boolean
        get() = _onCreateViewWasCalled

    companion object {
        private const val LAY_RES_ID = "layResId"

        /**
         * Creates a new instance of [SampleFragment] with the desired layout resource id.
         */
        fun newInstance(@LayoutRes layResId: Int): SampleFragment = SampleFragment().apply {
            arguments = bundleOf(LAY_RES_ID to layResId)
        }
    }

    /**
     * Resets the flag that tells that the [SampleFragment.onCreateView] method was called.
     */
    fun reset() {
        _onCreateViewWasCalled = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _onCreateViewWasCalled = true
        val layResId = arguments?.getInt(LAY_RES_ID) ?: 0
        return inflater.inflate(layResId, container, false)
    }
}