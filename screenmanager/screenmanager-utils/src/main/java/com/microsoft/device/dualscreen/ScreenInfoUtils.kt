/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen

val ScreenInfo.screenMode: ScreenMode
    get() = when {
        isDualMode() -> ScreenMode.DUAL_SCREEN
        else -> ScreenMode.SINGLE_SCREEN
    }