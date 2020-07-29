/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.core

enum class ScreenMode(val id: Int) {
    SINGLE_SCREEN(0),
    DUAL_SCREEN(1);

    companion object {
        fun fromId(id: Int): ScreenMode {
            return values().firstOrNull { it.id == id } ?: throw IllegalArgumentException(
                "The ScreenMode id doesn't exit"
            )
        }
    }
}