/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.activities

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.recyclerview.FoldableItemDecoration
import com.microsoft.device.dualscreen.recyclerview.FoldableLayoutManager
import com.microsoft.device.dualscreen.recyclerview.test.R
import com.microsoft.device.dualscreen.recyclerview.utils.NumbersAdapter
import com.microsoft.device.dualscreen.recyclerview.utils.replaceItemDecorationAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SimpleRecyclerViewActivity : BaseTestActivity() {

    override fun getContentViewLayoutResId(): Int {
        return R.layout.activity_simple_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRecyclerView()
        registerWindowInfoFlow()
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.hasFixedSize()
        recyclerView.adapter = NumbersAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun registerWindowInfoFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@SimpleRecyclerViewActivity)
                    .windowLayoutInfo(this@SimpleRecyclerViewActivity)
                    .collect { windowLayoutInfo ->
                        onWindowLayoutInfoChanged(windowLayoutInfo)
                    }
            }
        }
    }

    private fun onWindowLayoutInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = FoldableLayoutManager(this, windowLayoutInfo).get()
        recyclerView.replaceItemDecorationAt(FoldableItemDecoration(windowLayoutInfo))
    }
}
