package com.microsoft.device.surfaceduo.displaysample

import android.app.Application
import com.microsoft.device.dualscreen.layout.SurfaceDuoScreenManager

class DisplaySampleApp : Application() {
    lateinit var surfaceDuoScreenManager: SurfaceDuoScreenManager

    override fun onCreate() {
        super.onCreate()
        surfaceDuoScreenManager = SurfaceDuoScreenManager.init(this)
    }
}