/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_viewwrapper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.surfaceduo.sample_surfaceduo_viewwrapper.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: WindowInfoRepositoryCallbackAdapter
    private lateinit var consumerWindowLayoutInfo: Consumer<WindowLayoutInfo>
    private lateinit var runOnUiThreadExecutor: Executor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setListeners()
        initWindowLayoutInfo()
    }

    private fun initWindowLayoutInfo() {
        adapter = WindowInfoRepositoryCallbackAdapter(windowInfoRepository())
        runOnUiThreadExecutor = Executor { command: Runnable? ->
            command?.let {
                Handler(Looper.getMainLooper()).post(it)
            }
        }
        consumerWindowLayoutInfo = Consumer { windowLayoutInfo ->
            setButtonsVisibility(windowLayoutInfo)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.addWindowLayoutInfoListener(runOnUiThreadExecutor, consumerWindowLayoutInfo)
    }

    override fun onStop() {
        super.onStop()
        adapter.removeWindowLayoutInfoListener(consumerWindowLayoutInfo)
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
