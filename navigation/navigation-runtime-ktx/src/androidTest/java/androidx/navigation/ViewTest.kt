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
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class ViewTest {

    @Test fun findFoldableNavController() {
        val view = View(ApplicationProvider.getApplicationContext() as android.content.Context)
        val navController =
            FoldableNavController(ApplicationProvider.getApplicationContext() as Context)
        FoldableNavigation.setViewNavController(view, navController)

        val foundNavController = view.findFoldableNavController()
        assertTrue(
            "View should have FoldableNavController set",
            foundNavController == navController
        )
    }

    @Test fun findFoldableNavControllerNull() {
        val view = View(ApplicationProvider.getApplicationContext() as android.content.Context)
        try {
            view.findFoldableNavController()
            fail(
                "findFoldableNavController should throw IllegalStateException if a FoldableNavController" +
                    " was not set"
            )
        } catch (e: IllegalStateException) {
            // Expected
        }
    }
}
