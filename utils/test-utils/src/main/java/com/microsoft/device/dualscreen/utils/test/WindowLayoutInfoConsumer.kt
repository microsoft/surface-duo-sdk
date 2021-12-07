/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.test

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.window.java.layout.WindowInfoRepositoryCallbackAdapter
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class WindowLayoutInfoConsumer : Consumer<WindowLayoutInfo> {
    private var _windowLayoutInfo: WindowLayoutInfo? = null
    val windowLayoutInfo: WindowLayoutInfo?
        get() = _windowLayoutInfo
    private var windowLayoutInfoLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)

    private var adapter: WindowInfoRepositoryCallbackAdapter? = null
    private val runOnUiThreadExecutor = Executor { command: Runnable? ->
        command?.let {
            Handler(Looper.getMainLooper()).post(it)
        }
    }

    fun register(activity: AppCompatActivity) {
        adapter = WindowInfoRepositoryCallbackAdapter(activity.windowInfoRepository())
        adapter?.addWindowLayoutInfoListener(runOnUiThreadExecutor, this)
    }

    fun unregister() {
        adapter?.removeWindowLayoutInfoListener(this)
    }

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

    fun reset() {
        unregister()
        resetWindowInfoLayout()
        resetWindowInfoLayoutCounter()
    }

    companion object {
        private const val COUNT_DOWN_LATCH_COUNT = 1
        private const val TIMEOUT_IN_SECONDS = 3L
    }
}