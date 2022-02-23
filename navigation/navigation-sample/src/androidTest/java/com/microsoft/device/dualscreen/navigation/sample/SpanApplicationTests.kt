/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.microsoft.device.dualscreen.navigation.sample

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.testing.spanFromStart
import com.microsoft.device.dualscreen.testing.unspanToStart
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SpanApplicationTests : BaseTest() {
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun launchActivity() {
        rule.launchActivity(null)
    }

    @Test
    fun testSpanDashboard() {
        checkIfFirstContainerIsDisplayed()
        uiDevice.spanFromStart()
        checkIfSecondContainerIsDisplayed()
        uiDevice.unspanToStart()
        checkIfFirstContainerIsDisplayed()
    }

    @Test
    fun testSpanRegister() {
        checkIfFirstContainerIsDisplayed()
        uiDevice.spanFromStart()
        checkIfSecondContainerIsDisplayed()
        goToRegisterPage()
        checkRegisterPage(isDisplayed())
        goToRegistrationDone()
        checkRegistrationDonePage(isDisplayed())
        uiDevice.unspanToStart()
        checkRegisterPage(isDisplayed())
        checkRegistrationDonePage(isDisplayed())
    }

    @Test
    fun testSpanWelcome() {
        checkIfFirstContainerIsDisplayed()
        uiDevice.spanFromStart()
        checkIfSecondContainerIsDisplayed()
        goToWelcomePage()
        checkWelcomePage(isDisplayed())
        goToAboutPage()
        checkAboutPage(isDisplayed())
        uiDevice.unspanToStart()
        checkWelcomePage(isDisplayed())
        checkAboutPage(isDisplayed())
    }

    @Test
    fun testSpanPersons() {
        checkIfFirstContainerIsDisplayed()
        uiDevice.spanFromStart()
        checkIfSecondContainerIsDisplayed()
        checkPersonsPage(isDisplayed())
        goToProfilePage()
        checkProfilePage(isDisplayed())
        uiDevice.unspanToStart()
        checkPersonsPage(isDisplayed())
        checkProfilePage(isDisplayed())
    }
}