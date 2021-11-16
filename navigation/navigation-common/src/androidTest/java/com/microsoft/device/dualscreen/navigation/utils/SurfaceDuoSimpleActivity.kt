/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.common.test.R

class SurfaceDuoSimpleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.surface_duo_simple_activity)
    }
}