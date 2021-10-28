/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.tablayout

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.microsoft.device.dualscreen.sample.tablayout.databinding.ActivityMainBinding
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: WindowInfoRepositoryCallbackAdapter
    private lateinit var consumerWindowLayoutInfo: Consumer<WindowLayoutInfo>
    private lateinit var runOnUiThreadExecutor: Executor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        prepareViewPager()

        binding.tabLayout.arrangeButtons(2, 4)
        binding.tabLayout.allowFlingGesture = true
        binding.tabLayout.useTransparentBackground = true

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
                moveTabLayout(DisplayPosition.START)
            }
            moveToEnd.setOnClickListener {
                moveTabLayout(DisplayPosition.END)
            }
            moveToMiddle.setOnClickListener {
                binding.tabLayout.arrangeButtons(2, 4)
            }
        }
    }

    private fun moveTabLayout(displayPosition: DisplayPosition) {
        binding.tabLayout.displayPosition = displayPosition
    }

    private fun prepareViewPager() {
        setupViewPager(binding.viewpager)
        binding.tabLayout.setupWithViewPager(binding.viewpager)

        binding.tabLayout.addOnTabSelectedListener(
            object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.viewpager.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            },
        )
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter =
            ViewPagerAdapter(
                supportFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            )

        adapter.addFrag(DummyFragment(), "Tab 1")
        adapter.addFrag(DummyFragment(), "Tab 2")
        adapter.addFrag(DummyFragment(), "Tab 3")
        adapter.addFrag(DummyFragment(), "Tab 4")
        adapter.addFrag(DummyFragment(), "Tab 5")
        adapter.addFrag(DummyFragment(), "Tab 6")

        viewPager.adapter = adapter
    }
}
