/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.microsoft.device.dualscreen.recyclerview.utils.NumbersStaggeredAdapter
import com.microsoft.device.dualscreen.recyclerview.utils.replaceItemDecorationAt
import com.microsoft.device.dualscreen.sample_duolayoutmanager.R
import com.microsoft.device.dualscreen.sample_duolayoutmanager.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StaggeredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        registerWindowInfoFlow()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_staggered, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_grid -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.adapter = NumbersStaggeredAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun registerWindowInfoFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@StaggeredActivity)
                    .windowLayoutInfo(this@StaggeredActivity)
                    .collect { windowLayoutInfo ->
                        onWindowLayoutInfoChanged(windowLayoutInfo)
                    }
            }
        }
    }

    private fun onWindowLayoutInfoChanged(windowLayoutInfo: WindowLayoutInfo) {
        binding.recyclerView.layoutManager =
            FoldableStaggeredLayoutManager(this, windowLayoutInfo).get()
        binding.recyclerView.replaceItemDecorationAt(
            FoldableStaggeredItemDecoration(windowLayoutInfo)
        )
    }
}
