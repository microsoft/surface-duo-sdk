/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layout.manager

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.microsoft.device.dualscreen.layout.ScreenHelper
import com.microsoft.device.dualscreen.layout.ScreenMode
import java.lang.ref.WeakReference

class SurfaceDuoScreenManager private constructor(app: Application) : ActivityLifecycle() {

    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    var screenMode = ScreenMode.NOT_DEFINED
    private val screenModeWrappersMap = mutableMapOf<String, ScreenModeListenerWrapper>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isDualMode(activity)) {
            if (screenMode != ScreenMode.DUAL_SCREEN) {
                screenMode =
                    ScreenMode.DUAL_SCREEN
            }
        } else {
            if (screenMode != ScreenMode.SINGLE_SCREEN) {
                screenMode =
                    ScreenMode.SINGLE_SCREEN
            }
        }
    }

    fun addScreenModeListener(owner: LifecycleOwner, listener: ScreenModeListener) {
        var wrapper = screenModeWrappersMap[owner.getMapKey()]
        if (wrapper == null) {
            wrapper = ScreenModeListenerWrapper()
            screenModeWrappersMap[owner.getMapKey()] = wrapper
            owner.lifecycle.addObserver(wrapper)
        }
        wrapper.add(listener)
    }

    fun removeScreenModeListener(owner: LifecycleOwner) {
        screenModeWrappersMap.remove(owner.getMapKey())
    }

    internal inner class ScreenModeListenerWrapper: LifecycleEventObserver {
        private val screenModeListeners = mutableListOf<WeakReference<ScreenModeListener>>()
        private var isActive : Boolean = true

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_START -> if (isActive) {
                    screenModeListeners
                        .filter { it.get() != null }
                        .map { it.get() }
                        .forEach { when (screenMode) {
                            ScreenMode.SINGLE_SCREEN -> it?.onSwitchToSingleScreen()
                            ScreenMode.DUAL_SCREEN -> it?.onSwitchToDualScreen()
                            ScreenMode.NOT_DEFINED -> Unit
                        }}
                }
                Lifecycle.Event.ON_PAUSE -> isActive = false
                Lifecycle.Event.ON_DESTROY -> {
                    screenModeListeners.clear()
                    removeScreenModeListener(source)
                }
                else -> Unit
            }
        }

        internal fun add(listener: ScreenModeListener) {
            this.screenModeListeners.add(WeakReference(listener))
        }
    }

    companion object {
        private var instance: SurfaceDuoScreenManager? = null

        @JvmStatic
        fun init(app: Application): SurfaceDuoScreenManager {
            return instance
                ?: SurfaceDuoScreenManager(
                    app
                ).also {
                instance = it
            }
        }

        @JvmStatic
        fun getHinge(activity: Activity): Rect? {
            return ScreenHelper.getHinge(activity)
        }

        @JvmStatic
        fun isDeviceSurfaceDuo(activity: Activity): Boolean {
            return ScreenHelper.isDeviceSurfaceDuo(activity)
        }

        @JvmStatic
        internal fun getWindowRect(activity: Activity): Rect {
           return ScreenHelper.getWindowRect(activity)
        }

        @JvmStatic
        fun getScreenRectangles(activity: Activity): List<Rect> {
            return ScreenHelper.getScreenRectangles(activity)
        }

        @JvmStatic
        fun isDualMode(activity: Activity): Boolean {
            return ScreenHelper.isDualMode(activity)
        }

        @JvmStatic
        fun getCurrentRotation(activity: Activity): Int {
            return getCurrentRotation(activity)
        }
    }
}

private fun LifecycleOwner.getMapKey(): String =
    this::class.java.simpleName + Integer.toHexString(this.hashCode())
