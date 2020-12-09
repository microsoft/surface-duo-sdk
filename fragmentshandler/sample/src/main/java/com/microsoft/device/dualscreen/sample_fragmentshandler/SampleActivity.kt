/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_fragmentshandler

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualEndFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.DualStartFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.SingleScreenFragment
import com.microsoft.device.dualscreen.sample_fragmentshandler.fragments.inTransaction

class SampleActivity : FragmentActivity(), ScreenInfoListener {
    companion object {
        private const val FRAGMENT_DUAL_START = "FragmentDualStart"
        private const val FRAGMENT_DUAL_END = "FragmentDualEnd"
        private const val FRAGMENT_SINGLE_SCREEN = "FragmentSingleScreen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }

    override fun onStart() {
        super.onStart()
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
