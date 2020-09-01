/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.bottomnavigation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.microsoft.device.dualscreen.core.DisplayPosition
import com.microsoft.device.dualscreen.core.ScreenHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val SELECTED_NAV_ITEM = "selected_nav_item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setListeners()
        setButtonsVisibility()
        setBadges()

        nav_view.setOnNavigationItemSelectedListener { item: MenuItem ->
            changeFragment(item)
            return@setOnNavigationItemSelectedListener true
        }

        nav_view.selectedItemId = getSavedNavItem(savedInstanceState)
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
    }

    private fun setListeners() {
        move_to_start.setOnClickListener {
            moveNavigationView(DisplayPosition.START)
        }
        move_to_end.setOnClickListener {
            moveNavigationView(DisplayPosition.END)
        }
    }

    private fun moveNavigationView(displayPosition: DisplayPosition) {
        nav_view.surfaceDuoDisplayPosition = displayPosition
    }

    private fun setBadges() {
        var badge = nav_view.getOrCreateBadge(R.id.navigation_alerts)
        badge.isVisible = true
        // set a random number
        badge.number = 20
    }
}
