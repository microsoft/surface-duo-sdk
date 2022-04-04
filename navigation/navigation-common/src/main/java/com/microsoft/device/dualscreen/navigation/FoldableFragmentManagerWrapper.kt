/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.FoldableNavOptions
import androidx.navigation.common.R
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import kotlinx.parcelize.Parcelize
import java.util.Stack

/**
 * Wrapper over the [FragmentManager] class.
 * This class manages where a new fragment will be opened, on the start or end container depending on the [ScreenMode]
 */
class FoldableFragmentManagerWrapper(
    val fragmentManager: FragmentManager,
    private val requestConfigListener: RequestConfigListener? = null
) {
    companion object {
        private const val KEY_BACKSTACK = "microsoft-foldable-nav-fragment:fragment-manager:backstack"
        private const val KEY_SCREEN_MODE = "microsoft-foldable-nav-fragment:fragment-manager:screen-mode"
    }

    /**
     * Current screen mode.
     */
    var screenMode: ScreenMode
        get() = _screenMode ?: ScreenMode.SINGLE_SCREEN
        set(value) {
            if (value != _screenMode) {
                _screenMode = value
                updateInternalState()
            }
        }
    private var _screenMode: ScreenMode? = null
    private var backStack = Stack<BackStackEntry>()

    /**
     * If it's [true], then end container must be empty
     */
    private val endContainerIsEmpty: Boolean
        get() = backStack.firstOrNull { it.containerId == R.id.second_container_id } == null

    /**
     * Last added fragment from fragment manager.
     */
    @VisibleForTesting
    val topFragment: Fragment?
        get() {
            if (topFragmentTag == null) {
                return null
            }

            return fragmentManager.findFragmentByTag(topFragmentTag)
        }

    private val topFragmentTag: String?
        get() {
            if (backStack.isEmpty()) {
                return null
            }

            return backStack.peek().fragmentTag
        }

    private val topBackStackEntry: BackStackEntry?
        get() {
            if (backStack.isEmpty()) {
                return null
            }

            return backStack.peek()
        }

    private fun requireTopBackStackEntry(): BackStackEntry =
        topBackStackEntry ?: throw RuntimeException("Visible fragment must have a tag!")

    private fun updateInternalState() {
        when (screenMode) {
            ScreenMode.DUAL_SCREEN -> {
                if (isTransitionToDualScreenPossible()) {
                    transitionToDualScreen()
                }
            }
            ScreenMode.SINGLE_SCREEN -> {
                if (isTransitionToSingleScreenPossible()) {
                    transitionToSingleScreen()
                }
            }
        }
    }

    /**
     * Save all appropriate state.
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(KEY_BACKSTACK, backStack)
        outState.putSerializable(KEY_SCREEN_MODE, _screenMode)
    }

    /**
     * This method must be called inside [Activity.onRestoreInstanceState].
     *
     * @param savedInstanceState the data most recently supplied in [onSaveInstanceState].
     */
    @Suppress("UNCHECKED_CAST")
    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            backStack = it.getSerializable(KEY_BACKSTACK) as? Stack<BackStackEntry> ?: Stack()
            _screenMode = it.getSerializable(KEY_SCREEN_MODE) as? ScreenMode ?: ScreenMode.SINGLE_SCREEN
        }
    }

    /**
     * Called when configuration changes from [ScreenMode.SINGLE_SCREEN] to [ScreenMode.DUAL_SCREEN]
     * Remove all fragments that belongs  to the second container from the start container and add them to the second container.
     */
    private fun transitionToDualScreen() {
        val fragmentsForEndContainer = extractFragmentsForEndContainer()
        if (fragmentsForEndContainer.isEmpty()) {
            return
        }

        while (fragmentsForEndContainer.isNotEmpty()) {
            val fragmentEntry = fragmentsForEndContainer.pop()
            val navOptions = fragmentEntry.navOptions
            requestChangeConfiguration(navOptions)
            fragmentManager.inTransaction {
                applyAnimations(navOptions)

                val containerId = getContainerId(screenMode, navOptions)
                replaceFragment(fragmentEntry.fragment, fragmentEntry.backStackName, containerId, navOptions)
            }
        }
    }

    /**
     * Called when configuration changes from [ScreenMode.DUAL_SCREEN] to [ScreenMode.SINGLE_SCREEN]
     * Remove all fragments that belongs to the second container and add them to the second container.
     */
    private fun transitionToSingleScreen() {
        val fragmentsForEndContainer = extractFragmentsForEndContainer()
        if (fragmentsForEndContainer.isEmpty()) {
            return
        }

        while (fragmentsForEndContainer.isNotEmpty()) {
            val fragmentEntry = fragmentsForEndContainer.pop()
            val navOptions = fragmentEntry.navOptions
            requestChangeConfiguration(navOptions)
            fragmentManager.inTransaction {
                applyAnimations(navOptions)
                replaceFragment(fragmentEntry.fragment, fragmentEntry.backStackName, R.id.first_container_id, navOptions)
            }
        }
    }

    /**
     * @return the stack containing all fragments that belongs to the end container in reverse order that was added to the stack.
     */
    private fun extractFragmentsForEndContainer(): Stack<FragmentEntry> {
        val initialTopFragment = topFragment
        val isTopFragmentForEndContainer = {
            topBackStackEntry?.let {
                val navOptions = it.navOptions
                navOptions?.launchScreen == LaunchScreen.END ||
                    navOptions?.launchScreen == LaunchScreen.BOTH ||
                    (navOptions?.launchScreen == LaunchScreen.DEFAULT && topFragment == initialTopFragment)
            } ?: false
        }

        val fragments = Stack<FragmentEntry>()
        while (isTopFragmentForEndContainer()) {
            topFragment?.let { fragment ->
                val backStackName = topBackStackEntry?.backStackName
                val navOptions = topBackStackEntry?.navOptions
                popBackStackImmediate()
                fragments.push(FragmentEntry(fragment, generateUniqueFragmentTag(fragment), backStackName, navOptions))
            }
        }

        return fragments
    }

    /**
     * Begins a [FragmentTransaction] in order to add a new fragment to the back stack.
     * Depending on [screenMode], the fragment will be added to the start or end container.
     *
     * @param fragment the Fragment that will be added to the stack.
     * @param navOptions the navigation options from navigation graph.
     *
     * @return the new fragment transaction.
     */
    fun beginTransaction(fragment: Fragment, navOptions: FoldableNavOptions?): FragmentTransaction {
        requestChangeConfiguration(navOptions)

        if (endContainerIsEmpty.not() && screenMode == ScreenMode.DUAL_SCREEN) {
            moveTopFragmentToStartContainer()
        }

        val fragmentTag = generateUniqueFragmentTag(fragment)
        val containerId = getContainerId(screenMode, navOptions)

        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.applyAnimations(navOptions)
        fragmentTransaction.replace(containerId, fragment, fragmentTag)
        return fragmentTransaction
    }

    fun addToBackStack(
        transaction: FragmentTransaction,
        fragment: Fragment,
        backStackName: String?,
        navOptions: FoldableNavOptions?
    ) {
        transaction.addToBackStack(backStackName)
        val containerId = getContainerId(screenMode, navOptions)
        val fragmentTag = generateUniqueFragmentTag(fragment)
        backStack.add(BackStackEntry(fragmentTag, backStackName, containerId, navOptions))
    }

    fun addToBackStack(fragment: Fragment, navOptions: FoldableNavOptions?) {
        val containerId = getContainerId(screenMode, navOptions)
        val fragmentTag = generateUniqueFragmentTag(fragment)
        backStack.add(BackStackEntry(fragmentTag, null, containerId, navOptions))
    }

    /**
     * Removes the top fragment from back stack.
     * If the screen mode is [ScreenMode.DUAL_SCREEN] and the stack is not empty,
     * then the top fragment from start container will be moved to the end container
     */
    fun popBackStack(withTransition: Boolean, name: String?, flags: Int) {
        when (screenMode) {
            ScreenMode.SINGLE_SCREEN -> popBackStack(name, flags)
            ScreenMode.DUAL_SCREEN -> {
                if (isPopOnDualScreenPossible()) {
                    removeTopFragment(withTransition)
                } else {
                    popBackStack(name, flags)
                }

                topBackStackEntry?.let {
                    requestChangeConfiguration(it.navOptions)
                }
            }
        }
    }

    /**
     * @param screenMode the screen mode.
     * @param navOptions the navigation options params.
     * @return The container id depending on screen mode and launch screen value. If the [FoldableNavOptions.getLaunchScreen] is [LaunchScreen.DEFAULT],
     * then the container id will depend on [screenMode] value, otherwise the container id will depend on [FoldableNavOptions.getLaunchScreen]
     */
    private fun getContainerId(screenMode: ScreenMode, navOptions: FoldableNavOptions?): Int {
        return if (screenMode == ScreenMode.SINGLE_SCREEN || navOptions?.launchScreen == LaunchScreen.DEFAULT) {
            getContainerId(screenMode)
        } else {
            getContainerId(navOptions?.launchScreen)
        }
    }

    /**
     * @param screenMode the screen mode.
     * @return The container id depending on screen mode.
     */
    private fun getContainerId(screenMode: ScreenMode): Int {
        return when (screenMode) {
            ScreenMode.DUAL_SCREEN -> R.id.second_container_id
            else -> R.id.first_container_id
        }
    }

    /**
     * @param launchScreen the launch screen option from [FoldableNavOptions.getLaunchScreen].
     * @return The container id depending on launch screen value.
     */
    private fun getContainerId(launchScreen: LaunchScreen?): Int {
        return when (launchScreen) {
            LaunchScreen.END -> R.id.second_container_id
            else -> R.id.first_container_id
        }
    }

    /**
     * Moves top fragment from end container to start container.
     */
    private fun moveTopFragmentToStartContainer() {
        topFragment?.let { fragment ->
            val backStackEntry = requireTopBackStackEntry()
            if (!canMoveFragmentToStartContainer(backStackEntry)) {
                return@let
            }

            val backStackName = backStackEntry.backStackName
            val navOptions = backStackEntry.navOptions
            popBackStackImmediate()
            fragmentManager.inTransaction {
                applyAnimations(navOptions)
                replaceFragment(fragment, backStackName, R.id.first_container_id, navOptions)
            }
        }
    }

    /**
     * Returns [true] if the fragment is on the end container and [FoldableNavOptions.getLaunchScreen] option is not set to [LaunchScreen.END],
     * [false] otherwise
     * @return [true] if the fragment can be moved to the start container, [false] otherwise.
     */
    private fun canMoveFragmentToStartContainer(backStackEntry: BackStackEntry): Boolean {
        val navOptions = backStackEntry.navOptions
        val fragmentTag = backStackEntry.fragmentTag
        return navOptions?.launchScreen == LaunchScreen.BOTH ||
            (navOptions?.launchScreen != LaunchScreen.END && isOnEndContainer(fragmentTag))
    }

    /**
     * Removes the top fragment from end container and, if needed, the top fragment from start container will be moved to end container.
     * @param withTransition If it's [true], then the fragment from start container will be moved to the end container.
     */
    private fun removeTopFragment(withTransition: Boolean) {
        if (isPopOnDualScreenPossible()) {
            popBackStackImmediate()
            if (withTransition && isTransitionToDualScreenPossible()) {
                moveTopFragmentToEndContainer()
            }
        }
    }

    /**
     * Removes the top fragment from start container and moves it to the end container
     */
    private fun moveTopFragmentToEndContainer() {
        topFragment?.let {
            val backStackEntry = requireTopBackStackEntry()
            if (!canMoveFragmentToEndContainer(backStackEntry)) {
                return@let
            }

            val backStackName = backStackEntry.backStackName
            val navOptions = backStackEntry.navOptions
            popBackStackImmediate()
            fragmentManager.inTransaction {
                requestChangeConfiguration(navOptions)
                applyAnimations(navOptions)
                replaceFragment(it, backStackName, R.id.second_container_id, navOptions)
            }
        }
    }

    /**
     * Returns [true] if the fragment is on the start container and [FoldableNavOptions.getLaunchScreen] option is not set to [LaunchScreen.START],
     * [false] otherwise
     * @return [true] if the fragment can be moved to the end container, [false] otherwise.
     */
    private fun canMoveFragmentToEndContainer(backStackEntry: BackStackEntry): Boolean {
        val navOptions = backStackEntry.navOptions
        val fragmentTag = backStackEntry.fragmentTag
        return navOptions?.launchScreen != LaunchScreen.START &&
            navOptions?.launchScreen != LaunchScreen.BOTH &&
            isOnStartContainer(fragmentTag)
    }

    /**
     * Request a new configuration for [FoldableLauout]
     *
     * @param navOptions used to determine the new configuration
     */
    private fun requestChangeConfiguration(navOptions: FoldableNavOptions?) {
        val launchScreen = navOptions?.launchScreen ?: LaunchScreen.DEFAULT
        requestConfigListener?.changeConfiguration(RequestConfigParams(launchScreen, screenMode))
    }

    /**
     * @return [true] if the transition to dual screen is possible, [false] otherwise
     */
    @VisibleForTesting
    fun isTransitionToDualScreenPossible(): Boolean {
        val fragmentsCount = backStack.size
        val value = when {
            fragmentsCount == 1 -> topBackStackEntry?.let {
                val navOptions = it.navOptions
                navOptions?.launchScreen == LaunchScreen.BOTH ||
                    navOptions?.launchScreen == LaunchScreen.END
            } ?: false

            fragmentsCount > 1 -> true

            else -> false
        }
        return value
    }

    /**
     * @return [true] if the transition to single screen is possible, [false] otherwise
     */
    @VisibleForTesting
    fun isTransitionToSingleScreenPossible(): Boolean {
        val fragmentsCount = backStack.size
        return when {
            fragmentsCount == 1 -> topBackStackEntry?.let {
                val navOptions = it.navOptions
                navOptions?.launchScreen == LaunchScreen.BOTH ||
                    navOptions?.launchScreen == LaunchScreen.START
            } ?: false

            fragmentsCount > 1 -> true

            else -> false
        }
    }

    private fun FragmentTransaction.replaceFragment(
        fragment: Fragment,
        backStackName: String?,
        containerViewId: Int,
        navOptions: FoldableNavOptions?
    ) {
        val fragmentTag = generateUniqueFragmentTag(fragment)
        replace(containerViewId, fragment, fragmentTag)
        addToBackStack(backStackName)
        setReorderingAllowed(true)
        backStack.add(BackStackEntry(fragmentTag, backStackName, containerViewId, navOptions))
    }

    private fun generateUniqueFragmentTag(fragment: Fragment): String {
        return "${fragment.TAG}-${backStack.size}"
    }

    /**
     * @return [true] if the pop operation is possible on dual screen mode, [false] otherwise
     */
    @VisibleForTesting
    fun isPopOnDualScreenPossible(): Boolean = backStack.size >= 2

    @VisibleForTesting
    fun isOnStartContainer(fragmentTag: String): Boolean =
        backStack.firstOrNull { it.fragmentTag == fragmentTag }?.containerId == R.id.first_container_id

    @VisibleForTesting
    fun isOnEndContainer(fragmentTag: String): Boolean {
        return backStack.firstOrNull { it.fragmentTag == fragmentTag }?.containerId == R.id.second_container_id
    }

    private fun popBackStack(name: String?, flags: Int) {
        backStack.pop()
        fragmentManager.popBackStack(name, flags)
    }

    private fun popBackStackImmediate() {
        topFragment?.let { fragmentManager.inTransaction { remove(it) } }
        backStack.pop()
        fragmentManager.popBackStackImmediate()
    }

    @Parcelize
    private class BackStackEntry(
        val fragmentTag: String,
        val backStackName: String?,
        val containerId: Int,
        val navOptions: FoldableNavOptions?
    ) : Parcelable

    private class FragmentEntry(
        val fragment: Fragment,
        val fragmentTag: String,
        val backStackName: String?,
        val navOptions: FoldableNavOptions?
    )
}