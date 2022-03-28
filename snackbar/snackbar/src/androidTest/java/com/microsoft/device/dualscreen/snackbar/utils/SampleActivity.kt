/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition
import com.microsoft.device.dualscreen.snackbar.show
import com.microsoft.device.dualscreen.snackbar.test.R
import com.microsoft.device.dualscreen.snackbar.test.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun showSnackbar(message: String, position: SnackbarPosition, duration: Int) {
        Snackbar.make(binding.snackbarContainer.coordinatorLayout, message, duration)
            .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            .setAction(R.string.cancel) { }
            .show(binding.snackbarContainer, position)
    }
}