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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Lifecycle
import androidx.navigation.FoldableNavOptions
import androidx.navigation.fragment.test.R
import androidx.navigation.testutils.EmptyFragment
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.microsoft.device.dualscreen.navigation.FoldableFragmentManagerWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FragmentNavigatorTest {

    companion object {
        private const val INITIAL_FRAGMENT = 1
        private const val SECOND_FRAGMENT = 2
        private const val THIRD_FRAGMENT = 3
        private const val FOURTH_FRAGMENT = 4
        private const val TEST_LABEL = "test_label"
    }

    @Suppress("DEPRECATION")
    @get:Rule
    var activityRule = androidx.test.rule.ActivityTestRule(EmptyActivity::class.java)

    private lateinit var emptyActivity: EmptyActivity
    private lateinit var fragmentManagerWrapper: FoldableFragmentManagerWrapper

    @Before
    fun setup() {
        emptyActivity = activityRule.activity
        fragmentManagerWrapper = FoldableFragmentManagerWrapper(emptyActivity.supportFragmentManager)
    }

    @UiThreadTest
    @Test
    fun testNavigate() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination().apply {
            id = INITIAL_FRAGMENT
            className = EmptyFragment::class.java.name
        }

        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)
        assertEquals(
            "Fragment should be the correct type",
            EmptyFragment::class.java,
            fragment!!::class.java
        )
        assertEquals(
            "Fragment should be the primary navigation Fragment",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testNavigateWithFragmentFactory() {
        fragmentManagerWrapper.fragmentManager.fragmentFactory = NonEmptyFragmentFactory()
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination().apply {
            id = INITIAL_FRAGMENT
            className = NonEmptyConstructorFragment::class.java.name
        }

        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Fragment should be added")
            .that(fragment)
            .isNotNull()
        assertWithMessage("Fragment should be the correct type")
            .that(fragment)
            .isInstanceOf(NonEmptyConstructorFragment::class.java)
        assertWithMessage("Fragment should be the primary navigation Fragment")
            .that(fragment)
            .isSameInstanceAs(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
    }

    @UiThreadTest
    @Test
    fun testNavigateTwice() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination().apply {
            id = INITIAL_FRAGMENT
            className = EmptyFragment::class.java.name
        }

        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)
        assertEquals(
            "Fragment should be the correct type",
            EmptyFragment::class.java,
            fragment!!::class.java
        )
        assertEquals(
            "Fragment should be the primary navigation Fragment",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Now push a second fragment
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertEquals(
            "Replacement Fragment should be the correct type",
            EmptyFragment::class.java,
            replacementFragment!!::class.java
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testNavigateWithPopUpToThenPop() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // Push initial fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()

        // Push a second fragment
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()

        // Pop and then push third fragment, simulating popUpTo to initial.
        val success = fragmentNavigator.popBackStack(true)
        assertTrue("FragmentNavigator should return true when popping the third fragment", success)
        destination.id = THIRD_FRAGMENT
        assertThat(
            fragmentNavigator.navigate(
                destination,
                null,
                FoldableNavOptions.Builder().setPopUpTo(INITIAL_FRAGMENT, false).build(),
                null
            )
        )
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()

        // Now pop the Fragment
        val popped = fragmentNavigator.popBackStack(true)
        assertTrue("FragmentNavigator should return true when popping the third fragment", popped)
    }

    @UiThreadTest
    @Test
    fun testSingleTopInitial() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.className = EmptyFragment::class.java.name

        fragmentNavigator.navigate(destination, null, null, null)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)
        val lifecycle = fragment!!.lifecycle

        assertThat(
            fragmentNavigator.navigate(
                destination,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        )
            .isNull()
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
        assertNotEquals(
            "Replacement should be a new instance",
            fragment,
            replacementFragment
        )
        assertEquals(
            "Old instance should be destroyed",
            Lifecycle.State.DESTROYED,
            lifecycle.currentState
        )
    }

    @UiThreadTest
    @Test
    fun testSingleTop() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.className = EmptyFragment::class.java.name

        // First push an initial Fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val initialFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Initial Fragment should be added")
            .that(initialFragment)
            .isNotNull()

        // Now push the Fragment that we want to replace with a singleTop operation
        destination.id = 1
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Fragment should be added")
            .that(fragment)
            .isNotNull()
        val lifecycle = fragment!!.lifecycle

        assertThat(
            fragmentNavigator.navigate(
                destination,
                null,
                FoldableNavOptions.Builder().setLaunchSingleTop(true).build(),
                null
            )
        )
            .isNull()
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Replacement Fragment should be added")
            .that(replacementFragment)
            .isNotNull()
        assertWithMessage("Replacement Fragment should be the correct type")
            .that(replacementFragment)
            .isInstanceOf(EmptyFragment::class.java)
        assertWithMessage("Replacement Fragment should be the primary navigation Fragment")
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isSameInstanceAs(replacementFragment)
        assertWithMessage("Replacement should be a new instance")
            .that(replacementFragment)
            .isNotSameInstanceAs(fragment)
        assertWithMessage("Old instance should be destroyed")
            .that(lifecycle.currentState)
            .isEqualTo(Lifecycle.State.DESTROYED)

        assertThat(fragmentNavigator.popBackStack(true))
            .isTrue()
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertWithMessage("Initial Fragment should be on top of back stack after pop")
            .that(fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id))
            .isSameInstanceAs(initialFragment)
        assertWithMessage("Initial Fragment should be the primary navigation Fragment")
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isSameInstanceAs(initialFragment)
    }

    @UiThreadTest
    @Test
    fun testPopInitial() {
        val fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push an initial Fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)

        // Now pop the initial Fragment
        val popped = fragmentNavigator.popBackStack(true)
        assertWithMessage(
            "FragmentNavigator should return false when popping " +
                "the initial Fragment"
        )
            .that(popped)
            .isTrue()
    }

    @UiThreadTest
    @Test
    fun testPop() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push an initial Fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)

        // Now push the Fragment that we want to pop
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Now pop the Fragment
        val popped = fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertTrue("FragmentNavigator should return true when popping a Fragment", popped)
        assertEquals(
            "Fragment should be the primary navigation Fragment after pop",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testPopWithSameDestinationTwice() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push an initial Fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Fragment should be added")
            .that(fragment)
            .isNotNull()

        // Now push a second Fragment
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Replacement Fragment should be added")
            .that(replacementFragment)
            .isNotNull()
        assertWithMessage("Replacement Fragment should be the primary navigation Fragment")
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isSameInstanceAs(replacementFragment)

        // Push the same Fragment a second time, creating a stack of two
        // identical Fragments
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragmentToPop = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertWithMessage("Fragment to pop should be added")
            .that(fragmentToPop)
            .isNotNull()
        assertWithMessage("Fragment to pop should be the primary navigation Fragment")
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isSameInstanceAs(fragmentToPop)

        // Now pop the Fragment
        val popped = fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertWithMessage("FragmentNavigator should return true when popping a Fragment")
            .that(popped)
            .isTrue()
        assertWithMessage(
            "Replacement Fragment should be the primary navigation Fragment " +
                "after pop"
        )
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isSameInstanceAs(replacementFragment)
    }

    @UiThreadTest
    @Test
    fun testPopWithChildFragmentBackStack() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push an initial Fragment
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)

        // Now push the Fragment that we want to pop
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Add a Fragment to the replacementFragment's childFragmentManager back stack
        replacementFragment?.childFragmentManager?.run {
            beginTransaction()
                .add(EmptyFragment(), "child")
                .addToBackStack(null)
                .commit()
            executePendingTransactions()
        }

        // Now pop the Fragment
        val popped = fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertTrue("FragmentNavigator should return true when popping a Fragment", popped)
        assertEquals(
            "Fragment should be the primary navigation Fragment after pop",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testDeepLinkPop() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push two Fragments as our 'deep link'
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)

        // Now push the Fragment that we want to pop
        destination.id = THIRD_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Now pop the Fragment
        fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertEquals(
            "Fragment should be the primary navigation Fragment after pop",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testDeepLinkPopWithSaveState() {
        var fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        // First push two Fragments as our 'deep link'
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)

        // Now push the Fragment that we want to pop
        destination.id = THIRD_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Create a new FragmentNavigator, replacing the previous one
        val savedState = fragmentNavigator.onSaveState()
        fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        fragmentNavigator.onRestoreState(savedState)

        // Now pop the Fragment
        fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        val fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertEquals(
            "Fragment should be the primary navigation Fragment after pop",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testNavigateThenPopAfterSaveState() {
        var fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        val destination = fragmentNavigator.createDestination()
        destination.id = INITIAL_FRAGMENT
        destination.className = EmptyFragment::class.java.name

        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        var fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Fragment should be added", fragment)
        assertEquals(
            "Fragment should be the correct type",
            EmptyFragment::class.java,
            fragment!!::class.java
        )
        assertEquals(
            "Fragment should be the primary navigation Fragment",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Now push a second fragment
        destination.id = SECOND_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        var replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertEquals(
            "Replacement Fragment should be the correct type",
            EmptyFragment::class.java,
            replacementFragment!!::class.java
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Create a new FragmentNavigator, replacing the previous one
        val savedState = fragmentNavigator.onSaveState()
        fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        fragmentNavigator.onRestoreState(savedState)

        // Now push a third fragment after the state save
        destination.id = THIRD_FRAGMENT
        assertThat(fragmentNavigator.navigate(destination, null, null, null))
            .isEqualTo(destination)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        replacementFragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertNotNull("Replacement Fragment should be added", replacementFragment)
        assertTrue(
            "Replacement Fragment should be the correct type",
            replacementFragment is EmptyFragment
        )
        assertEquals(
            "Replacement Fragment should be the primary navigation Fragment",
            replacementFragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )

        // Now pop the Fragment
        fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        fragment = fragmentManagerWrapper.fragmentManager.findFragmentById(R.id.first_container_id)
        assertEquals(
            "Fragment should be the primary navigation Fragment after pop",
            fragment,
            fragmentManagerWrapper.fragmentManager.primaryNavigationFragment
        )
    }

    @UiThreadTest
    @Test
    fun testMultipleNavigateFragmentTransactionsThenPop() {
        val fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        val destination = fragmentNavigator.createDestination()
        destination.className = EmptyFragment::class.java.name
        val destination2 = fragmentNavigator.createDestination()
        destination2.className = Fragment::class.java.name

        // Push 3 fragments without executing pending transactions.
        destination.id = INITIAL_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)
        destination2.id = SECOND_FRAGMENT
        fragmentNavigator.navigate(destination2, null, null, null)
        destination.id = THIRD_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)

        // Now pop the Fragment
        val popped = fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertWithMessage("FragmentNavigator should return true when popping the third fragment")
            .that(popped).isTrue()
        // We should ensure the fragment manager is on the proper fragment at the end
        assertWithMessage("FragmentManager back stack should have only SECOND_FRAGMENT")
            .that(fragmentManagerWrapper.fragmentManager.backStackEntryCount)
            .isEqualTo(1)
        assertWithMessage("PrimaryFragment should be the correct type")
            .that(fragmentManagerWrapper.fragmentManager.primaryNavigationFragment)
            .isNotInstanceOf(EmptyFragment::class.java)
    }

    @UiThreadTest
    @Test
    fun testMultiplePopFragmentTransactionsThenPop() {
        val fragmentNavigator = FoldableFragmentNavigator(
            emptyActivity,
            fragmentManagerWrapper,
            R.id.container
        )
        val destination = fragmentNavigator.createDestination()
        destination.className = EmptyFragment::class.java.name

        // Push 4 fragments
        destination.id = INITIAL_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)
        destination.id = SECOND_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)
        destination.id = THIRD_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)
        destination.id = FOURTH_FRAGMENT
        fragmentNavigator.navigate(destination, null, null, null)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()

        // Pop 2 fragments without executing pending transactions.
        fragmentNavigator.popBackStack(true)
        fragmentNavigator.popBackStack(true)

        val popped = fragmentNavigator.popBackStack(true)
        fragmentManagerWrapper.fragmentManager.executePendingTransactions()
        assertTrue("FragmentNavigator should return true when popping the third fragment", popped)
    }

    @Test
    fun testToString() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination().apply {
            id = INITIAL_FRAGMENT
            className = EmptyFragment::class.java.name
            label = TEST_LABEL
        }
        val expected = "Destination(0x${INITIAL_FRAGMENT.toString(16)}) label=test_label " +
            "class=${EmptyFragment::class.java.name}"
        assertThat(destination.toString()).isEqualTo(expected)
    }

    @Test
    fun testToStringNoClassName() {
        val fragmentNavigator =
            FoldableFragmentNavigator(emptyActivity, fragmentManagerWrapper, R.id.container)
        val destination = fragmentNavigator.createDestination().apply {
            id = INITIAL_FRAGMENT
            label = TEST_LABEL
        }
        val expected = "Destination(0x${INITIAL_FRAGMENT.toString(16)}) label=test_label " +
            "class=null"
        assertThat(destination.toString()).isEqualTo(expected)
    }
}

class EmptyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_activity)
    }
}

class NonEmptyConstructorFragment(val test: String) : Fragment()

class NonEmptyFragmentFactory : FragmentFactory() {
    override fun instantiate(
        classLoader: ClassLoader,
        className: String
    ) = if (className == NonEmptyConstructorFragment::class.java.name) {
        NonEmptyConstructorFragment("test")
    } else {
        super.instantiate(classLoader, className)
    }
}
