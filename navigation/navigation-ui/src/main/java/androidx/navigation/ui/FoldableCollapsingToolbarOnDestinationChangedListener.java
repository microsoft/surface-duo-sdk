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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.FoldableNavController;
import androidx.navigation.FoldableNavDestination;
import androidx.transition.TransitionManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.lang.ref.WeakReference;

/**
 * The OnDestinationChangedListener specifically for keeping a
 * CollapsingToolbarLayout+Toolbar updated.
 * This handles both updating the title and updating the Up Indicator, transitioning between
 * the drawer icon and up arrow as needed.
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class FoldableCollapsingToolbarOnDestinationChangedListener
        extends FoldableAbstractAppBarOnDestinationChangedListener {
    private final WeakReference<CollapsingToolbarLayout> mCollapsingToolbarLayoutWeakReference;
    private final WeakReference<Toolbar> mToolbarWeakReference;

    FoldableCollapsingToolbarOnDestinationChangedListener(
            @NonNull CollapsingToolbarLayout collapsingToolbarLayout,
            @NonNull Toolbar toolbar, @NonNull FoldableAppBarConfiguration configuration) {
        super(collapsingToolbarLayout.getContext(), configuration);
        mCollapsingToolbarLayoutWeakReference = new WeakReference<>(collapsingToolbarLayout);
        mToolbarWeakReference = new WeakReference<>(toolbar);
    }

    @Override
    public void onDestinationChanged(@NonNull FoldableNavController controller,
                                     @NonNull FoldableNavDestination destination, @Nullable Bundle arguments) {
        CollapsingToolbarLayout collapsingToolbarLayout =
                mCollapsingToolbarLayoutWeakReference.get();
        Toolbar toolbar = mToolbarWeakReference.get();
        if (collapsingToolbarLayout == null || toolbar == null) {
            controller.removeOnDestinationChangedListener(this);
            return;
        }
        super.onDestinationChanged(controller, destination, arguments);
    }

    @Override
    protected void setTitle(CharSequence title) {
        CollapsingToolbarLayout collapsingToolbarLayout =
                mCollapsingToolbarLayoutWeakReference.get();
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(title);
        }
    }

    @Override
    protected void setNavigationIcon(Drawable icon,
            @StringRes int contentDescription) {
        Toolbar toolbar = mToolbarWeakReference.get();
        if (toolbar != null) {
            boolean useTransition = icon == null && toolbar.getNavigationIcon() != null;
            toolbar.setNavigationIcon(icon);
            toolbar.setNavigationContentDescription(contentDescription);
            if (useTransition) {
                TransitionManager.beginDelayedTransition(toolbar);
            }
        }
    }
}
