/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation;

public enum LaunchScreen {
    DEFAULT(0), START(1), END(2), BOTH(3);

    private final int value;

    LaunchScreen(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LaunchScreen fromValue(int value) {
        switch (value) {
            case 1:
                return LaunchScreen.START;
            case 2:
                return LaunchScreen.END;
            case 3:
                return LaunchScreen.BOTH;
            default:
                return LaunchScreen.DEFAULT;
        }
    }
}
