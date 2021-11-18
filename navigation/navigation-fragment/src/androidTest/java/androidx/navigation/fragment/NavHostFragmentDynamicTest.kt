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

package androidx.navigation.fragment

import androidx.navigation.findFoldableNavController
import androidx.navigation.fragment.test.R
import androidx.navigation.testutils.withActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class NavHostFragmentDynamicTest {

    @Test
    fun testFindNavControllerDynamic() {
        with(ActivityScenario.launch(EmptyActivity::class.java)) {
            val navController = withActivity {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, FoldableNavHostFragment())
                    .commitNow()
                findFoldableNavController(R.id.container)
            }
            assertWithMessage("NavController on the activity's view should be non-null")
                .that(navController)
                .isNotNull()
            val hostRootNavController = withActivity {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.container)!!
                navHostFragment.requireView().findFoldableNavController()
            }
            assertWithMessage("NavController on the host's root view should be non-null")
                .that(hostRootNavController)
                .isNotNull()
        }
    }

    @Test
    fun testFindNavControllerMultipleDynamic() {
        with(ActivityScenario.launch(EmptyActivity::class.java)) {
            val initialNavHostFragment = FoldableNavHostFragment()
            val secondNavHostFragment = FoldableNavHostFragment()
            val navController = withActivity {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, initialNavHostFragment)
                    .commitNow()
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, secondNavHostFragment)
                    .commitNow()
                findFoldableNavController(R.id.container)
            }
            assertWithMessage("NavController on the activity's view should be the last one set")
                .that(navController)
                .isSameInstanceAs(secondNavHostFragment.navController)

            assertWithMessage("Initial host's root view controller should be its NavController")
                .that(initialNavHostFragment.requireView().findFoldableNavController())
                .isSameInstanceAs(initialNavHostFragment.navController)
            assertWithMessage("Second host's root view controller should be its NavController")
                .that(secondNavHostFragment.requireView().findFoldableNavController())
                .isSameInstanceAs(secondNavHostFragment.navController)
        }
    }

    @Test
    fun testFindNavControllerDynamicWithoutId() {
        with(ActivityScenario.launch(EmptyActivity::class.java)) {
            withActivity {
                supportFragmentManager.beginTransaction()
                    .add(FoldableNavHostFragment(), "tag")
                    .commitNow()
            }
            val hostRootNavController = withActivity {
                val navHostFragment = supportFragmentManager.findFragmentByTag("tag")!!
                navHostFragment.requireView().findFoldableNavController()
            }
            assertWithMessage("NavController on the host's root view should be non-null")
                .that(hostRootNavController)
                .isNotNull()
        }
    }
}
