/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts.utils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.layouts.test.R

class FoldableLayoutTestOnSecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_start)
        findViewById<Button>(R.id.start).setOnClickListener {
            startActivity(Intent(this, FoldableLayoutSingleScreenActivity::class.java))
        }
    }
}