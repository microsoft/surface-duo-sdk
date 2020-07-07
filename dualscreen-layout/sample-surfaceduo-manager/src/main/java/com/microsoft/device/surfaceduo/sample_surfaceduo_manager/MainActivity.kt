/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_manager

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.microsoft.device.dualscreen.layout.ScreenMode
import com.microsoft.device.dualscreen.layout.manager.ScreenModeListener
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.EndFragment
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.StartFragment

class MainActivity : FragmentActivity() {
    private val singleScreenFragmentTag = "single_screen_fragment"
    private val dualStartFragmentTag = "dual_start_fragment"
    private val dualEndFragmentTag = "dual_end_fragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when ((application as SampleApp).surfaceDuoScreenManager.screenMode) {
            ScreenMode.SINGLE_SCREEN -> {
                handleSingleScreenFragments(savedInstanceState)
            }
            ScreenMode.DUAL_SCREEN -> {
                handleDualScreenFragments(savedInstanceState)
            }
        }

        (application as SampleApp).surfaceDuoScreenManager.addScreenModeListener(
            this,
            object :
                ScreenModeListener {
                override fun onSwitchToSingleScreen() {}
                override fun onSwitchToDualScreen() {}
            }
        )
    }

    private fun handleSingleScreenFragments(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
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

    private fun handleDualScreenFragments(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
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
