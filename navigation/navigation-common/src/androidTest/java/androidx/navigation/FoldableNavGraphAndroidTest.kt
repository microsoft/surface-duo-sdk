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

package androidx.navigation

import android.net.Uri
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

@SmallTest
class FoldableNavGraphAndroidTest {
    companion object {
        const val DESTINATION_ID = 1
        const val DESTINATION_LABEL = "test_label"
        const val GRAPH_ID = 2
        const val GRAPH_LABEL = "graph_label"
    }
    @Test
    fun matchDeepLink() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        val idArgument = NavArgument.Builder()
            .setType(NavType.IntType)
            .build()
        graph.addArgument("id", idArgument)
        graph.addDeepLink("www.example.com/users/{id}")
        val match = graph.matchDeepLink(
            Uri.parse("https://www.example.com/users/43")
        )
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should extract id argument correctly")
            .that(match?.matchingArgs?.getInt("id"))
            .isEqualTo(43)
    }
    @Test
    fun matchDeepLinkBestMatchExact() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        graph.addDeepLink("www.example.com/users/index.html")
        val idArgument = NavArgument.Builder()
            .setType(NavType.StringType)
            .build()
        graph.addArgument("id", idArgument)
        graph.addDeepLink("www.example.com/users/{name}")
        val match = graph.matchDeepLink(
            Uri.parse("https://www.example.com/users/index.html")
        )
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should pick the exact match")
            .that(match?.matchingArgs?.size())
            .isEqualTo(0)
    }
    @Test
    fun matchDotStar() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        graph.addDeepLink("www.example.com/.*")
        graph.addDeepLink("www.example.com/{name}")
        val match = graph.matchDeepLink(Uri.parse("https://www.example.com/foo"))
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should pick name over .*")
            .that(match?.matchingArgs?.size())
            .isEqualTo(1)
    }
    @Test
    fun matchDeepLinkBestMatch() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        val idArgument = NavArgument.Builder()
            .setType(NavType.IntType)
            .build()
        graph.addArgument("id", idArgument)
        graph.addDeepLink("www.example.com/users/{id}")
        val postIdArgument = NavArgument.Builder()
            .setType(NavType.IntType)
            .build()
        graph.addArgument("postId", postIdArgument)
        graph.addDeepLink("www.example.com/users/{id}/posts/{postId}")
        val match = graph.matchDeepLink(
            Uri.parse("https://www.example.com/users/43/posts/99")
        )
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should pick the argument with more matching arguments")
            .that(match?.matchingArgs?.size())
            .isEqualTo(2)
        assertWithMessage("Deep link should extract id argument correctly")
            .that(match?.matchingArgs?.getInt("id"))
            .isEqualTo(43)
        assertWithMessage("Deep link should extract postId argument correctly")
            .that(match?.matchingArgs?.getInt("postId"))
            .isEqualTo(99)
    }
    @Test
    fun matchDeepLinkBestMatchPathAndQuery() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        val codeArgument = NavArgument.Builder()
            .setType(NavType.StringType)
            .build()
        graph.addArgument("code", codeArgument)
        graph.addDeepLink("www.example.com/users?code={code}")
        val idArgument = NavArgument.Builder()
            .setType(NavType.StringType)
            .build()
        graph.addArgument("id", idArgument)
        graph.addDeepLink("www.example.com/users?id={id}")
        val match = graph.matchDeepLink(
            Uri.parse("https://www.example.com/users?id=1234")
        )
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should pick the argument with given values")
            .that(match?.matchingArgs?.size())
            .isEqualTo(1)
        assertWithMessage("Deep link should extract id argument correctly")
            .that(match?.matchingArgs?.getString("id"))
            .isEqualTo("1234")
    }
    @Test
    fun matchDeepLinkBestMatchChildren() {
        val navigatorProvider = FoldableNavigatorProvider().apply {
            addNavigator(FoldableNavGraphNavigator(this))
            addNavigator(FoldableNoOpNavigator())
        }
        val graph = navigatorProvider.getNavigator(FoldableNavGraphNavigator::class.java)
            .createDestination()
        val userDestination = navigatorProvider.getNavigator(FoldableNoOpNavigator::class.java)
            .createDestination()
        userDestination.id = 1
        val idArgument = NavArgument.Builder()
            .setType(NavType.IntType)
            .build()
        userDestination.addArgument("id", idArgument)
        userDestination.addDeepLink("www.example.com/users/{id}")
        graph.addDestination(userDestination)
        val postDestination = navigatorProvider.getNavigator(FoldableNoOpNavigator::class.java)
            .createDestination()
        postDestination.id = 2
        val postIdArgument = NavArgument.Builder()
            .setType(NavType.IntType)
            .build()
        postDestination.addArgument("id", idArgument)
        postDestination.addArgument("postId", postIdArgument)
        postDestination.addDeepLink("www.example.com/users/{id}/posts/{postId}")
        graph.addDestination(postDestination)
        val match = graph.matchDeepLink(
            Uri.parse("https://www.example.com/users/43/posts/99")
        )
        assertWithMessage("Deep link should match")
            .that(match)
            .isNotNull()
        assertWithMessage("Deep link should point to correct destination")
            .that(match?.destination)
            .isSameInstanceAs(postDestination)
        assertWithMessage("Deep link should extract id argument correctly")
            .that(match?.matchingArgs?.getInt("id"))
            .isEqualTo(43)
        assertWithMessage("Deep link should extract postId argument correctly")
            .that(match?.matchingArgs?.getInt("postId"))
            .isEqualTo(99)
    }
    @Test
    fun toStringStartDestIdOnly() {
        val navigatorProvider = NavigatorProvider().apply {
            addNavigator(NavGraphNavigator(this))
            addNavigator(NoOpNavigator())
        }
        val graph = navigatorProvider.getNavigator(NavGraphNavigator::class.java)
            .createDestination().apply {
                id = GRAPH_ID
                label = GRAPH_LABEL
                startDestination = DESTINATION_ID
            }
        val expected = "NavGraph(0x${GRAPH_ID.toString(16)}) label=$GRAPH_LABEL " +
            "startDestination=0x${DESTINATION_ID.toString(16)}"
        assertThat(graph.toString()).isEqualTo(expected)
    }
    @Test
    fun toStringStartDestInNodes() {
        val navigatorProvider = NavigatorProvider().apply {
            addNavigator(NavGraphNavigator(this))
            addNavigator(NoOpNavigator())
        }
        val destination = navigatorProvider.getNavigator(NoOpNavigator::class.java)
            .createDestination().apply {
                id = DESTINATION_ID
                label = DESTINATION_LABEL
            }
        val graph = navigatorProvider.getNavigator(NavGraphNavigator::class.java)
            .createDestination().apply {
                id = GRAPH_ID
                label = GRAPH_LABEL
                startDestination = DESTINATION_ID
                addDestination(destination)
            }
        val expected = "NavGraph(0x${GRAPH_ID.toString(16)}) label=$GRAPH_LABEL " +
            "startDestination={NavDestination(0x${DESTINATION_ID.toString(16)}) " +
            "label=$DESTINATION_LABEL}"
        assertThat(graph.toString()).isEqualTo(expected)
    }
}