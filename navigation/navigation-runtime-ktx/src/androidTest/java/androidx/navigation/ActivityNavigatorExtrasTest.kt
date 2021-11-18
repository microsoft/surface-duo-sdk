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

import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class ActivityNavigatorExtrasTest {

    @Test
    fun testFragmentNavigatorExtras() {
        val activityOptions = ActivityOptionsCompat.makeBasic()
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val extras = FoldableActivityNavigatorExtras(activityOptions, flags)
        assertWithMessage("ActivityOptions should be passed through")
            .that(extras.activityOptions)
            .isSameInstanceAs(activityOptions)
        assertThat(extras.activityOptions).isSameInstanceAs(activityOptions)
        assertWithMessage("Flags should be passed through")
            .that(extras.flags)
            .isEqualTo(flags)
    }
}
