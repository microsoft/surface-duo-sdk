/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview

import android.view.Surface
import com.microsoft.device.dualscreen.ScreenInfo

fun ScreenInfo.isDeviceInLandscape(): Boolean =
    getScreenRotation() == Surface.ROTATION_0 ||
        getScreenRotation() == Surface.ROTATION_180
