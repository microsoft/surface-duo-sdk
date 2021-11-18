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
import androidx.fragment.app.Fragment
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
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(Parameterized::class)
class OnBackPressedTest(
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
    fun testOnBackPressedOnRoot() {
        with(ActivityScenario.launch(activityClass)) {
            val countDownLatch = withActivity {
                navController.setGraph(R.navigation.nav_simple)
                onBackPressed()
                finishCountDownLatch
            }
            assertWithMessage("onBackPressed() should finish the activity on the root")
                .that(countDownLatch.await(1, TimeUnit.SECONDS))
                .isTrue()
        }
    }

    @Test
    fun testOnBackPressedAfterNavigate() {
        with(ActivityScenario.launch(activityClass)) {
            withActivity {
                navController.setGraph(R.navigation.nav_simple)
                navController.navigate(R.id.empty_fragment)
                onBackPressed()
                assertWithMessage("onBackPressed() should trigger NavController.popBackStack()")
                    .that(navController.currentDestination?.id)
                    .isEqualTo(R.id.start_fragment)
            }
        }
    }

    @Test
    fun testOnBackPressedAfterNavigate_notDefaultNavHost() {
        with(ActivityScenario.launch(activityClass)) {
            val countDownLatch = withActivity {
                navController.setGraph(R.navigation.nav_simple)
                navController.navigate(R.id.empty_fragment)
                supportFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(null)
                    .commitNow()

                onBackPressed()
                finishCountDownLatch
            }
            assertWithMessage(
                "onBackPressed() should finish the activity when not the " +
                    "primary nav"
            )
                .that(countDownLatch.await(1, TimeUnit.SECONDS))
                .isTrue()
        }
    }

    @Test
    fun testOnBackPressedWithChildBackStack() {
        with(ActivityScenario.launch(activityClass)) {
            withActivity {
                val navHostFragment = supportFragmentManager.primaryNavigationFragment
                    as FoldableNavHostFragment
                val navHostFragmentManager = navHostFragment.childFragmentManager
                val navController = navHostFragment.navController
                navController.setGraph(R.navigation.nav_simple)
                navController.navigate(R.id.child_back_stack_fragment)
                navHostFragmentManager.executePendingTransactions()

                val currentFragment = navHostFragmentManager.primaryNavigationFragment
                    as ChildBackStackFragment
                assertWithMessage("Current Fragment should have a child Fragment by default")
                    .that(currentFragment.childFragment)
                    .isNotNull()

                onBackPressed()
                assertWithMessage(
                    "onBackPressed() should not trigger NavController when there " +
                        "is a child back stack"
                )
                    .that(navController.currentDestination?.id)
                    .isEqualTo(R.id.child_back_stack_fragment)
                assertWithMessage("Child Fragment should be popped")
                    .that(currentFragment.childFragment)
                    .isNull()
            }
        }
    }
}

class ChildBackStackFragment : EmptyFragment() {
    val childFragment get() = childFragmentManager.findFragmentByTag("child")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction()
            .add(Fragment(), "child")
            .addToBackStack(null)
            .commit()
    }
}