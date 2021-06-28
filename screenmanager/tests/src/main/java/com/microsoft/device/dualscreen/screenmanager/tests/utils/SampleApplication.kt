/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.screenmanager.tests.utils

import android.app.Application
import com.microsoft.device.dualscreen.ScreenManagerProvider

/**
 * Simple application used for testing purpose.
 */
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScreenManagerProvider.init(this)
    }
}