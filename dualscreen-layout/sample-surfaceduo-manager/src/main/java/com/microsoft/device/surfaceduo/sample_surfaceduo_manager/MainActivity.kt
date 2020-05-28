/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_manager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.microsoft.device.dualscreen.layout.ScreenMode
import com.microsoft.device.dualscreen.layout.manager.ScreenModeListener
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.StartFragment
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.EndFragment

class MainActivity : FragmentActivity() {
    private val singleScreenFragmentTag = "single_screen_fragment"
    private val dualStartFragmentTag = "dual_start_fragment"
    private val dualEndFragmentTag = "dual_end_fragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when ((application as SampleApp).surfaceDuoScreenManager.screenMode) {
            ScreenMode.SINGLE_SCREEN -> {
                if (savedInstanceState != null) {
                    supportFragmentManager.findFragmentByTag(dualStartFragmentTag)?.let {
                        supportFragmentManager.beginTransaction()
                            .remove(it).commit()
                    }
                    supportFragmentManager.findFragmentByTag(dualEndFragmentTag)?.let {
                        supportFragmentManager.beginTransaction()
                            .remove(it).commit()
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.single_screen_container_id, StartFragment(),
                        singleScreenFragmentTag
                    )
                    .commit()
            }
            ScreenMode.DUAL_SCREEN -> {
                if (savedInstanceState != null) {
                    supportFragmentManager.findFragmentByTag(singleScreenFragmentTag)?.let {
                        supportFragmentManager.beginTransaction()
                            .remove(it).commit()
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.dual_screen_start_container_id, StartFragment(),
                        dualStartFragmentTag
                    )
                    .commit()
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.dual_screen_end_container_id, EndFragment(),
                        dualEndFragmentTag
                    )
                    .commit()
            }
        }

        (application as SampleApp).surfaceDuoScreenManager.addScreenModeListener( this,
            object :
                ScreenModeListener {
                override fun onSwitchToSingleScreen() {}

                override fun onSwitchToDualScreen() {}
        })

    }

}
