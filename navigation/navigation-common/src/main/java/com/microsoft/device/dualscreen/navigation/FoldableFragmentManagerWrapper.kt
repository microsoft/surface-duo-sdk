/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation

import android.app.Activity
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.FoldableNavOptions
import androidx.navigation.common.R
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
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
        private const val KEY_NAV_OPTIONS = "microsoft-foldable-nav-fragment:fragment-manager:nav-options"
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

    /**
     * Navigation options for the fragments added to stack.
     */
    private var navOptionsArgs = hashMapOf<String, FoldableNavOptions?>()

    /**
     * If it's [true], then end container must be empty
     */
    private var endContainerIsEmpty = false

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
        outState.putSerializable(KEY_NAV_OPTIONS, navOptionsArgs)
    }

    /**
     * This method must be called inside [Activity.onRestoreInstanceState].
     *
     * @param savedInstanceState the data most recently supplied in [onSaveInstanceState].
     */
    @Suppress("UNCHECKED_CAST")
    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            navOptionsArgs = it.getSerializable(KEY_NAV_OPTIONS) as? HashMap<String, FoldableNavOptions?> ?: hashMapOf()
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
            val fragment = fragmentsForEndContainer.pop()
            fragmentManager.inTransaction {
                val navOptions = navOptionsArgs[fragment.TAG]
                requestChangeConfiguration(navOptions)
                applyAnimations(navOptions)

                add(getContainerId(screenMode, navOptions), fragment)
                addToBackStack(fragment.TAG)
            }
        }

        endContainerIsEmpty = false
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
            val fragment = fragmentsForEndContainer.pop()
            fragmentManager.inTransaction {
                val navOptions = navOptionsArgs[fragment.TAG]
                requestChangeConfiguration(navOptions)
                applyAnimations(navOptions)

                add(R.id.first_container_id, fragment)
                addToBackStack(fragment.TAG)
            }
        }

        endContainerIsEmpty = true
    }

    /**
     * @return the stack containing all fragments that belongs to the end container in reverse order that was added to the stack.
     */
    private fun extractFragmentsForEndContainer(): Stack<Fragment> {
        val initialTopFragment = fragmentManager.topFragment
        val isTopFragmentForEndContainer = {
            fragmentManager.topFragment?.let {
                val navOptions = navOptionsArgs[it.TAG]
                navOptions?.launchScreen == LaunchScreen.END ||
                    navOptions?.launchScreen == LaunchScreen.BOTH ||
                    (navOptions?.launchScreen == LaunchScreen.DEFAULT && it == initialTopFragment)
            } ?: false
        }

        val fragments = Stack<Fragment>()
        while (isTopFragmentForEndContainer()) {
            fragmentManager.topFragment?.let {
                fragmentManager.inTransaction { remove(it) }
                fragments.push(it)
                fragmentManager.popBackStackImmediate()
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

        val containerId = getContainerId(screenMode, navOptions)

        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (endContainerIsEmpty.not() && screenMode == ScreenMode.DUAL_SCREEN) {
            moveTopFragmentToStartContainer()
        }

        navOptionsArgs[fragment.TAG] = navOptions
        fragmentTransaction.applyAnimations(navOptions)
        fragmentTransaction.add(containerId, fragment)
        fragmentTransaction.addToBackStack(fragment.TAG)
        endContainerIsEmpty = if (endContainerIsEmpty) {
            containerId == R.id.first_container_id
        } else {
            false
        }

        return fragmentTransaction
    }

    /**
     * Removes the top fragment from back stack.
     * If the screen mode is [ScreenMode.DUAL_SCREEN] and the stack is not empty,
     * then the top fragment from start container will be moved to the end container
     */
    fun popBackStack(withTransition: Boolean, name: String?, flags: Int) {
        when (screenMode) {
            ScreenMode.SINGLE_SCREEN -> fragmentManager.popBackStack()
            ScreenMode.DUAL_SCREEN -> {
                if (fragmentManager.isPopOnDualScreenPossible()) {
                    removeTopFragment(withTransition)
                }

                fragmentManager.topFragment?.let {
                    val navOptions = navOptionsArgs[it.TAG]
                    requestChangeConfiguration(navOptions)
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
        fragmentManager.topFragment?.let {
            if (!canMoveFragmentToStartContainer(it)) {
                return@let
            }

            fragmentManager.inTransaction { remove(it) }
            fragmentManager.popBackStackImmediate()
            fragmentManager.inTransaction {
                val navOptions = navOptionsArgs[it.TAG]
                applyAnimations(navOptions)
                add(R.id.first_container_id, it)
                addToBackStack(it.TAG)
            }
            endContainerIsEmpty = false
        }
    }

    /**
     * Returns [true] if the fragment is on the end container and [FoldableNavOptions.getLaunchScreen] option is not set to [LaunchScreen.END],
     * [false] otherwise
     * @return [true] if the fragment can be moved to the start container, [false] otherwise.
     */
    private fun canMoveFragmentToStartContainer(fragment: Fragment): Boolean {
        val navOptions = navOptionsArgs[fragment.TAG]
        return navOptions?.launchScreen == LaunchScreen.BOTH ||
            (navOptions?.launchScreen != LaunchScreen.END && fragment.isOnEndContainer())
    }

    /**
     * Removes the top fragment from end container and, if needed, the top fragment from start container will be moved to end container.
     * @param withTransition If it's [true], then the fragment from start container will be moved to the end container.
     */
    private fun removeTopFragment(withTransition: Boolean) {
        if (fragmentManager.isPopOnDualScreenPossible()) {
            fragmentManager.popBackStackImmediate()
            endContainerIsEmpty = true
            if (withTransition && isTransitionToDualScreenPossible()) {
                moveTopFragmentToEndContainer()
            }
        }
    }

    /**
     * Removes the top fragment from start container and moves it to the end container
     */
    private fun moveTopFragmentToEndContainer() {
        fragmentManager.topFragment?.let {
            if (!canMoveFragmentToEndContainer(it)) {
                return@let
            }

            fragmentManager.inTransaction { remove(it) }
            fragmentManager.popBackStackImmediate()
            fragmentManager.inTransaction {
                val navOptions = navOptionsArgs[it.TAG]
                requestChangeConfiguration(navOptions)
                applyAnimations(navOptions)
                add(R.id.second_container_id, it)
                addToBackStack(it.TAG)
            }

            endContainerIsEmpty = false
        }
    }

    /**
     * Returns [true] if the fragment is on the start container and [FoldableNavOptions.getLaunchScreen] option is not set to [LaunchScreen.START],
     * [false] otherwise
     * @return [true] if the fragment can be moved to the end container, [false] otherwise.
     */
    private fun canMoveFragmentToEndContainer(fragment: Fragment): Boolean {
        val navOptions = navOptionsArgs[fragment.TAG]
        return navOptions?.launchScreen != LaunchScreen.START &&
            navOptions?.launchScreen != LaunchScreen.BOTH &&
            fragment.isOnStartContainer()
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
        val fragmentsCount = fragmentManager.fragments.size
        return when {
            fragmentsCount == 1 -> fragmentManager.topFragment?.let { topFragment ->
                val navOptions = navOptionsArgs[topFragment.TAG]
                navOptions?.launchScreen == LaunchScreen.BOTH ||
                    navOptions?.launchScreen == LaunchScreen.END
            } ?: false

            fragmentsCount > 1 -> true

            else -> false
        }
    }

    /**
     * @return [true] if the transition to single screen is possible, [false] otherwise
     */
    @VisibleForTesting
    fun isTransitionToSingleScreenPossible(): Boolean {
        val fragmentsCount = fragmentManager.fragments.size
        return when {
            fragmentsCount == 1 -> fragmentManager.topFragment?.let { topFragment ->
                val navOptions = navOptionsArgs[topFragment.TAG]
                navOptions?.launchScreen == LaunchScreen.BOTH ||
                    navOptions?.launchScreen == LaunchScreen.START
            } ?: false

            fragmentsCount > 1 -> true

            else -> false
        }
    }
}