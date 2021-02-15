/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.activities

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.recyclerview.SurfaceDuoItemDecoration
import com.microsoft.device.dualscreen.recyclerview.SurfaceDuoLayoutManager
import com.microsoft.device.dualscreen.recyclerview.test.R
import com.microsoft.device.dualscreen.recyclerview.utils.NumbersAdapter

class SimpleRecyclerViewActivity : BaseTestActivity(), ScreenInfoListener {

    override fun getContentViewLayoutResId(): Int {
        return R.layout.activity_simple_surfaceduo_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
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

    override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = SurfaceDuoLayoutManager(this, screenInfo).get()
        recyclerView.addItemDecoration(SurfaceDuoItemDecoration(screenInfo))
    }
}
