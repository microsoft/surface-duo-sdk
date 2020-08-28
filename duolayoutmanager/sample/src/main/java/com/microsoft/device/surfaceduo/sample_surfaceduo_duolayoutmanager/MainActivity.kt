/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_duolayoutmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.duolayoutmanager.DuoItemDecoration
import com.microsoft.device.dualscreen.duolayoutmanager.DuoLayoutManager
import com.microsoft.device.dualscreen.sample_duolayoutmanager.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.hasFixedSize()
        recyclerView.adapter = NumbersAdapter()

        recyclerView.layoutManager = DuoLayoutManager(this).get()
        recyclerView.addItemDecoration(DuoItemDecoration())
    }
}