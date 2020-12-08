/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_viewwrapper

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
    }

    override fun onStart() {
        super.onStart()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(this)
    }

    override fun onPause() {
        super.onPause()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(this)
    }

    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        setButtonsVisibility(screenInfo)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
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
