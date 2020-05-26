/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.microsoft.device.dualscreen.layout.manager.ScreenModeListener
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.R
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.SampleApp

class EndFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity!!.application as SampleApp).surfaceDuoScreenManager.addScreenModeListener(
            this,
            object : ScreenModeListener {

                override fun onSwitchToSingleScreen() {
                    val a = 1
                }

                override fun onSwitchToDualScreen() {
                    val a = 1
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_end, container, false)
    }
}