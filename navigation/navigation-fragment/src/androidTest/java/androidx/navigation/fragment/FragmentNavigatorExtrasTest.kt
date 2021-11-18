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

package androidx.navigation.fragment

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FragmentNavigatorExtrasTest {

    @Test
    fun testAddSharedElement() {
        val view = View(InstrumentationRegistry.getInstrumentation().targetContext)
        val extras = FoldableFragmentNavigator.Extras.Builder()
            .addSharedElement(view, "test")
            .build()
        val sharedElements = extras.sharedElements
        assertEquals("Should be one shared element", 1, sharedElements.size)
        val name = sharedElements[view]
        assertNotNull("Shared element should exist in the map", name)
        assertEquals("Shared element's name should match", "test", name)
    }

    @Test
    fun testAddSharedElements() {
        val map = mapOf(
            View(InstrumentationRegistry.getInstrumentation().targetContext) to "test1",
            View(InstrumentationRegistry.getInstrumentation().targetContext) to "test2"
        )
        val extras = FoldableFragmentNavigator.Extras.Builder()
            .addSharedElements(map)
            .build()
        val sharedElements = extras.sharedElements
        assertEquals("Should be ${map.size} shared elements", map.size, sharedElements.size)
        map.forEach { (view, expected) ->
            val name = sharedElements[view]
            assertNotNull("Shared element should exist in the map", name)
            assertEquals("Shared element's name should match", expected, name)
        }
    }
}
