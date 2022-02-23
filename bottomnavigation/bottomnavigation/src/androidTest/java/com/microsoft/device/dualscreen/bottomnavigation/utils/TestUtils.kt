/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation.utils

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.microsoft.device.dualscreen.bottomnavigation.BottomNavigationView
import com.microsoft.device.dualscreen.testing.DeviceModel
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun changeDisplayPosition(pos: DisplayPosition): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of BottomNavigation buttons"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as BottomNavigationView
            tabLayout.displayPosition = pos

            uiController.loopMainThreadUntilIdle()
        }
    }

fun disableAnimation(): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "disables the animations for the BottomNavigation"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val bottomNavigationView = view as BottomNavigationView
            bottomNavigationView.useAnimation = false

            uiController.loopMainThreadUntilIdle()
        }
    }

fun areTabsOnScreen(pos: DisplayPosition): Matcher<View> =
    object :
        BoundedMatcher<View, BottomNavigationView>(BottomNavigationView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the tabs are displayed on the right screen"
            )
        }

        override fun matchesSafely(item: BottomNavigationView?): Boolean {
            if (item == null || pos != item.displayPosition) {
                return false
            }
            val navMenu = item.getChildAt(0) as BottomNavigationMenuView
            val firstChild = navMenu.getChildAt(0)
            val lastChild = navMenu.getChildAt(navMenu.childCount - 1)
            if (firstChild == null || lastChild == null) {
                return false
            }

            val xStart = firstChild.translationX.toInt() + firstChild.left
            val xEnd = (lastChild.translationX + lastChild.left + lastChild.width).toInt()

            with(DeviceModel.SurfaceDuo) {
                return when (pos) {
                    DisplayPosition.DUAL ->
                        xStart in 0..paneWidth && xEnd in (paneWidth + foldWidth)..totalDisplay
                    DisplayPosition.START ->
                        xStart in 0..paneWidth && xEnd in 0..paneWidth
                    DisplayPosition.END ->
                        xStart in (paneWidth + foldWidth)..totalDisplay && xEnd in (paneWidth + foldWidth)..totalDisplay
                }
            }
        }
    }

fun changeButtonArrangement(startBtnCount: Int, endBtnCount: Int): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change the button's position for the BottomNavigation"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as BottomNavigationView
            tabLayout.arrangeButtons(startBtnCount, endBtnCount)

            uiController.loopMainThreadUntilIdle()
        }
    }

fun hasHalfTransparentBackground(): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "the background color of the view is the same as the expected one"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null) {
                return false
            }

            return item.background is LayerDrawable
        }
    }

fun checkChildCount(expectedChildCount: Int): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "the number of children is the same as the expected one"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null || item !is BottomNavigationView) {
                return false
            }

            val child = item.getChildAt(0) as ViewGroup
            return child.childCount == expectedChildCount
        }
    }