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

package androidx.navigation

import android.content.Context
import androidx.navigation.testutils.TestNavigator
import androidx.navigation.testutils.test
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import org.junit.Assert.assertTrue
import org.junit.Test

@SmallTest
class FoldableNavHostTest {
    private val navController =
        FoldableNavController(ApplicationProvider.getApplicationContext() as Context).apply {
            navigatorProvider += TestNavigator()
        }
    private val navHost = FoldableNavHost { this@FoldableNavHostTest.navController }

    @Test
    fun createGraph() {
        val graph = navHost.createGraph(startDestination = DESTINATION_ID) {
            test(DESTINATION_ID)
        }
        assertTrue(
            "Destination should be added to the graph",
            DESTINATION_ID in graph
        )
    }
}

private const val DESTINATION_ID = 1
