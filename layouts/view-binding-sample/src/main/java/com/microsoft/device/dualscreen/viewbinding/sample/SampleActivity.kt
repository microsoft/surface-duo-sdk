/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.viewbinding.sample

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.microsoft.device.dualscreen.layouts.FoldableLayout
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.ScreenMode.DUAL_SCREEN
import com.microsoft.device.dualscreen.utils.wm.ScreenMode.SINGLE_SCREEN
import com.microsoft.device.dualscreen.viewbinding.sample.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    private val contentChangedListener = object : FoldableLayout.ContentChangedListener {
        override fun contentChanged(screenMode: ScreenMode?) {
            setupContent(screenMode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.foldableLayout.addContentChangedListener(contentChangedListener)
    }

    override fun onPause() {
        super.onPause()
        binding.foldableLayout.removeContentChangedListener(contentChangedListener)
    }

    private fun setupContent(screenMode: ScreenMode?) {
        when (screenMode) {
            SINGLE_SCREEN -> setupContentForSingleScreen()
            DUAL_SCREEN -> setupContentForDualScreen()
            else -> {}
        }
    }

    private fun setupContentForSingleScreen() {
        with(binding.foldableLayout) {
            val holoBlueLight = ContextCompat.getColor(context, android.R.color.holo_blue_light)
            findViewById<View>(R.id.single_screen_layout).setBackgroundColor(holoBlueLight)

            findViewById<TextView>(R.id.single_text_view).text = getString(R.string.single_screen_fragment)
        }
    }

    private fun setupContentForDualScreen() {
        with(binding.foldableLayout) {
            val holoGreenLight = ContextCompat.getColor(context, android.R.color.holo_green_light)
            findViewById<View>(R.id.dual_screen_start_layout).setBackgroundColor(holoGreenLight)
            findViewById<TextView>(R.id.dual_start_text_view).text = getString(R.string.dual_start_screen_fragment)

            val holoRedLight = ContextCompat.getColor(context, android.R.color.holo_red_light)
            findViewById<View>(R.id.dual_screen_end_layout).setBackgroundColor(holoRedLight)
            findViewById<TextView>(R.id.dual_end_text_view).text = getString(R.string.dual_end_screen_fragment)
        }
    }
}