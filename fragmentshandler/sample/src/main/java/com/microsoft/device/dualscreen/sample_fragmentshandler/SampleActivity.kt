/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_fragmentshandler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualEndFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualStartFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.SingleScreenFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.inTransaction
import com.microsoft.device.dualscreen.utils.wm.isInDualMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class SampleActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_DUAL_START = "FragmentDualStart"
        private const val FRAGMENT_DUAL_END = "FragmentDualEnd"
        private const val FRAGMENT_SINGLE_SCREEN = "FragmentSingleScreen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        registerWindowInfoFlow()
    }

    private fun registerWindowInfoFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@SampleActivity)
                    .windowLayoutInfo(this@SampleActivity)
                    .collectIndexed { index, info ->
                        if (index == 0) {
                            onScreenInfoChanged(info)
                        }
                    }
            }
        }
    }

    /**
     * Called whenever the screen info was changed.
     * @param screenInfo object used to retrieve screen information
     */
    private fun onScreenInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
        when {
            windowLayoutInfo.isInDualMode() -> setupDualScreenFragments()
            else -> setupSingleScreenFragments()
        }
    }

    /**
     * Adds fragments for the single screen mode
     */
    private fun setupSingleScreenFragments() {
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_SINGLE_SCREEN) == null) {
            supportFragmentManager.inTransaction {
                replace(R.id.first_container_id, SingleScreenFragment(), FRAGMENT_SINGLE_SCREEN)
            }
        }
    }

    /**
     * Adds fragments for the dual screen mode
     */
    private fun setupDualScreenFragments() {
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_START) == null &&
            supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_END) == null
        ) {
            supportFragmentManager.inTransaction {
                replace(R.id.first_container_id, DualStartFragment(), FRAGMENT_DUAL_START)
            }

            supportFragmentManager.inTransaction {
                replace(R.id.second_container_id, DualEndFragment(), FRAGMENT_DUAL_END)
            }
        }
    }
}
