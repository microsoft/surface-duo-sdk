/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.navigation.ui

import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.FoldableNavController
import androidx.navigation.createGraph
import androidx.navigation.plusAssign
import androidx.navigation.testutils.TestNavigator
import androidx.navigation.testutils.test
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AppBarConfigurationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testTopLevelFromGraph() {
        val navGraph = FoldableNavController(context).apply {
            navigatorProvider += TestNavigator()
        }.createGraph(startDestination = 1) {
            test(1)
            test(2)
        }
        val builder = FoldableAppBarConfiguration.Builder(navGraph)
        val appBarConfiguration = builder.build()
        assertThat(appBarConfiguration.topLevelDestinations).containsExactly(1)
    }

    @Test
    fun testTopLevelFromVarargs() {
        val builder = FoldableAppBarConfiguration.Builder(1)
        val appBarConfiguration = builder.build()
        assertThat(appBarConfiguration.topLevelDestinations).containsExactly(1)
    }

    @Test
    fun testTopLevelFromSet() {
        val builder = FoldableAppBarConfiguration.Builder(setOf(1, 2))
        val appBarConfiguration = builder.build()
        assertThat(appBarConfiguration.topLevelDestinations).containsExactly(1, 2)
    }

    @Test
    fun testSetOpenableLayout() {
        val builder = FoldableAppBarConfiguration.Builder()
        val drawerLayout = DrawerLayout(context)
        builder.setOpenableLayout(drawerLayout)
        val appBarConfiguration = builder.build()
        assertThat(appBarConfiguration.openableLayout).isEqualTo(drawerLayout)
    }

    @Test
    fun testSetFallbackOnNavigateUpListener() {
        val builder = FoldableAppBarConfiguration.Builder()
        val onNavigateUpListener = FoldableAppBarConfiguration.OnNavigateUpListener {
            false
        }
        builder.setFallbackOnNavigateUpListener(onNavigateUpListener)
        val appBarConfiguration = builder.build()
        assertThat(appBarConfiguration.fallbackOnNavigateUpListener)
            .isEqualTo(onNavigateUpListener)
    }
}
