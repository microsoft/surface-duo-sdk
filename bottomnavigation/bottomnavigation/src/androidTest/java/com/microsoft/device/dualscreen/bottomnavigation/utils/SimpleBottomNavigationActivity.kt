/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation.utils

import com.google.android.material.bottomnavigation.compatibility.activities.BaseTestActivity
import com.microsoft.device.dualscreen.bottomnavigation.test.R

class SimpleBottomNavigationActivity : BaseTestActivity() {
    override fun getContentViewLayoutResId(): Int {
        return R.layout.activity_simple_bottomnavigation
    }
}
