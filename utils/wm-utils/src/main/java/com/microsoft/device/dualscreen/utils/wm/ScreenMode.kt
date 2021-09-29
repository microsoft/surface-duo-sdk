/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

/**
 * Enum that represents the view spanning state inside the entire screen.
 *
 * [ScreenMode.SINGLE_SCREEN] - The view is spanned only to one screen.
 * [ScreenMode.DUAL_SCREEN] - The view is spanned to the entire screen.
 */
enum class ScreenMode(val id: Int) {
    SINGLE_SCREEN(0),
    DUAL_SCREEN(1);

    companion object {
        /**
         * @return the corresponding [ScreenMode], otherwise will throw [IllegalArgumentException]
         * @throws [IllegalArgumentException] when the provided id doesn't correspond to any [ScreenMode]
         */
        fun fromId(id: Int): ScreenMode {
            return values().firstOrNull { it.id == id } ?: throw IllegalArgumentException("The ScreenMode id doesn't exist")
        }
    }
}