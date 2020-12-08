/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.fragmentshandler.utils

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.fragmentshandler.test.R

/**
 * Simple activity used for testing purpose.
 */
class SampleActivity : FragmentActivity(), ScreenInfoListener {
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }

    override fun onResume() {
        super.onResume()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(this)
    }

    override fun onPause() {
        super.onPause()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(this)
    }

    /**
     * Called whenever the screen info was changed.
     * @param screenInfo object used to retrieve screen information
     */
    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        when {
            screenInfo.isDualMode() -> setupDualScreenFragments()
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