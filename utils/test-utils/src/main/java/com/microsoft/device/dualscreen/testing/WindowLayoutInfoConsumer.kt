/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.core.util.Consumer
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Extends a Consumer<WindowLayoutInfo> {@see Consumer<WindowLayoutInfo>} that an Activity {@see AppCompatActivity}
 * can use to reset previous WindowLayoutInfo {@see WindowLayoutInfo} data and to wait for new WindowLayoutInfo
 * changes.
 *
 * @constructor Create empty Window layout info consumer
 */
class WindowLayoutInfoConsumer : Consumer<WindowLayoutInfo> {
    private var _windowLayoutInfo: WindowLayoutInfo? = null
    val windowLayoutInfo: WindowLayoutInfo?
        get() = _windowLayoutInfo
    private var windowLayoutInfoLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)

    private var adapter: WindowInfoTrackerCallbackAdapter? = null
    private val runOnUiThreadExecutor = Executor { command: Runnable? ->
        command?.let {
            Handler(Looper.getMainLooper()).post(it)
        }
    }

    /**
     * Register a listener to consume WindowLayoutInfo values.
     *
     * @param activity : a valid Context
     */
    fun register(activity: Activity) {
        adapter = WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(activity))
        adapter?.addWindowLayoutInfoListener(activity, runOnUiThreadExecutor, this)
    }

    /**
     * Unregister the current instance from the WindowInfoTrackerCallbackAdapter.
     */
    fun unregister() {
        adapter?.removeWindowLayoutInfoListener(this)
        reset()
    }

    /**
     * Accepts a WindowLayoutInfo {@see WindowLayoutInfo} with the new information that will handle.
     *
     * @param windowLayoutInfo : with new window layout information
     */
    override fun accept(windowLayoutInfo: WindowLayoutInfo) {
        _windowLayoutInfo = windowLayoutInfo
        windowLayoutInfoLatch.countDown()
    }

    /**
     * Resets the last window layout info to {@code null}
     */
    fun resetWindowInfoLayout() {
        _windowLayoutInfo = null
    }

    /**
     * Resets window layout info counter when waiting for window layout changes to happen before calling
     * [waitForWindowInfoLayoutChanges].
     */
    fun resetWindowInfoLayoutCounter() {
        windowLayoutInfoLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)
    }

    /**
     * Blocks and waits for the next window layout info changes to happen.
     * @return {@code true} if the window layout info changed before the timeout count reached zero and
     *         {@code false} if the waiting time elapsed before the changes happened.
     */
    fun waitForWindowInfoLayoutChanges(): Boolean {
        return try {
            val result = windowLayoutInfoLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            result
        } catch (e: InterruptedException) {
            false
        }
    }

    /**
     * Resets current used instance to its original state.
     */
    fun reset() {
        resetWindowInfoLayout()
        resetWindowInfoLayoutCounter()
    }

    /**
     * Constants that this class uses in order to wait for changes.
     */
    companion object {
        private const val COUNT_DOWN_LATCH_COUNT = 1
        private const val TIMEOUT_IN_SECONDS = 3L
    }
}
