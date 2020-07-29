/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample.tablayout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.microsoft.device.dualscreen.core.DisplayPosition
import com.microsoft.device.dualscreen.core.ScreenHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonsVisibility()
        setListeners()
        prepareViewPager()
    }

    private fun setButtonsVisibility() {
        val visibility = if (ScreenHelper.isDeviceSurfaceDuo(this) &&
            ScreenHelper.isDualMode(this)
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
            moveTabLayout(DisplayPosition.DUAL)
        }
    }

    private fun moveTabLayout(displayPosition: DisplayPosition) {
        tab_layout.surfaceDuoDisplayPosition = displayPosition
    }

    private fun prepareViewPager() {
        setupViewPager(viewpager)
        tab_layout.setupWithViewPager(viewpager)

        tab_layout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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
