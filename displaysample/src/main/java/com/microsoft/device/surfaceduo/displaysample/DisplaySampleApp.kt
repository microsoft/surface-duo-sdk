package com.microsoft.device.surfaceduo.displaysample

import android.app.Application
import com.microsoft.device.surfaceduo.display.SurfaceDuoScreenManager

class DisplaySampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SurfaceDuoScreenManager.init(this)
    }
}