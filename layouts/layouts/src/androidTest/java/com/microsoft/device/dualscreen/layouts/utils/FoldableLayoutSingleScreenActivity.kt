/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.layouts.test.R

class FoldableLayoutSingleScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_duo_layout)
    }
}
