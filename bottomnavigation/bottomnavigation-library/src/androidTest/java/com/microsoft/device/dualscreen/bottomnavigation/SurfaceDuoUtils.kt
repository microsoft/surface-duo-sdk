/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.microsoft.device.dualscreen.DisplayPosition
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

const val SCREEN_WIDTH = 1350
const val HINGE_WIDTH = 84
const val SCREEN_COUNT = 2
const val TOTAL_WIDTH = SCREEN_WIDTH * SCREEN_COUNT + HINGE_WIDTH

fun spanApplication() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.swipe(675, 1780, 1350, 900, 400)
}

/**
 * Simulates orienting the device to the left and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationLeft() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationLeft()
}

/**
 * Simulates orienting the device to the right and also freezes rotation
 * by disabling the sensors.
 */
fun setOrientationRight() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationRight()
}

/**
 * Re-enables the sensors and un-freezes the device rotation allowing its contents
 * to rotate with the device physical rotation. During a test execution, it is best to
 * keep the device frozen in a specific orientation until the test case execution has completed.
 */
fun unfreezeRotation() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.unfreezeRotation()
}

fun resetOrientation() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.setOrientationNatural()
    device.unfreezeRotation()
}

fun changeDisplayPosition(pos: DisplayPosition): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of SurfaceDuoTabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as SurfaceDuoBottomNavigationView
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
            return "Change Display Position value of SurfaceDuoBottomNavigationView"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val view = view as SurfaceDuoBottomNavigationView
            view.useAnimation = false

            uiController.loopMainThreadUntilIdle()
        }
    }

fun areTabsOnScreen(pos: DisplayPosition): Matcher<View> =
    object :
        BoundedMatcher<View, SurfaceDuoBottomNavigationView>(SurfaceDuoBottomNavigationView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the tabs are displayed on the right screen"
            )
        }

        override fun matchesSafely(item: SurfaceDuoBottomNavigationView?): Boolean {
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

            return when (pos) {
                DisplayPosition.DUAL ->
                    xStart in 0..SCREEN_WIDTH && xEnd in (SCREEN_WIDTH + HINGE_WIDTH)..TOTAL_WIDTH
                DisplayPosition.START ->
                    xStart in 0..SCREEN_WIDTH && xEnd in 0..SCREEN_WIDTH
                DisplayPosition.END ->
                    xStart in (SCREEN_WIDTH + HINGE_WIDTH)..TOTAL_WIDTH && xEnd in (SCREEN_WIDTH + HINGE_WIDTH)..TOTAL_WIDTH
            }
        }
    }

fun changeButtonArrangement(startBtnCount: Int, endBtnCount: Int): ViewAction =
    object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String {
            return "Change Display Position value of SurfaceDuoTabLayout"
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val tabLayout = view as SurfaceDuoBottomNavigationView
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
                "the background color of the view is the same as the expected one"
            )
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item == null || item !is SurfaceDuoBottomNavigationView) {
                return false
            }

            val child = item.getChildAt(0) as ViewGroup
            return child.childCount == expectedChildCount
        }
    }