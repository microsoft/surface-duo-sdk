/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.bottomnavigation

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val SELECTED_NAV_ITEM = "selected_nav_item"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setListeners()
        setBadges()

        nav_view.setOnNavigationItemSelectedListener { item: MenuItem ->
            changeFragment(item)
            return@setOnNavigationItemSelectedListener true
        }

        nav_view.selectedItemId = getSavedNavItem(savedInstanceState)
        nav_view.useTransparentBackground = true
        nav_view.arrangeButtons(3, 2)

        nav_view.useAnimation = true
        nav_view.animationInterpolator = OvershootInterpolator()
        nav_view.allowFlingGesture = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(screenInfoListener)
    }

    private val screenInfoListener = object : ScreenInfoListener {
        override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
            setButtonsVisibility(screenInfo)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SELECTED_NAV_ITEM, nav_view.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    private fun changeFragment(item: MenuItem) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            SelectedFragment.newInstance(item.title.toString())
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

    private fun setButtonsVisibility(screenInfo: ScreenInfo) {
        val visibility = if (screenInfo.isDualMode()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }

        move_to_start.visibility = visibility
        move_to_end.visibility = visibility
        span_buttons.visibility = visibility
    }

    private fun setListeners() {
        move_to_start.setOnClickListener {
            moveNavigationView(DisplayPosition.START)
        }
        move_to_end.setOnClickListener {
            moveNavigationView(DisplayPosition.END)
        }
        span_buttons.setOnClickListener {
            nav_view.arrangeButtons(2, 3)
        }
    }

    private fun moveNavigationView(displayPosition: DisplayPosition) {
        nav_view.displayPosition = displayPosition
    }

    private fun setBadges() {
        val badge = nav_view.getOrCreateBadge(R.id.navigation_alerts)
        badge.isVisible = true
        // set a random number
        badge.number = 20
    }
}
