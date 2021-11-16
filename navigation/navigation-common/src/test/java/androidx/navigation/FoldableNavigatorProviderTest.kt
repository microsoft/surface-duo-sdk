/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
@RunWith(JUnit4::class)
class FoldableNavigatorProviderTest {
    @Test
    fun addWithMissingAnnotationName() {
        val provider = FoldableNavigatorProvider()
        val navigator = NoNameNavigator()
        try {
            provider.addNavigator(navigator)
            fail(
                "Adding a provider with no @FoldableNavigator.Name should cause an " +
                    "IllegalArgumentException"
            )
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }
    @Test
    fun addWithMissingAnnotationNameGetWithExplicitName() {
        val provider = FoldableNavigatorProvider()
        val navigator = NoNameNavigator()
        provider.addNavigator("name", navigator)
        assertThat(provider.getNavigator<NoNameNavigator>("name"))
            .isEqualTo(navigator)
    }
    @Test
    fun addWithExplicitNameGetWithExplicitName() {
        val provider = FoldableNavigatorProvider()
        val navigator = EmptyNavigator()
        provider.addNavigator("name", navigator)
        assertThat(provider.getNavigator<EmptyNavigator>("name"))
            .isEqualTo(navigator)
        try {
            provider.getNavigator(EmptyNavigator::class.java)
            fail("getNavigator(Class) with an invalid name should cause an IllegalStateException")
        } catch (e: IllegalStateException) {
            // Expected
        }
    }
    @Test
    fun addWithExplicitNameGetWithMissingAnnotationName() {
        val provider = FoldableNavigatorProvider()
        val navigator = NoNameNavigator()
        provider.addNavigator("name", navigator)
        try {
            provider.getNavigator(NoNameNavigator::class.java)
            fail(
                "getNavigator(Class) with no @FoldableNavigator.Name should cause an " +
                    "IllegalArgumentException"
            )
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }
    @Test
    fun addWithAnnotationNameGetWithAnnotationName() {
        val provider = FoldableNavigatorProvider()
        val navigator = EmptyNavigator()
        provider.addNavigator(navigator)
        assertThat(provider.getNavigator(EmptyNavigator::class.java))
            .isEqualTo(navigator)
    }
    @Test
    fun addWithAnnotationNameGetWithExplicitName() {
        val provider = FoldableNavigatorProvider()
        val navigator = EmptyNavigator()
        provider.addNavigator(navigator)
        assertThat(provider.getNavigator<EmptyNavigator>(EmptyNavigator.NAME))
            .isEqualTo(navigator)
    }
}
class NoNameNavigator : FoldableNavigator<FoldableNavDestination>() {
    override fun createDestination(): FoldableNavDestination {
        throw IllegalStateException("createDestination is not supported")
    }
    override fun navigate(
        destination: FoldableNavDestination,
        args: Bundle?,
        navOptions: FoldableNavOptions?,
        navigatorExtras: FoldableNavigator.Extras?
    ): FoldableNavDestination? {
        throw IllegalStateException("navigate is not supported")
    }
    override fun popBackStack(withTransition: Boolean): Boolean {
        throw IllegalStateException("popBackStack is not supported")
    }
}
/**
 * An empty [FoldableNavigator] used to test [FoldableNavigatorProvider].
 */
@FoldableNavigator.Name(EmptyNavigator.NAME)
internal open class EmptyNavigator : FoldableNavigator<FoldableNavDestination>() {
    companion object {
        const val NAME = "empty"
    }
    override fun createDestination(): FoldableNavDestination {
        throw IllegalStateException("createDestination is not supported")
    }
    override fun navigate(
        destination: FoldableNavDestination,
        args: Bundle?,
        navOptions: FoldableNavOptions?,
        navigatorExtras: FoldableNavigator.Extras?
    ): FoldableNavDestination? {
        throw IllegalStateException("navigate is not supported")
    }
    override fun popBackStack(withTransition: Boolean): Boolean {
        throw IllegalStateException("popBackStack is not supported")
    }
}