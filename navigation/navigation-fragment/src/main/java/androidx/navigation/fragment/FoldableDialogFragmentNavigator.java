/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.FoldableNavController;
import androidx.navigation.FoldableNavDestination;
import androidx.navigation.FoldableNavOptions;
import androidx.navigation.FoldableNavigator;
import androidx.navigation.FoldableNavigatorProvider;
import androidx.navigation.FloatingWindow;

import java.util.HashSet;

/**
 * Navigator that uses {@link DialogFragment#show(FragmentManager, String)}. Every
 * destination using this Navigator must set a valid DialogFragment class name with
 * <code>android:name</code> or {@link Destination#setClassName(String)}.
 */
@FoldableNavigator.Name("dialog")
public final class FoldableDialogFragmentNavigator extends FoldableNavigator<FoldableDialogFragmentNavigator.Destination> {
    private static final String TAG = "DialogFragmentNavigator";
    private static final String KEY_DIALOG_COUNT = "androidx-nav-dialogfragment:navigator:count";
    private static final String DIALOG_TAG = "androidx-nav-fragment:navigator:dialog:";
    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private int mDialogCount = 0;
    private final HashSet<String> mRestoredTagsAwaitingAttach = new HashSet<>();
    private LifecycleEventObserver mObserver = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source,
                                   @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_STOP) {
                DialogFragment dialogFragment = (DialogFragment) source;
                if (!dialogFragment.requireDialog().isShowing()) {
                    FoldableNavHostFragment.findNavController(dialogFragment).popBackStack(false);
                }
            }
        }
    };

    public FoldableDialogFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager) {
        mContext = context;
        mFragmentManager = manager;
    }

    @Override
    public boolean popBackStack(boolean withTransition) {
        if (mDialogCount == 0) {
            return false;
        }
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already"
                    + " saved its state");
            return false;
        }
        Fragment existingFragment = mFragmentManager
                .findFragmentByTag(DIALOG_TAG + --mDialogCount);
        if (existingFragment != null) {
            existingFragment.getLifecycle().removeObserver(mObserver);
            ((DialogFragment) existingFragment).dismiss();
        }
        return true;
    }

    @NonNull
    @Override
    public Destination createDestination() {
        return new Destination(this);
    }

    @Nullable
    @Override
    public FoldableNavDestination navigate(@NonNull final Destination destination, @Nullable Bundle args,
                                           @Nullable FoldableNavOptions navOptions, @Nullable FoldableNavigator.Extras navigatorExtras) {
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
                    + " saved its state");
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }
        final Fragment frag = mFragmentManager.getFragmentFactory().instantiate(
                mContext.getClassLoader(), className);
        if (!DialogFragment.class.isAssignableFrom(frag.getClass())) {
            throw new IllegalArgumentException("Dialog destination " + destination.getClassName()
                    + " is not an instance of DialogFragment");
        }
        final DialogFragment dialogFragment = (DialogFragment) frag;
        dialogFragment.setArguments(args);
        dialogFragment.getLifecycle().addObserver(mObserver);
        dialogFragment.show(mFragmentManager, DIALOG_TAG + mDialogCount++);
        return destination;
    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        if (mDialogCount == 0) {
            return null;
        }
        Bundle b = new Bundle();
        b.putInt(KEY_DIALOG_COUNT, mDialogCount);
        return b;
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            mDialogCount = savedState.getInt(KEY_DIALOG_COUNT, 0);
            for (int index = 0; index < mDialogCount; index++) {
                DialogFragment fragment = (DialogFragment) mFragmentManager
                        .findFragmentByTag(DIALOG_TAG + index);
                if (fragment != null) {
                    fragment.getLifecycle().addObserver(mObserver);
                } else {
                    mRestoredTagsAwaitingAttach.add(DIALOG_TAG + index);
                }
            }
        }
    }

    // TODO: Switch to FragmentOnAttachListener once we depend on Fragment 1.3
    void onAttachFragment(@NonNull Fragment childFragment) {
        boolean needToAddObserver = mRestoredTagsAwaitingAttach.remove(childFragment.getTag());
        if (needToAddObserver) {
            childFragment.getLifecycle().addObserver(mObserver);
        }
    }

    /**
     * NavDestination specific to {@link FoldableDialogFragmentNavigator}.
     */
    @FoldableNavDestination.ClassType(DialogFragment.class)
    public static class Destination extends FoldableNavDestination implements FloatingWindow {
        private String mClassName;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param navigatorProvider The {@link FoldableNavController} which this destination
         *                          will be associated with.
         */
        public Destination(@NonNull FoldableNavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(FoldableDialogFragmentNavigator.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param fragmentNavigator The {@link FoldableDialogFragmentNavigator} which this destination
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
                    R.styleable.DialogFragmentNavigator);
            String className = a.getString(R.styleable.DialogFragmentNavigator_android_name);
            if (className != null) {
                setClassName(className);
            }
            a.recycle();
        }

        /**
         * Set the DialogFragment class name associated with this destination
         *
         * @param className The class name of the DialogFragment to show when you navigate to this
         *                  destination
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setClassName(@NonNull String className) {
            mClassName = className;
            return this;
        }

        /**
         * Gets the DialogFragment's class name associated with this destination
         *
         * @throws IllegalStateException when no DialogFragment class was set.
         */
        @NonNull
        public final String getClassName() {
            if (mClassName == null) {
                throw new IllegalStateException("DialogFragment class was not set");
            }
            return mClassName;
        }
    }
}
