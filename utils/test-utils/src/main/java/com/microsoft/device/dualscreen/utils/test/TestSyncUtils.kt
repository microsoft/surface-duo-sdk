/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Simple implementation for [ScreenInfoListener] that saves internally the last screen info data.
 */
class TestSyncUtils {
    private var screenInfoLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)

    /**
     * Resets screen info counter when waiting for screen changes to happen before calling
     * [waitForScreenInfoChanges].
     */
    fun resetScreenInfoCounter() {
        screenInfoLatch = CountDownLatch(COUNT_DOWN_LATCH_COUNT)
    }

    /**
     * Blocks and waits for the next screen info changes to happen.
     * @return {@code true} if the screen info changed before the timeout count reached zero and
     *         {@code false} if the waiting time elapsed before the changes happened.
     */
    fun waitForScreenInfoChanges(): Boolean {
        return try {
            val result = screenInfoLatch.await(10, TimeUnit.SECONDS)
            result
        } catch (e: InterruptedException) {
            false
        }
    }

    companion object {
        private const val COUNT_DOWN_LATCH_COUNT = 1
    }
}