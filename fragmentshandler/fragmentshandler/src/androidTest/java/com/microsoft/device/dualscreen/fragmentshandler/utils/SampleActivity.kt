/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.fragmentshandler.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.fragmentshandler.test.R
import com.microsoft.device.dualscreen.utils.wm.isInDualMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

/**
 * Simple activity used for testing purpose.
 */
class SampleActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_DUAL_START = "FragmentDualStart"
        private const val FRAGMENT_DUAL_END = "FragmentDualEnd"
        private const val FRAGMENT_SINGLE_SCREEN = "FragmentSingleScreen"
    }

    /**
     * [SampleFragment] instance that it's visible when application is in single screen mode
     */
    val singleScreenFragment by lazy {
        supportFragmentManager.findFragmentByTag(FRAGMENT_SINGLE_SCREEN) as? SampleFragment
            ?: SampleFragment.newInstance(R.layout.fragment_single)
    }

    /**
     * [SampleFragment] instance that it's visible inside the first screen when application is in dual screen mode
     */
    val dualScreenStartFragment by lazy {
        supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_START) as? SampleFragment
            ?: SampleFragment.newInstance(R.layout.fragment_start)
    }

    /**
     * [SampleFragment] instance that it's visible inside the second screen when application is in dual screen mode
     */
    val dualScreenEndFragment by lazy {
        supportFragmentManager.findFragmentByTag(FRAGMENT_DUAL_END) as? SampleFragment
            ?: SampleFragment.newInstance(R.layout.fragment_end)
    }

    /**
     * Last [Bundle] from [SampleActivity.onCreate]
     */
    var lastSavedInstanceState: Bundle? = null

    /**
     * Reset [SampleFragment.onCreateViewWasCalled] flag from all fragments
     */
    fun resetFragments() {
        singleScreenFragment.reset()
        dualScreenEndFragment.reset()
        dualScreenStartFragment.reset()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lastSavedInstanceState = savedInstanceState
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
                            onWindowLayoutInfoChanged(info)
                        }
                    }
            }
        }
    }

    /**
     * Called whenever the screen info was changed.
     * @param windowLayoutInfo object used to retrieve screen information
     */
    private fun onWindowLayoutInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
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
                replace(R.id.first_container_id, singleScreenFragment, FRAGMENT_SINGLE_SCREEN)
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
                replace(R.id.first_container_id, dualScreenStartFragment, FRAGMENT_DUAL_START)
            }

            supportFragmentManager.inTransaction {
                replace(R.id.second_container_id, dualScreenEndFragment, FRAGMENT_DUAL_END)
            }
        }
    }
}