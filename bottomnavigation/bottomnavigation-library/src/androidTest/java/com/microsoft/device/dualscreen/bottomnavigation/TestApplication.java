/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation;

import android.app.Application;

import com.microsoft.device.dualscreen.ScreenManagerProvider;

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ScreenManagerProvider.init(this);
    }
}
