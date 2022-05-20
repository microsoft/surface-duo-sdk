/*
 * Copyright 2021 The Android Open Source Project
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
 */

package com.microsoft.device.dualscreen.testing.rules

import android.annotation.SuppressLint
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowInfoTrackerDecorator
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.Flow

@SuppressLint("RestrictedApi")
internal class PublishWindowInfoTrackerDecorator(
    private val flow: Flow<WindowLayoutInfo>
) : WindowInfoTrackerDecorator {
    override fun decorate(tracker: WindowInfoTracker): WindowInfoTracker {
        return PublishLayoutInfoTracker(tracker, flow)
    }
}
