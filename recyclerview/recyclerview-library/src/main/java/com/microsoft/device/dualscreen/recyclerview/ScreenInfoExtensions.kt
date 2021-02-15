package com.microsoft.device.dualscreen.recyclerview

import android.view.Surface
import com.microsoft.device.dualscreen.ScreenInfo

fun ScreenInfo.isDeviceInLandscape(): Boolean =
    getScreenRotation() == Surface.ROTATION_0 ||
        getScreenRotation() == Surface.ROTATION_180