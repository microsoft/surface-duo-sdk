/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;

/**
 * Entry point for navigation operations.
 *
 * <p>This class provides utilities for finding a relevant {@link FoldableNavController} instance from
 * various common places in your application, or for performing navigation in response to
 * UI events.</p>
 */
public final class FoldableNavigation {
    // No instances. Static utilities only.
    private FoldableNavigation() {
    }

    /**
     * Find a {@link FoldableNavController} given the id of a View and its containing
     * {@link Activity}. This is a convenience wrapper around {@link #findNavController(View)}.
     *
     * <p>This method will locate the {@link FoldableNavController} associated with this view.
     * This is automatically populated for the id of a {@link FoldableNavHost} and its children.</p>
     *
     * @param activity The Activity hosting the view
     * @param viewId   The id of the view to search from
     * @return the {@link FoldableNavController} associated with the view referenced by id
     * @throws IllegalStateException if the given viewId does not correspond with a
     *                               {@link FoldableNavHost} or is not within a NavHost.
     */
    @NonNull
    public static FoldableNavController findNavController(@NonNull Activity activity, @IdRes int viewId) {
        View view = ActivityCompat.requireViewById(activity, viewId);
        FoldableNavController navController = findViewNavController(view);
        if (navController == null) {
            throw new IllegalStateException("Activity " + activity
                    + " does not have a FoldableNavController set on " + viewId);
        }
        return navController;
    }

    /**
     * Find a {@link FoldableNavController} given a local {@link View}.
     *
     * <p>This method will locate the {@link FoldableNavController} associated with this view.
     * This is automatically populated for views that are managed by a {@link FoldableNavHost}
     * and is intended for use by various {@link View.OnClickListener listener}
     * interfaces.</p>
     *
     * @param view the view to search from
     * @return the locally scoped {@link FoldableNavController} to the given view
     * @throws IllegalStateException if the given view does not correspond with a
     *                               {@link FoldableNavHost} or is not within a NavHost.
     */
    @NonNull
    public static FoldableNavController findNavController(@NonNull View view) {
        FoldableNavController navController = findViewNavController(view);
        if (navController == null) {
            throw new IllegalStateException("View " + view + " does not have a FoldableNavController set");
        }
        return navController;
    }

    /**
     * Create an {@link View.OnClickListener} for navigating
     * to a destination. This supports both navigating via an
     * {@link FoldableNavDestination#getAction(int) action} and directly navigating to a destination.
     *
     * @param resId an {@link FoldableNavDestination#getAction(int) action} id or a destination id to
     *              navigate to when the view is clicked
     * @return a new click listener for setting on an arbitrary view
     */
    @NonNull
    public static View.OnClickListener createNavigateOnClickListener(@IdRes final int resId) {
        return createNavigateOnClickListener(resId, null);
    }

    /**
     * Create an {@link View.OnClickListener} for navigating
     * to a destination. This supports both navigating via an
     * {@link FoldableNavDestination#getAction(int) action} and directly navigating to a destination.
     *
     * @param resId an {@link FoldableNavDestination#getAction(int) action} id or a destination id to
     *              navigate to when the view is clicked
     * @param args  arguments to pass to the final destination
     * @return a new click listener for setting on an arbitrary view
     */
    @NonNull
    public static View.OnClickListener createNavigateOnClickListener(@IdRes final int resId,
                                                                     @Nullable final Bundle args) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findNavController(view).navigate(resId, args);
            }
        };
    }

    /**
     * Create an {@link View.OnClickListener} for navigating
     * to a destination via a generated {@link NavDirections}.
     *
     * @param directions directions that describe this navigation operation
     * @return a new click listener for setting on an arbitrary view
     */
    @NonNull
    public static View.OnClickListener createNavigateOnClickListener(
            @NonNull final NavDirections directions) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findNavController(view).navigate(directions);
            }
        };
    }

    /**
     * Associates a FoldableNavController with the given View, allowing developers to use
     * {@link #findNavController(View)} and {@link #findNavController(Activity, int)} with that
     * View or any of its children to retrieve the FoldablesNavController.
     * <p>
     * This is generally called for you by the hosting {@link NavHost}.
     *
     * @param view       View that should be associated with the given FoldablesNavController
     * @param controller The controller you wish to later retrieve via
     *                   {@link #findNavController(View)}
     */
    public static void setViewNavController(@NonNull View view,
                                            @Nullable FoldableNavController controller) {
        view.setTag(R.id.nav_controller_view_tag, controller);
    }

    /**
     * Recurse up the view hierarchy, looking for the FoldablesNavController
     *
     * @param view the view to search from
     * @return the locally scoped {@link FoldableNavController} to the given view, if found
     */
    @Nullable
    private static FoldableNavController findViewNavController(@NonNull View view) {
        while (view != null) {
            FoldableNavController controller = getViewNavController(view);
            if (controller != null) {
                return controller;
            }
            ViewParent parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static FoldableNavController getViewNavController(@NonNull View view) {
        Object tag = view.getTag(R.id.nav_controller_view_tag);
        FoldableNavController controller = null;
        if (tag instanceof WeakReference) {
            controller = ((WeakReference<FoldableNavController>) tag).get();
        } else if (tag instanceof FoldableNavController) {
            controller = (FoldableNavController) tag;
        }
        return controller;
    }
}
