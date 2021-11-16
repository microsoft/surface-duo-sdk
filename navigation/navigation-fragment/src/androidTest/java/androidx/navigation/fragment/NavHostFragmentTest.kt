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

import android.os.Bundle
import androidx.navigation.findFoldableNavController
import androidx.navigation.fragment.test.NavigationActivity
import androidx.navigation.fragment.test.NavigationActivityWithFragmentTag
import androidx.navigation.fragment.test.NavigationBaseActivity
import androidx.navigation.fragment.test.R
import androidx.navigation.testutils.EmptyFragment
import androidx.navigation.testutils.withActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@MediumTest
@RunWith(Parameterized::class)
class NavHostFragmentTest(
    private val activityClass: Class<NavigationBaseActivity>
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Class<out NavigationBaseActivity>> {
            return arrayOf(
                NavigationActivity::class.java,
                NavigationActivityWithFragmentTag::class.java
            )
        }
    }

    @Test
    fun testFindNavControllerXml() {
        with(ActivityScenario.launch(activityClass)) {
            val navController = withActivity {
                findFoldableNavController(R.id.nav_host)
            }
            assertWithMessage("NavController on the activity's view should be non-null")
                .that(navController)
                .isNotNull()
            val hostRootNavController = withActivity {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host)!!
                navHostFragment.requireView().findFoldableNavController()
            }
            assertWithMessage("NavController on the host's root view should be non-null")
                .that(hostRootNavController)
                .isNotNull()
        }
    }

    @Test
    fun testFindNavControllerRecreate() {
        with(ActivityScenario.launch(activityClass)) {
            val navController = withActivity {
                findFoldableNavController(R.id.nav_host)
            }
            assertWithMessage("NavController on the activity's view should be non-null")
                .that(navController)
                .isNotNull()

            assertWithMessage("NavController graph should be non-null")
                .that(navController.graph)
                .isNotNull()

            recreate()

            val restoredNavController = withActivity {
                findFoldableNavController(R.id.nav_host)
            }

            assertWithMessage("NavController on the activity's view should be non-null")
                .that(restoredNavController)
                .isNotNull()

            assertWithMessage("NavController graph should be non-null")
                .that(restoredNavController.graph)
                .isNotNull()
        }
    }
}

class NavControllerInOnCreateFragment : EmptyFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navController = FoldableNavHostFragment.findNavController(this)
        assertWithMessage("The NavController's graph should be set")
            .that(navController.graph)
            .isNotNull()
    }
}