/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_bottomnavigation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.common.DisplayPosition
import com.microsoft.device.dualscreen.layout.ScreenHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setListeners()
        setButtonsVisibility()

        nav_view.duoDisplayPosition = DisplayPosition.END
        nav_view.setOnClickListener {
            moveNavView()
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
        move_to_middle.visibility = View.INVISIBLE
    }

    private fun setListeners() {
        move_to_start.setOnClickListener {
            moveNavigationView(DisplayPosition.START)
        }
        move_to_end.setOnClickListener {
            moveNavigationView(DisplayPosition.END)
        }
        move_to_middle.setOnClickListener {
            moveNavigationView(DisplayPosition.DUAL)
        }
    }

    private fun moveNavigationView(displayPosition: DisplayPosition) {
        nav_view.duoDisplayPosition = displayPosition
    }

    private fun moveNavView() {
        val mode = nav_view.duoDisplayPosition
        if (mode == DisplayPosition.DUAL) {
            nav_view.duoDisplayPosition = DisplayPosition.START
        }

        if (mode == DisplayPosition.START) {
            nav_view.duoDisplayPosition = DisplayPosition.END
        }

        if (mode == DisplayPosition.END) {
            nav_view.duoDisplayPosition = DisplayPosition.DUAL
        }
    }
}
