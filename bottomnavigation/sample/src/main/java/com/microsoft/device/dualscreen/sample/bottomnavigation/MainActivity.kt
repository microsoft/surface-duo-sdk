/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.bottomnavigation

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.sample.bottomnavigation.databinding.ActivityMainBinding
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        const val SELECTED_NAV_ITEM = "selected_nav_item"
    }

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        setBadges()

        binding.navView.setOnNavigationItemSelectedListener { item: MenuItem ->
            changeFragment(item)
            return@setOnNavigationItemSelectedListener true
        }

        binding.navView.apply {
            selectedItemId = getSavedNavItem(savedInstanceState)
            useTransparentBackground = true
            arrangeButtons(3, 2)

            useAnimation = true
            animationInterpolator = OvershootInterpolator()
            allowFlingGesture = true
        }

        registerWindowInfoFlow()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SELECTED_NAV_ITEM, binding.navView.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    private fun changeFragment(item: MenuItem) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            NumbersFragment.newInstance()
        )
        transaction.commit()
        binding.fragmentName.text = getString(R.string.key_fragment, item.title.toString())
    }

    private fun getSavedNavItem(savedInstanceState: Bundle?): Int {
        return if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_NAV_ITEM)) {
            savedInstanceState.getInt(SELECTED_NAV_ITEM)
        } else {
            R.id.navigation_home
        }
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
                spanButtons.isVisible = isVisible
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            moveToStart.setOnClickListener {
                moveNavigationView(DisplayPosition.START)
            }
            moveToEnd.setOnClickListener {
                moveNavigationView(DisplayPosition.END)
            }
            spanButtons.setOnClickListener {
                navView.arrangeButtons(2, 3)
            }
        }
    }

    private fun moveNavigationView(displayPosition: DisplayPosition) {
        binding.navView.displayPosition = displayPosition
    }

    private fun setBadges() {
        val badge = binding.navView.getOrCreateBadge(R.id.navigation_alerts)
        badge.isVisible = true
        // set a random number
        badge.number = 20
    }
}
