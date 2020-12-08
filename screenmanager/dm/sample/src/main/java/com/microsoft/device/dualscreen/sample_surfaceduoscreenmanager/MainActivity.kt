/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_surfaceduoscreenmanager

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider

class MainActivity : AppCompatActivity(), ScreenInfoListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        if (screenInfo.isDualMode()) {
            Toast.makeText(this@MainActivity, "Dual Screen Mode", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Single Screen Mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(this)
    }

    override fun onPause() {
        super.onPause()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }
}