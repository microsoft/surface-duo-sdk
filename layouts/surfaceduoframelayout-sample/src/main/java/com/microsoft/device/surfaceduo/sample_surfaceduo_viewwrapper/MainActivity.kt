/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_viewwrapper

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.core.DisplayPosition
import com.microsoft.device.dualscreen.core.ScreenHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonsVisibility()
        setListeners()
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
            moveWrapperContent(DisplayPosition.START)
        }
        move_to_end.setOnClickListener {
            moveWrapperContent(DisplayPosition.END)
        }
        move_to_middle.setOnClickListener {
            moveWrapperContent(DisplayPosition.DUAL)
        }
    }

    private fun moveWrapperContent(displayPosition: DisplayPosition) {
        duo_wrapper.surfaceDuoDisplayPosition = displayPosition
    }
}
