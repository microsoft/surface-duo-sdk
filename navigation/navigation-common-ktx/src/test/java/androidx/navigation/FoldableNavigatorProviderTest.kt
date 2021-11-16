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
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
@RunWith(JUnit4::class)
class FoldableNavigatorProviderTest {
    private val provider = FoldableNavigatorProvider()
    @Test
    fun set() {
        val navigator = FoldableNoOpNavigator()
        provider[NAME] = navigator
        val foundNavigator: FoldableNavigator<FoldableNavDestination> = provider[NAME]
        assertWithMessage("Set destination should be retrieved with get")
            .that(foundNavigator)
            .isSameInstanceAs(navigator)
    }
    @Test
    fun plusAssign() {
        val navigator = FoldableNoOpNavigator()
        provider += navigator
        assertWithMessage("Set destination should be retrieved with get")
            .that(provider[FoldableNoOpNavigator::class])
            .isSameInstanceAs(navigator)
    }
}
private const val NAME = "TEST"