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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * FoldableNavigator defines a mechanism for navigating within an app.
 *
 * <p>Each FoldableNavigator sets the policy for a specific type of navigation, e.g.
 * {@link FoldableActivityNavigator} knows how to launch into {@link FoldableNavDestination destinations}
 * backed by activities using {@link Context#startActivity(Intent) startActivity}.</p>
 *
 * <p>Navigators should be able to manage their own back stack when navigating between two
 * destinations that belong to that navigator. The {@link FoldableNavController} manages a back stack of
 * navigators representing the current navigation stack across all navigators.</p>
 *
 * <p>Each FoldableNavigator should add the {@link Name FoldableNavigator.Name annotation} to their class. Any
 * custom attributes used by the associated {@link FoldableNavDestination destination} subclass should
 * have a name corresponding with the name of the FoldableNavigator, e.g., {@link FoldableActivityNavigator} uses
 * <code>&lt;declare-styleable name="FoldableActivityNavigator"&gt;</code></p>
 *
 * @param <D> the subclass of {@link FoldableNavDestination} used with this FoldableNavigator which can be used
 *            to hold any special data that will be needed to navigate to that destination.
 *            Examples include information about an intent to navigate to other activities,
 *            or a fragment class name to instantiate and swap to a new fragment.
 */
public abstract class FoldableNavigator<D extends FoldableNavDestination> {
    /**
     * This annotation should be added to each FoldableNavigator subclass to denote the default name used
     * to register the FoldableNavigator with a {@link NavigatorProvider}.
     *
     * @see NavigatorProvider#addNavigator(FoldableNavigator)
     * @see NavigatorProvider#getNavigator(Class)
     */
    @Retention(RUNTIME)
    @Target({TYPE})
    @SuppressWarnings("UnknownNullness") // TODO https://issuetracker.google.com/issues/112185120
    public @interface Name {
        String value();
    }

    /**
     * Construct a new FoldableNavDestination associated with this FoldableNavigator.
     *
     * <p>Any initialization of the destination should be done in the destination's constructor as
     * it is not guaranteed that every destination will be created through this method.</p>
     *
     * @return a new FoldableNavDestination
     */
    @NonNull
    public abstract D createDestination();

    /**
     * Navigate to a destination.
     *
     * <p>Requests navigation to a given destination associated with this navigator in
     * the navigation graph. This method generally should not be called directly;
     * {@link FoldableNavController} will delegate to it when appropriate.</p>
     *
     * @param destination     destination node to navigate to
     * @param args            arguments to use for navigation
     * @param navOptions      additional options for navigation
     * @param navigatorExtras extras unique to your FoldableNavigator.
     * @return The FoldableNavDestination that should be added to the back stack or null if
     * no change was made to the back stack (i.e., in cases of single top operations
     * where the destination is already on top of the back stack).
     */
    @Nullable
    public abstract FoldableNavDestination navigate(@NonNull D destination, @Nullable Bundle args,
                                                    @Nullable FoldableNavOptions navOptions, @Nullable Extras navigatorExtras);

    /**
     * Attempt to pop this navigator's back stack, performing the appropriate navigation.
     *
     * <p>Implementations should return {@code true} if navigation
     * was successful. Implementations should return {@code false} if navigation could not
     * be performed, for example if the navigator's back stack was empty.</p>
     *
     * @return {@code true} if pop was successful
     */
    public abstract boolean popBackStack(boolean withTransition);

    /**
     * Called to ask for a {@link Bundle} representing the FoldableNavigator's state. This will be
     * restored in {@link #onRestoreState(Bundle)}.
     */
    @Nullable
    public Bundle onSaveState() {
        return null;
    }

    /**
     * Restore any state previously saved in {@link #onSaveState()}. This will be called before
     * any calls to {@link #navigate(FoldableNavDestination, Bundle, FoldableNavOptions, FoldableNavigator.Extras)} or
     * {@link #popBackStack(boolean)}.
     * <p>
     * Calls to {@link #createDestination()} should not be dependent on any state restored here as
     * {@link #createDestination()} can be called before the state is restored.
     *
     * @param savedState The state previously saved
     */
    public void onRestoreState(@NonNull Bundle savedState) {
    }

    /**
     * Interface indicating that this class should be passed to its respective
     * {@link FoldableNavigator} to enable FoldableNavigator specific behavior.
     */
    public interface Extras {
    }
}