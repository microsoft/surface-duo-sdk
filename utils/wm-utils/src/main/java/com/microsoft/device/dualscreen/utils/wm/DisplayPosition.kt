/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

/**
 * Enum that represents the children's position when the view is spanned to the entire screen.
 * [DisplayPosition.DUAL] - The view's children are visible to the entire screen
 * [DisplayPosition.START] - The view's children are visible only to the start screen
 * [DisplayPosition.END] - The view's children are visible only to the end screen
 */
enum class DisplayPosition(val id: Int) {
    DUAL(0),
    START(1),
    END(2);

    companion object {
        /**
         * @return the corresponding [DisplayPosition], otherwise will throw [IllegalArgumentException]
         * @throws [IllegalArgumentException] when the provided id doesn't correspond to any [DisplayPosition]
         */
        fun fromResId(id: Int): DisplayPosition {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("The ScreenMode id doesn't exist")
        }
    }
}