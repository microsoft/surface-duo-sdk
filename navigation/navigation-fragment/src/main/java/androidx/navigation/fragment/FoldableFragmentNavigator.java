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

package androidx.navigation.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.FoldableNavController;
import androidx.navigation.FoldableNavDestination;
import com.microsoft.device.dualscreen.navigation.FoldableNavInflaterUtils;
import androidx.navigation.FoldableNavOptions;
import androidx.navigation.FoldableNavigator;
import androidx.navigation.FoldableNavigatorProvider;
import androidx.navigation.NavDestination;

import com.microsoft.device.dualscreen.navigation.FoldableFragmentManagerWrapper;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Navigator that navigates through {@link FragmentTransaction fragment transactions}. Every
 * destination using this Navigator must set a valid Fragment class name with
 * <code>android:name</code> or {@link Destination#setClassName(String)}.
 * <p>
 * The current Fragment from FragmentNavigator's perspective can be retrieved by calling
 * {@link FragmentManager#getPrimaryNavigationFragment()} with the FragmentManager
 * passed to this FragmentNavigator.
 * <p>
 * Note that the default implementation does Fragment transactions
 * asynchronously, so the current Fragment will not be available immediately
 * (i.e., in callbacks to {@link FoldableNavController.OnDestinationChangedListener}).
 */
@FoldableNavigator.Name("fragment")
public class FoldableFragmentNavigator extends FoldableNavigator<FoldableFragmentNavigator.Destination> {
    private static final String TAG = "FragmentNavigator";
    private static final String KEY_BACK_STACK_IDS = "androidx-foldable-nav-fragment:navigator:backStackIds";

    private final Context mContext;
    private final FoldableFragmentManagerWrapper mFragmentManager;
    private final int mContainerId;
    private ArrayDeque<Integer> mBackStack = new ArrayDeque<>();

    public FoldableFragmentNavigator(@NonNull Context context, @NonNull FoldableFragmentManagerWrapper manager,
                                     int containerId) {
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method must call
     * {@link FragmentTransaction#setPrimaryNavigationFragment(Fragment)}
     * if the pop succeeded so that the newly visible Fragment can be retrieved with
     * {@link FragmentManager#getPrimaryNavigationFragment()}.
     * <p>
     * Note that the default implementation pops the Fragment
     * asynchronously, so the newly visible Fragment from the back stack
     * is not instantly available after this call completes.
     */
    @Override
    public boolean popBackStack(boolean withTransition) {
        if (mBackStack.isEmpty()) {
            return false;
        }
        if (mFragmentManager.getFragmentManager().isStateSaved()) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already"
                    + " saved its state");
            return false;
        }
        mFragmentManager.popBackStack(withTransition,
                generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mBackStack.removeLast();
        return true;
    }

    @NonNull
    @Override
    public Destination createDestination() {
        return new Destination(this);
    }

    /**
     * Instantiates the Fragment via the FragmentManager's
     * {@link androidx.fragment.app.FragmentFactory}.
     * <p>
     * Note that this method is <strong>not</strong> responsible for calling
     * {@link Fragment#setArguments(Bundle)} on the returned Fragment instance.
     *
     * @param context         Context providing the correct {@link ClassLoader}
     * @param fragmentManager FragmentManager the Fragment will be added to
     * @param className       The Fragment to instantiate
     * @param args            The Fragment's arguments, if any
     * @return A new fragment instance.
     * @deprecated Set a custom {@link androidx.fragment.app.FragmentFactory} via
     * {@link FragmentManager#setFragmentFactory(FragmentFactory)} to control
     * instantiation of Fragments.
     */
    @SuppressWarnings("DeprecatedIsStillUsed") // needed to maintain forward compatibility
    @Deprecated
    @NonNull
    public Fragment instantiateFragment(@NonNull Context context,
                                        @NonNull FragmentManager fragmentManager,
                                        @NonNull String className, @SuppressWarnings("unused") @Nullable Bundle args) {
        return fragmentManager.getFragmentFactory().instantiate(
                context.getClassLoader(), className);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method should always call
     * {@link FragmentTransaction#setPrimaryNavigationFragment(Fragment)}
     * so that the Fragment associated with the new destination can be retrieved with
     * {@link FragmentManager#getPrimaryNavigationFragment()}.
     * <p>
     * Note that the default implementation commits the new Fragment
     * asynchronously, so the new Fragment is not instantly available
     * after this call completes.
     */
    @SuppressWarnings("deprecation") /* Using instantiateFragment for forward compatibility */
    @Nullable
    @Override
    public FoldableNavDestination navigate(@NonNull Destination destination, @Nullable Bundle args,
                                           @Nullable FoldableNavOptions navOptions, @Nullable FoldableNavigator.Extras navigatorExtras) {
        if (mFragmentManager.getFragmentManager().isStateSaved()) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
                    + " saved its state");
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }
        final Fragment frag = instantiateFragment(mContext, mFragmentManager.getFragmentManager(),
                className, args);
        frag.setArguments(args);

        if (navOptions == null) {
            navOptions = new FoldableNavOptions.Builder()
                    .setLaunchScreen(destination.getLaunchScreen())
                    .build();
        }

        final FragmentTransaction ft = mFragmentManager.beginTransaction(frag, navOptions);
        ft.setPrimaryNavigationFragment(frag);
        final @IdRes int destId = destination.getId();
        final boolean initialNavigation = mBackStack.isEmpty();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;
        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
            mFragmentManager.addToBackStack(frag, navOptions);
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size() > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mFragmentManager.popBackStack(true,
                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                mFragmentManager.addToBackStack(ft, frag, generateBackStackName(mBackStack.size(), destId), navOptions);
            } else {
                mFragmentManager.addToBackStack(frag, navOptions);
            }
            isAdded = false;
        } else {
            mFragmentManager.addToBackStack(ft, frag, generateBackStackName(mBackStack.size() + 1, destId), navOptions);
            isAdded = true;
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        ft.setReorderingAllowed(true);
        ft.commit();
        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        Bundle b = new Bundle();
        int[] backStack = new int[mBackStack.size()];
        int index = 0;
        for (Integer id : mBackStack) {
            backStack[index++] = id;
        }
        b.putIntArray(KEY_BACK_STACK_IDS, backStack);
        return b;
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            int[] backStack = savedState.getIntArray(KEY_BACK_STACK_IDS);
            if (backStack != null) {
                mBackStack.clear();
                for (int destId : backStack) {
                    mBackStack.add(destId);
                }
            }
        }
    }

    @NonNull
    private String generateBackStackName(int backStackIndex, int destId) {
        return backStackIndex + "-" + destId;
    }

    /**
     * NavDestination specific to {@link FoldableFragmentNavigator}
     */
    @NavDestination.ClassType(Fragment.class)
    public static class Destination extends FoldableNavDestination {
        private String mClassName;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param navigatorProvider The {@link FoldableNavController} which this destination
         *                          will be associated with.
         */
        public Destination(@NonNull FoldableNavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(FoldableFragmentNavigator.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param fragmentNavigator The {@link FoldableFragmentNavigator} which this destination
         *                          will be associated with. Generally retrieved via a
         *                          {@link FoldableNavController}'s
         *                          {@link FoldableNavigatorProvider#getNavigator(Class)} method.
         */
        public Destination(@NonNull FoldableNavigator<? extends Destination> fragmentNavigator) {
            super(fragmentNavigator);
        }

        @CallSuper
        @Override
        public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs) {
            super.onInflate(context, attrs);
            TypedArray a = context.getResources().obtainAttributes(attrs,
                    R.styleable.FragmentNavigator);
            String className = a.getString(R.styleable.FragmentNavigator_android_name);
            if (className != null) {
                setClassName(className);
            }
            a.recycle();

            setLaunchScreen(FoldableNavInflaterUtils.getLaunchScreen(context.getResources(), attrs));
        }

        /**
         * Set the Fragment class name associated with this destination
         *
         * @param className The class name of the Fragment to show when you navigate to this
         *                  destination
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setClassName(@NonNull String className) {
            mClassName = className;
            return this;
        }

        /**
         * Gets the Fragment's class name associated with this destination
         *
         * @throws IllegalStateException when no Fragment class was set.
         */
        @NonNull
        public final String getClassName() {
            if (mClassName == null) {
                throw new IllegalStateException("Fragment class was not set");
            }
            return mClassName;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" class=");
            if (mClassName == null) {
                sb.append("null");
            } else {
                sb.append(mClassName);
            }
            return sb.toString();
        }
    }

    /**
     * Extras that can be passed to FoldableFragmentNavigator to enable Fragment specific behavior
     */
    public static final class Extras implements FoldableNavigator.Extras {
        private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

        Extras(Map<View, String> sharedElements) {
            mSharedElements.putAll(sharedElements);
        }

        /**
         * Gets the map of shared elements associated with these Extras. The returned map
         * is an {@link Collections#unmodifiableMap(Map) unmodifiable} copy of the underlying
         * map and should be treated as immutable.
         */
        @NonNull
        public Map<View, String> getSharedElements() {
            return Collections.unmodifiableMap(mSharedElements);
        }

        /**
         * Builder for constructing new {@link Extras} instances. The resulting instances are
         * immutable.
         */
        public static final class Builder {
            private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

            /**
             * Adds multiple shared elements for mapping Views in the current Fragment to
             * transitionNames in the Fragment being navigated to.
             *
             * @param sharedElements Shared element pairs to add
             * @return this {@link Builder}
             */
            @NonNull
            public Builder addSharedElements(@NonNull Map<View, String> sharedElements) {
                for (Map.Entry<View, String> sharedElement : sharedElements.entrySet()) {
                    View view = sharedElement.getKey();
                    String name = sharedElement.getValue();
                    if (view != null && name != null) {
                        addSharedElement(view, name);
                    }
                }
                return this;
            }

            /**
             * Maps the given View in the current Fragment to the given transition name in the
             * Fragment being navigated to.
             *
             * @param sharedElement A View in the current Fragment to match with a View in the
             *                      Fragment being navigated to.
             * @param name          The transitionName of the View in the Fragment being navigated to that
             *                      should be matched to the shared element.
             * @return this {@link Builder}
             * @see FragmentTransaction#addSharedElement(View, String)
             */
            @NonNull
            public Builder addSharedElement(@NonNull View sharedElement, @NonNull String name) {
                mSharedElements.put(sharedElement, name);
                return this;
            }

            /**
             * Constructs the final {@link Extras} instance.
             *
             * @return An immutable {@link Extras} instance.
             */
            @NonNull
            public Extras build() {
                return new Extras(mSharedElements);
            }
        }
    }
}