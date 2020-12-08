/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_fragmentshandler

import android.app.Application
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.fragmentshandler.FragmentManagerStateHandler

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ScreenManagerProvider.init(this)
        FragmentManagerStateHandler.init(this)
    }
}