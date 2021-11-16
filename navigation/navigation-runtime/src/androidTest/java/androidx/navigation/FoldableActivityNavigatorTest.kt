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

package androidx.navigation

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.argThat
import org.mockito.ArgumentMatchers.refEq
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

@LargeTest
@RunWith(AndroidJUnit4::class)
class FoldableActivityNavigatorTest {
    companion object {
        const val TARGET_ID = 1
        const val TARGET_ACTION = "test_action"
        val TARGET_DATA: Uri = Uri.parse("http://www.example.com")
        const val TARGET_ARGUMENT_NAME = "test"
        const val TARGET_DATA_PATTERN = "http://www.example.com/{$TARGET_ARGUMENT_NAME}"
        const val TARGET_ARGUMENT_VALUE = "data_pattern"
        const val TARGET_ARGUMENT_INT_VALUE = 1
        const val TARGET_LABEL = "test_label"
    }

    @Suppress("DEPRECATION")
    @get:Rule
    val activityRule = androidx.test.rule.ActivityTestRule(ActivityNavigatorActivity::class.java)

    private lateinit var activityNavigator: FoldableActivityNavigator

    @Before
    fun setup() {
        activityNavigator = FoldableActivityNavigator(activityRule.activity)
        TargetActivity.instances = spy(ArrayList())
    }

    @After
    fun cleanup() {
        TargetActivity.instances.forEach { activity ->
            activity.finish()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun throwOnPutAction() {
        val targetDestination = activityNavigator.createDestination()
        targetDestination.putAction(TARGET_ID, 0)
    }

    @Test
    fun navigate() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(targetDestination, null, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should not include FLAG_ACTIVITY_NEW_TASK",
            0,
            intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }

    @Test
    fun navigateFromNonActivityContext() {
        // Create using the applicationContext
        val activityNavigator = FoldableActivityNavigator(activityRule.activity.applicationContext)

        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(targetDestination, null, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should include FLAG_ACTIVITY_NEW_TASK",
            Intent.FLAG_ACTIVITY_NEW_TASK,
            intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }

    @Test
    fun navigateSingleTop() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(
            targetDestination,
            null,
            foldableNavOptions {
                launchSingleTop = true
            },
            null
        )

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should include FLAG_ACTIVITY_SINGLE_TOP",
            Intent.FLAG_ACTIVITY_SINGLE_TOP,
            intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    @Test
    fun navigateWithArgs() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }

        val args = Bundle().apply {
            putString(TARGET_ARGUMENT_NAME, TARGET_ARGUMENT_VALUE)
        }
        activityNavigator.navigate(targetDestination, args, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should have its arguments in its extras",
            TARGET_ARGUMENT_VALUE,
            intent.getStringExtra(TARGET_ARGUMENT_NAME)
        )
    }

    @Test
    fun navigateAction() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            action = TARGET_ACTION
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(targetDestination, null, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should have action set",
            TARGET_ACTION,
            intent.action
        )
    }

    @Test
    fun navigateData() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            data = TARGET_DATA
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(targetDestination, null, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should have data set",
            TARGET_DATA,
            intent.data
        )
    }

    @Test
    fun navigateDataPattern() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            dataPattern = TARGET_DATA_PATTERN
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        val args = Bundle().apply {
            putString(TARGET_ARGUMENT_NAME, TARGET_ARGUMENT_VALUE)
        }
        activityNavigator.navigate(targetDestination, args, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should have data set with argument filled in",
            TARGET_DATA_PATTERN.replace("{$TARGET_ARGUMENT_NAME}", TARGET_ARGUMENT_VALUE),
            intent.data?.toString()
        )
        assertEquals(
            "Intent should have its arguments in its extras",
            TARGET_ARGUMENT_VALUE,
            intent.getStringExtra(TARGET_ARGUMENT_NAME)
        )
    }

    @Test
    fun navigateDataPatternInt() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            dataPattern = TARGET_DATA_PATTERN
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        val args = Bundle().apply {
            putInt(TARGET_ARGUMENT_NAME, TARGET_ARGUMENT_INT_VALUE)
        }
        activityNavigator.navigate(targetDestination, args, null, null)

        val targetActivity = waitForActivity()
        val intent = targetActivity.intent
        assertNotNull(intent)
        assertEquals(
            "Intent should have data set with argument filled in",
            TARGET_DATA_PATTERN.replace(
                "{$TARGET_ARGUMENT_NAME}",
                TARGET_ARGUMENT_INT_VALUE.toString()
            ),
            intent.data?.toString()
        )
        assertEquals(
            "Intent should have its arguments in its extras",
            TARGET_ARGUMENT_INT_VALUE,
            intent.getIntExtra(TARGET_ARGUMENT_NAME, -1)
        )
    }

    @Test
    fun navigateDataPatternMissingArgument() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            dataPattern = TARGET_DATA_PATTERN
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        try {
            val args = Bundle()
            activityNavigator.navigate(targetDestination, args, null, null)
            fail("navigate() should fail if required arguments are not included")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
    @Test
    @UiThreadTest
    fun navigateWithExtras() {
        val context = mock(Context::class.java)
        val view = mock(View::class.java)
        val activityNavigator = FoldableActivityNavigator(context)
        val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activityRule.activity,
            view,
            "test"
        )
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val extras = FoldableActivityNavigator.Extras.Builder()
            .setActivityOptions(activityOptions)
            .addFlags(flags)
            .build()

        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        activityNavigator.navigate(targetDestination, null, null, extras)
        // Just verify that the ActivityOptions got passed through, there's
        // CTS tests to ensure that the ActivityOptions do the right thing
        verify(context).startActivity(
            argThat { intent ->
                intent.flags and flags != 0
            },
            refEq(activityOptions.toBundle())
        )
    }

    @Test
    fun testToString() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            label = TARGET_LABEL
            setComponentName(ComponentName(activityRule.activity, TargetActivity::class.java))
        }
        val expected = "Destination(0x${TARGET_ID.toString(16)}) label=$TARGET_LABEL " +
            "class=${TargetActivity::class.java.name}"
        assertThat(targetDestination.toString()).isEqualTo(expected)
    }

    @Test
    fun testToStringNoClass() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            label = TARGET_LABEL
            action = TARGET_ACTION
        }
        val expected = "Destination(0x${TARGET_ID.toString(16)}) label=$TARGET_LABEL " +
            "action=$TARGET_ACTION"
        assertThat(targetDestination.toString()).isEqualTo(expected)
    }

    @Test
    fun testToStringNoClassOrAction() {
        val targetDestination = activityNavigator.createDestination().apply {
            id = TARGET_ID
            label = TARGET_LABEL
        }
        val expected = "Destination(0x${TARGET_ID.toString(16)}) label=$TARGET_LABEL"
        assertThat(targetDestination.toString()).isEqualTo(expected)
    }

    private fun waitForActivity(): TargetActivity {
        verify(TargetActivity.instances, timeout(3000)).add(any())
        verifyNoMoreInteractions(TargetActivity.instances)
        val targetActivity: ArrayList<TargetActivity> = ArrayList()
        activityRule.runOnUiThread {
            targetActivity.addAll(TargetActivity.instances)
        }
        assertTrue("Only expected a single TargetActivity", targetActivity.size == 1)
        return targetActivity[0]
    }
}

class ActivityNavigatorActivity : Activity()
class TargetActivity : Activity() {
    companion object {
        var instances: ArrayList<TargetActivity> = spy(ArrayList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instances.add(this)
    }
}
