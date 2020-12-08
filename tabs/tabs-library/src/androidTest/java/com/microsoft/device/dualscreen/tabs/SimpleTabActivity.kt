/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.tabs

import com.microsoft.device.dualscreen.tabs.compatibility.activities.BaseTestActivity
import com.microsoft.device.dualscreen.tabs.test.R

class SimpleTabActivity : BaseTestActivity() {
    override fun getContentViewLayoutResId(): Int {
        return R.layout.activity_simple_tab
    }
}
