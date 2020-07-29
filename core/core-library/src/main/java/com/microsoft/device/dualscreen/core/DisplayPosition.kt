package com.microsoft.device.dualscreen.core

enum class DisplayPosition(val id: Int) {
    DUAL(0),
    START(1),
    END(2);

    companion object {
        fun fromId(id: Int): DisplayPosition {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException(
                    "The ScreenMode id doesn't exit"
                )
        }
    }
}