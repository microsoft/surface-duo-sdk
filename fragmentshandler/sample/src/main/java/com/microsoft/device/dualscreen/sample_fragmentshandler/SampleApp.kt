/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_fragmentshandler

import android.app.Application
import com.microsoft.device.dualscreen.core.manager.SurfaceDuoScreenManager
import com.microsoft.device.dualscreen.fragmentshandler.FragmentManagerStateHandler

class SampleApp : Application() {
    lateinit var surfaceDuoScreenManager: SurfaceDuoScreenManager

    override fun onCreate() {
        super.onCreate()
        surfaceDuoScreenManager = SurfaceDuoScreenManager.getInstance(this)
        FragmentManagerStateHandler.initialize(this, surfaceDuoScreenManager)
    }
}