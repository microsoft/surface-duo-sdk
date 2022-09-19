/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.navigationrail

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.sample.navigationrail.databinding.ActivityMainBinding
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureHorizontal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navRailView.menuGravity = Gravity.CENTER
        binding.navRailView.arrangeButtons(5, 2)

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
        windowLayoutInfo.isFoldingFeatureHorizontal().let { isVisible ->
            binding.apply {
                moveToStart.isVisible = isVisible
                moveToEnd.isVisible = isVisible
                spanButtons.isVisible = isVisible
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            moveToStart.setOnClickListener {
                binding.navRailView.menuGravity = Gravity.TOP
            }
            moveToEnd.setOnClickListener {
                binding.navRailView.menuGravity = Gravity.BOTTOM
            }
            spanButtons.setOnClickListener {
                binding.navRailView.arrangeButtons(4, 3)
            }
        }
    }
}
