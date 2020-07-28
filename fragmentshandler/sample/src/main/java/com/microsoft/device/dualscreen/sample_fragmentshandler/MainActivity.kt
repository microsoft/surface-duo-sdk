/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_fragmentshandler

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualEndFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualStartFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.SingleScreenFragment

class MainActivity : FragmentActivity() {
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
                if (supportFragmentManager.findFragmentByTag(FRAGMENT_SINGLE_SCREEN) == null) {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.first_container_id,
                        SingleScreenFragment(),
                        FRAGMENT_SINGLE_SCREEN
                    ).commit()
                }
            }
            ScreenMode.DUAL_SCREEN -> {
                if (supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_START) == null &&
                    supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_END) == null
                ) {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.first_container_id,
                        DualStartFragment(),
                        FRAGMENT_DUAL_START
                    ).commit()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.second_container_id,
                        DualEndFragment(),
                        FRAGMENT_DUAL_END
                    ).commit()
                }
            }
        }
    }
}
