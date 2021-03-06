/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.screenmanager.tests.utils

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.screenmanager.tests.R

/**
 * Simple activity used for testing purpose.
 */
class SampleActivity : AppCompatActivity() {
    val containerView: View
        get() = findViewById(R.id.simple_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ScreenManagerProvider.getScreenManager().onConfigurationChanged()
    }
}