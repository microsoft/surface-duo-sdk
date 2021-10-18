/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.recyclerview.FoldableItemDecoration
import com.microsoft.device.dualscreen.recyclerview.FoldableLayoutManager
import com.microsoft.device.dualscreen.recyclerview.test.R
import com.microsoft.device.dualscreen.recyclerview.utils.NumbersAdapter
import com.microsoft.device.dualscreen.recyclerview.utils.replaceItemDecorationAt
import java.util.concurrent.Executor

class SimpleRecyclerViewActivity : BaseTestActivity() {

    private lateinit var adapter: WindowInfoRepositoryCallbackAdapter
    private lateinit var consumerWindowLayoutInfo: Consumer<WindowLayoutInfo>
    private lateinit var runOnUiThreadExecutor: Executor

    override fun getContentViewLayoutResId(): Int {
        return R.layout.activity_simple_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRecyclerView()
        initWindowLayoutInfo()
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.hasFixedSize()
        recyclerView.adapter = NumbersAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initWindowLayoutInfo() {
        adapter = WindowInfoRepositoryCallbackAdapter(windowInfoRepository())
        runOnUiThreadExecutor = Executor { command: Runnable? ->
            command?.let {
                Handler(Looper.getMainLooper()).post(it)
            }
        }
        consumerWindowLayoutInfo = Consumer { windowLayoutInfo ->
            onWindowLayoutInfoChanged(windowLayoutInfo)
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

    private fun onWindowLayoutInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = FoldableLayoutManager(this, windowLayoutInfo).get()
        recyclerView.replaceItemDecorationAt(FoldableItemDecoration(windowLayoutInfo))
    }
}
