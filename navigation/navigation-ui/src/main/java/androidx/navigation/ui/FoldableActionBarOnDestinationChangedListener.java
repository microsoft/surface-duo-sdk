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

package androidx.navigation.ui;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The OnDestinationChangedListener specifically for keeping the ActionBar updated.
 * This handles both updating the title and updating the Up Indicator, transitioning between
 * the drawer icon and up arrow as needed.
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class FoldableActionBarOnDestinationChangedListener extends
        FoldableAbstractAppBarOnDestinationChangedListener {
    private final AppCompatActivity mActivity;

    FoldableActionBarOnDestinationChangedListener(@NonNull AppCompatActivity activity,
                                                  @NonNull FoldableAppBarConfiguration configuration) {
        super(activity.getDrawerToggleDelegate().getActionBarThemedContext(), configuration);
        mActivity = activity;
    }

    @Override
    protected void setTitle(CharSequence title) {
        ActionBar actionBar = mActivity.getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    protected void setNavigationIcon(Drawable icon,
            @StringRes int contentDescription) {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (icon == null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
            ActionBarDrawerToggle.Delegate delegate = mActivity.getDrawerToggleDelegate();
            delegate.setActionBarUpIndicator(icon, contentDescription);
        }
    }
}
