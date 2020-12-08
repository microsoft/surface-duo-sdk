/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.recyclerview

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.recyclerview.SurfaceDuoItemDecoration
import com.microsoft.device.dualscreen.recyclerview.SurfaceDuoLayoutManager
import com.microsoft.device.dualscreen.sample_duolayoutmanager.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ScreenInfoListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.hasFixedSize()
        recyclerView.adapter = NumbersAdapter()
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

    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        recyclerView.layoutManager = SurfaceDuoLayoutManager(this, screenInfo).get()
        recyclerView.addItemDecoration(SurfaceDuoItemDecoration(screenInfo))
    }
}