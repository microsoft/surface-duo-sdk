/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_surfaceduoscreenmanager

import android.app.Application
import com.microsoft.device.dualscreen.core.manager.SurfaceDuoScreenManager

class SampleApp : Application() {
    lateinit var surfaceDuoScreenManager: SurfaceDuoScreenManager

    override fun onCreate() {
        super.onCreate()
        surfaceDuoScreenManager = SurfaceDuoScreenManager.getInstance(this)
    }
}