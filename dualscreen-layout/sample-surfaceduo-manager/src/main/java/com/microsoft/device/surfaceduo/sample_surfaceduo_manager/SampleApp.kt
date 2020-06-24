/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.sample_surfaceduo_manager

import android.app.Application
import com.microsoft.device.dualscreen.layout.manager.SurfaceDuoScreenManager

class SampleApp : Application() {
    lateinit var surfaceDuoScreenManager: SurfaceDuoScreenManager

    override fun onCreate() {
        super.onCreate()
        surfaceDuoScreenManager = SurfaceDuoScreenManager.init(this)
    }
}