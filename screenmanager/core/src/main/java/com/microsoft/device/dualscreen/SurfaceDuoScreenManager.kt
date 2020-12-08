/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

import android.app.Activity

/**
 * Interface that defines methods used to register/unregister screen info listeners.
 */
interface SurfaceDuoScreenManager {
    /**
     * Add a new listener for changes to the screen info.
     * @param listener the listener to be added
     */
    fun addScreenInfoListener(listener: ScreenInfoListener?)

    /**
     * Remove a listener that was previously added with [addScreenInfoListener].
     * @param listener the listener to be removed
     */
    fun removeScreenInfoListener(listener: ScreenInfoListener?)

    /**
     * This should be called from [Activity.onConfigurationChanged] when config changes are handled by developer.
     * android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
     */
    fun onConfigurationChanged()
}