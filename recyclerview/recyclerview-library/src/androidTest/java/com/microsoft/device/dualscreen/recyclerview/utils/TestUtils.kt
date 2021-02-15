package com.microsoft.device.dualscreen.recyclerview.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun areItemsDisplayed(): Matcher<View> =
    object :
        BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText(
                "Checks whether the tabs are displayed on the right screen"
            )
        }

        override fun matchesSafely(item: RecyclerView?): Boolean {
            if (item == null) {
                return false
            }
            return item.adapter != null && item.adapter?.itemCount != 0
        }
    }
