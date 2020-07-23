/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.fragmentshandler.FragmentManagerStateWrapper
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.DualEndFragment
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.DualStartFragment
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.SingleScreenFragment

class MainActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_DUAL_START = "FragmentDualStart"
        private const val FRAGMENT_DUAL_END = "FragmentDualEnd"
        private const val FRAGMENT_SINGLE_SCREEN = "FragmentSingleScreen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when ((application as SampleApp).surfaceDuoScreenManager.screenMode) {
            ScreenMode.SINGLE_SCREEN -> {
                if (savedInstanceState?.get(FragmentManagerStateWrapper.FM_STATE_KEY) == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.first_container_id,
                            SingleScreenFragment(),
                            FRAGMENT_SINGLE_SCREEN
                        ).commit()
                }
            }
            ScreenMode.DUAL_SCREEN -> {
                if (savedInstanceState?.get(FragmentManagerStateWrapper.FM_STATE_KEY) == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.first_container_id,
                            DualStartFragment(),
                            FRAGMENT_DUAL_START
                        ).commit()
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.second_container_id,
                            DualEndFragment(),
                            FRAGMENT_DUAL_END
                        ).commit()
                }
            }
        }
    }
}
