/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.microsoft.device.dualscreen.navigation.sample

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.microsoft.device.dualscreen.navigation.sample.listscreen.LeaderBoardAdapter
import org.hamcrest.Matcher

open class BaseTest {
    fun checkIfFirstContainerIsDisplayed() {
        onView(withId(R.id.first_container_id)).check(matches(ViewMatchers.isDisplayed()))
    }

    fun checkIfSecondContainerIsDisplayed() {
        onView(withId(R.id.first_container_id)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.second_container_id)).check(matches(ViewMatchers.isDisplayed()))
    }

    fun goToRegisterPage() {
        onView(withId(R.id.btn_register)).perform(click())
    }

    fun checkRegisterPage(matcher: Matcher<View>) {
        onView(withId(R.id.username_text)).check(matches(matcher))
        onView(withId(R.id.email_text)).check(matches(matcher))
    }

    fun goToRegistrationDone() {
        onView(withId(R.id.signup_btn)).perform(click())
    }

    fun checkRegistrationDonePage(matcher: Matcher<View>) {
        onView(withId(R.id.registered_message)).check(matches(matcher))
    }

    fun goToWelcomePage() {
        onView(withId(R.id.btn_welcome)).perform(click())
    }

    fun checkWelcomePage(matcher: Matcher<View>) {
        onView(withId(R.id.btn_welcome))
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.game_title)).check(matches(matcher))
    }

    fun goToAboutPage() {
        onView(withId(R.id.btn_welcome)).perform(click())
    }

    fun checkAboutPage(matcher: Matcher<View>) {
        onView(withId(R.id.about_btn))
            .check(matches(ViewMatchers.isDisplayed())).perform(click())
        onView(withId(R.id.about_tv)).check(matches(matcher))
    }

    fun checkPersonsPage(matcher: Matcher<View>) {
        onView(withId(R.id.btn_persons)).check(matches(matcher)).perform(click())
        onView(withId(R.id.leaderboard_list)).check(matches(matcher))
    }

    fun goToProfilePage() {
        onView(withId(R.id.leaderboard_list))
            .perform(actionOnItemAtPosition<LeaderBoardAdapter.ViewHolder>(0, click()))
    }

    fun checkProfilePage(matcher: Matcher<View>) {
        onView(withId(R.id.profile_pic)).check(matches(matcher))
        onView(withId(R.id.user_data_card)).check(matches(matcher))
        onView(withId(R.id.profile_user_name)).check(matches(matcher))
    }
}