/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.app.Activity
import android.app.Application
import androidx.core.view.doOnNextLayout

/**
 * [SurfaceDuoScreenManager] implementation that uses display mask in order to retrieve screen information.
 */
internal class SurfaceDuoScreenManagerImpl constructor(app: Application) : SurfaceDuoScreenManager {
    /**
     * [ActivityLifecycle] object used to get the current screen info when a new activity is created.
     */
    private val activityLifecycle = object : ActivityLifecycle() {
        override fun onActivityStarted(activity: Activity) {
            super.onActivityStarted(activity)
            currentActivity = activity
            this@SurfaceDuoScreenManagerImpl.onActivityStarted(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            super.onActivityDestroyed(activity)
            currentActivity = null
        }
    }

    /**
     * Current displayed activity.
     */
    private var currentActivity: Activity? = null

    /**
     * Current screen info
     */
    private var currentScreenInfo: ScreenInfo? = null

    /**
     * List of all [ScreenInfoListener] callbacks.
     */
    private val screenInfoListeners = ArrayList<ScreenInfoListener>()

    init {
        app.registerActivityLifecycleCallbacks(activityLifecycle)
    }

    /**
     * Called when an activity was started.
     * @param activity the current activity
     */
    private fun onActivityStarted(activity: Activity) {
        currentScreenInfo = ScreenInfoProvider.getScreenInfo(activity).also { screenInfo ->
            notifyObservers(screenInfo)

            currentActivity?.doOnAttach {
                screenInfo.updateHingeIfNull()
            }
        }
    }

    override val lastKnownScreenInfo: ScreenInfo?
        get() = currentScreenInfo

    /**
     * Add a new listener for changes to the screen info.
     * @param listener the listener to be added
     */
    override fun addScreenInfoListener(listener: ScreenInfoListener?) {
        listener?.let {
            screenInfoListeners.add(listener)
        }

        currentScreenInfo?.let {
            listener?.onScreenInfoChanged(it)
        }
    }

    /**
     * Remove a listener that was previously added with [addScreenInfoListener].
     * @param listener the listener to be removed
     */
    override fun removeScreenInfoListener(listener: ScreenInfoListener?) {
        screenInfoListeners.remove(listener)
    }

    /**
     * This should be called from [Activity.onConfigurationChanged] when config changes are handled by developer.
     * android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
     */
    override fun onConfigurationChanged() {
        val rootView = currentActivity?.window?.decorView?.rootView
        rootView?.doOnNextLayout {
            currentScreenInfo?.let {
                notifyObservers(it)
            }
        }
    }

    /**
     * Notify all listeners that the screen info was changed.
     * @param screenInfo the current screen information
     */
    private fun notifyObservers(screenInfo: ScreenInfo) {
        screenInfoListeners.forEach { it.onScreenInfoChanged(screenInfo) }
    }
}
