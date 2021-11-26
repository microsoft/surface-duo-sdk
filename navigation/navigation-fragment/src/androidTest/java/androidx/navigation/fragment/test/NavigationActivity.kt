/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions Copyright (c) Microsoft Corporation
 */

package androidx.navigation.fragment.test

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findFoldableNavController
import java.util.concurrent.CountDownLatch

class NavigationActivity : NavigationBaseActivity(R.layout.navigation_activity)

class NavigationActivityWithFragmentTag : NavigationBaseActivity(
    R.layout.navigation_activity_fragment_tag
)

class NavigationActivityMultiNavHost : NavigationBaseActivity(R.layout.navigation_activity_nav_host)

open class NavigationBaseActivity(contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    val navController get() = findFoldableNavController(R.id.nav_host)

    val finishCountDownLatch = CountDownLatch(1)

    override fun finish() {
        super.finish()
        finishCountDownLatch.countDown()
    }
}