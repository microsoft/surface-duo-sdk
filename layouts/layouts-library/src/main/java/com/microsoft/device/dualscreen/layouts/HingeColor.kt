/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

internal enum class HingeColor(val id: Int) {
    BLACK(0),
    WHITE(1);

    companion object {
        fun fromId(id: Int): HingeColor {
            return values().firstOrNull { it.id == id } ?: throw IllegalArgumentException(
                "The HingeColor id doesn't exit"
            )
        }
    }
}