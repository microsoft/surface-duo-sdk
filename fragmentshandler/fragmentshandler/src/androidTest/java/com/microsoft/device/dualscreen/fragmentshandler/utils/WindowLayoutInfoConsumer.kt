/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.fragmentshandler.utils

import com.microsoft.device.dualscreen.utils.test.WindowLayoutInfoConsumer

fun WindowLayoutInfoConsumer.runAction(action: () -> Unit) {
    resetWindowInfoLayoutCounter()
    action()
    waitForWindowInfoLayoutChanges()
}