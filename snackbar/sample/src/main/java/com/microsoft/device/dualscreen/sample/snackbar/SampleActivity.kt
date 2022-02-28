/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.snackbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.microsoft.device.dualscreen.sample.snackbar.databinding.ActivitySampleBinding
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.BOTH
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.END
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.START
import com.microsoft.device.dualscreen.snackbar.show

class SampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.showSnackbarToStart.setOnClickListener {
            showSnackbar(getString(R.string.snackbar_to_start), START)
        }

        binding.showSnackbarToEnd.setOnClickListener {
            showSnackbar(getString(R.string.snackbar_to_end), END)
        }

        binding.showSnackbarToBoth.setOnClickListener {
            showSnackbar(getString(R.string.snackbar_to_both), BOTH)
        }
    }

    private fun showSnackbar(message: String, position: SnackbarPosition) {
        Snackbar.make(binding.snackbarContainer.coordinatorLayout, message, LENGTH_LONG)
            .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            .setAction(R.string.cancel) { }
            .show(binding.snackbarContainer, position)
    }
}