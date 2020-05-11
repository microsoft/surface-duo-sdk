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
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.FirstFragment
import com.microsoft.device.surfaceduo.sample_surfaceduo_manager.fragments.SecondFragment

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
                        R.id.single_screen_container_id, FirstFragment(),
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
                        R.id.dual_screen_start_container_id, FirstFragment(),
                        dualStartFragmentTag
                    )
                    .commit()
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.dual_screen_end_container_id, SecondFragment(),
                        dualEndFragmentTag
                    )
                    .commit()
            }
            ScreenMode.NOT_DEFINED -> { }
        }

        (application as SampleApp).surfaceDuoScreenManager.addScreenModeListener( this,
            object :
                ScreenModeListener {
                override fun onSwitchToSingleScreen() {
                    // "Not the val we deserve... But the val we need..." to put a breakpoint on it :D
                    val a = 1
                }

                override fun onSwitchToDualScreen() {
                    val a = 1
                }
        })

        Log.d("Check", "FIRST: onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("Check", "FIRST: onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Check", "FIRST: onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Check", "FIRST: onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Check", "FIRST: onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Check", "FIRST: onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Check", "FIRST: onDestroy")
    }
}
