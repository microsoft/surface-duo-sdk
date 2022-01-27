/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_surfaceduo_viewwrapper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.sample_surfaceduo_viewwrapper.databinding.ActivityMainBinding
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        registerWindowInfoFlow()
    }

    private fun registerWindowInfoFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { windowLayoutInfo ->
                        setButtonsVisibility(windowLayoutInfo)
                    }
            }
        }
    }

    private fun setButtonsVisibility(windowLayoutInfo: WindowLayoutInfo) {
        windowLayoutInfo.isFoldingFeatureVertical().let { isVisible ->
            binding.apply {
                moveToStart.isVisible = isVisible
                moveToEnd.isVisible = isVisible
                moveToMiddle.isVisible = isVisible
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            moveToStart.setOnClickListener {
                moveWrapperContent(DisplayPosition.START)
            }
            moveToEnd.setOnClickListener {
                moveWrapperContent(DisplayPosition.END)
            }
            moveToMiddle.setOnClickListener {
                moveWrapperContent(DisplayPosition.DUAL)
            }
        }
    }

    private fun moveWrapperContent(displayPosition: DisplayPosition) {
        binding.duoWrapper.foldableDisplayPosition = displayPosition
    }
}
