/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.microsoft.device.dualscreen.navigation.sample

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.microsoft.device.dualscreen.testing.SurfaceDuo1
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SpanApplicationTests : BaseTest() {
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @Before
    fun launchActivity() {
        rule.launchActivity(null)
    }

    @Test
    fun testSpanDashboard() {
        checkIfFirstContainerIsDisplayed()
        SurfaceDuo1.switchFromSingleToDualScreen()
        checkIfSecondContainerIsDisplayed()
        SurfaceDuo1.switchFromDualToSingleScreen()
        checkIfFirstContainerIsDisplayed()
    }

    @Test
    fun testSpanRegister() {
        checkIfFirstContainerIsDisplayed()
        SurfaceDuo1.switchFromSingleToDualScreen()
        checkIfSecondContainerIsDisplayed()
        goToRegisterPage()
        checkRegisterPage(isDisplayed())
        goToRegistrationDone()
        checkRegistrationDonePage(isDisplayed())
        SurfaceDuo1.switchFromDualToSingleScreen()
        checkRegisterPage(isDisplayed())
        checkRegistrationDonePage(isDisplayed())
    }

    @Test
    fun testSpanWelcome() {
        checkIfFirstContainerIsDisplayed()
        SurfaceDuo1.switchFromSingleToDualScreen()
        checkIfSecondContainerIsDisplayed()
        goToWelcomePage()
        checkWelcomePage(isDisplayed())
        goToAboutPage()
        checkAboutPage(isDisplayed())
        SurfaceDuo1.switchFromDualToSingleScreen()
        checkWelcomePage(isDisplayed())
        checkAboutPage(isDisplayed())
    }

    @Test
    fun testSpanPersons() {
        checkIfFirstContainerIsDisplayed()
        SurfaceDuo1.switchFromSingleToDualScreen()
        checkIfSecondContainerIsDisplayed()
        checkPersonsPage(isDisplayed())
        goToProfilePage()
        checkProfilePage(isDisplayed())
        SurfaceDuo1.switchFromDualToSingleScreen()
        checkPersonsPage(isDisplayed())
        checkProfilePage(isDisplayed())
    }
}