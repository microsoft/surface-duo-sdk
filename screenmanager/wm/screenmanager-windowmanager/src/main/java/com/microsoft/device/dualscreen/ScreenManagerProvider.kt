/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.app.Application
import java.lang.IllegalStateException

/**
 * Utility class used to initialize and retrieve [SurfaceDuoScreenManager] object.
 */
object ScreenManagerProvider {
    private var instance: SurfaceDuoScreenManager? = null

    /**
     * Use this method to initialize the screen manager object inside [Application.onCreate]
     */
    @JvmStatic
    @Synchronized
    fun init(application: Application) {
        instance = SurfaceDuoScreenManagerImpl(application)
    }

    /**
     * @return the singleton instance of [SurfaceDuoScreenManager]
     */
    @JvmStatic
    @Synchronized
    fun getScreenManager(): SurfaceDuoScreenManager {
        return instance ?: throw IllegalStateException(this::javaClass.toString() + " must be initialized inside Application#onCreate()")
    }
}