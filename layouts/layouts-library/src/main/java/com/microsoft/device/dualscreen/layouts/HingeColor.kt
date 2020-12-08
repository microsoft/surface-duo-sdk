/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

/**
 * Enum representing the hinge color, black or white.
 */
internal enum class HingeColor(val id: Int) {
    BLACK(0),
    WHITE(1);

    companion object {
        /**
         * @return the corresponding [HingeColor], otherwise will throw [IllegalArgumentException]
         * @throws [IllegalArgumentException] when the provided id doesn't correspond to any [HingeColor]
         */
        fun fromId(id: Int): HingeColor {
            return values().firstOrNull { it.id == id } ?: throw IllegalArgumentException("The HingeColor id doesn't exist")
        }
    }
}