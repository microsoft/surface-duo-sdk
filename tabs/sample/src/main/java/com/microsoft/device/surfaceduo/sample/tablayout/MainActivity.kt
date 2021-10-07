/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample.tablayout

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ScreenInfoListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
        prepareViewPager()

        tab_layout.arrangeButtons(2, 4)
        tab_layout.allowFlingGesture = true
    }

    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        setButtonsVisibility(screenInfo)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }

    override fun onStart() {
        super.onStart()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(this)
    }

    override fun onStop() {
        super.onStop()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(this)
    }

    private fun setButtonsVisibility(screenInfo: ScreenInfo) {
        val visibility = if (screenInfo.isDualMode()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }

        move_to_start.visibility = visibility
        move_to_end.visibility = visibility
        move_to_middle.visibility = visibility
    }

    private fun setListeners() {
        move_to_start.setOnClickListener {
            moveTabLayout(DisplayPosition.START)
        }
        move_to_end.setOnClickListener {
            moveTabLayout(DisplayPosition.END)
        }
        move_to_middle.setOnClickListener {
            tab_layout.arrangeButtons(4, 2)
        }
    }

    private fun moveTabLayout(displayPosition: DisplayPosition) {
        tab_layout.displayPosition = displayPosition
    }

    private fun prepareViewPager() {
        setupViewPager(viewpager)
        tab_layout.setupWithViewPager(viewpager)

        tab_layout.addOnTabSelectedListener(
            object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewpager.currentItem = tab.position
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
