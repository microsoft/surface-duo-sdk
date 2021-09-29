/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.bottomnavigation

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentTransaction
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.sample.bottomnavigation.databinding.ActivityMainBinding
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.isSpannedHorizontally
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    companion object {
        const val SELECTED_NAV_ITEM = "selected_nav_item"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: WindowInfoRepositoryCallbackAdapter
    private lateinit var consumerWindowLayoutInfo: Consumer<WindowLayoutInfo>
    private lateinit var runOnUiThreadExecutor: Executor

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

        initWindowLayoutInfo()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SELECTED_NAV_ITEM, binding.navView.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    private fun changeFragment(item: MenuItem) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            NumbersFragment.newInstance(item.title.toString())
        )
        transaction.commit()
    }

    private fun getSavedNavItem(savedInstanceState: Bundle?): Int {
        return if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_NAV_ITEM)) {
            savedInstanceState.getInt(SELECTED_NAV_ITEM)
        } else {
            R.id.navigation_home
        }
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
        (windowLayoutInfo.displayFeatures.firstOrNull() as FoldingFeature?).let { foldingFeature ->
            val visibility = if (foldingFeature.isSpannedHorizontally()
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.apply {
                moveToStart.visibility = visibility
                moveToEnd.visibility = visibility
                spanButtons.visibility = visibility
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
