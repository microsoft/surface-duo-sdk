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

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.CallSuper;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.FoldableNavController;
import androidx.navigation.FoldableNavGraph;
import androidx.navigation.FoldableNavHost;
import androidx.navigation.FoldableNavHostController;
import androidx.navigation.FoldableNavigation;
import androidx.navigation.FoldableNavigator;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import com.microsoft.device.dualscreen.layouts.FoldableLayout;
import com.microsoft.device.dualscreen.navigation.FoldableFragmentManagerWrapper;
import com.microsoft.device.dualscreen.navigation.FoldableLayoutExtensionsKt;
import com.microsoft.device.dualscreen.navigation.RequestConfigListener;
import com.microsoft.device.dualscreen.navigation.RequestConfigParams;
import com.microsoft.device.dualscreen.utils.wm.WindowLayoutInfoExtensionsKt;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

/**
 * NavHostFragment provides an area within your layout for self-contained navigation to occur.
 *
 * <p>NavHostFragment is intended to be used as the content area within a layout resource
 * defining your app's chrome around it, e.g.:</p>
 *
 * <pre class="prettyprint">
 * &lt;androidx.drawerlayout.widget.DrawerLayout
 *        xmlns:android="http://schemas.android.com/apk/res/android"
 *        xmlns:app="http://schemas.android.com/apk/res-auto"
 *        android:layout_width="match_parent"
 *        android:layout_height="match_parent"&gt;
 *    &lt;fragment
 *            android:layout_width="match_parent"
 *            android:layout_height="match_parent"
 *            android:id="@+id/my_nav_host_fragment"
 *            android:name="androidx.navigation.fragment.FoldableNavHostFragment"
 *            app:navGraph="@navigation/nav_sample"
 *            app:defaultNavHost="true" /&gt;
 *    &lt;com.google.android.material.navigation.NavigationView
 *            android:layout_width="wrap_content"
 *            android:layout_height="match_parent"
 *            android:layout_gravity="start"/&gt;
 * &lt;/androidx.drawerlayout.widget.DrawerLayout&gt;
 * </pre>
 *
 * <p>Each NavHostFragment has a {@link FoldableNavController} that defines valid navigation within
 * the navigation host. This includes the {@link FoldableNavGraph navigation graph} as well as navigation
 * state such as current location and back stack that will be saved and restored along with the
 * NavHostFragment itself.</p>
 *
 * <p>NavHostFragments register their navigation controller at the root of their view subtree
 * such that any descendant can obtain the controller instance through the {@link FoldableNavigation}
 * helper class's methods such as {@link FoldableNavigation#findNavController(View)}. View event listener
 * implementations such as {@link android.view.View.OnClickListener} within navigation destination
 * fragments can use these helpers to navigate based on user interaction without creating a tight
 * coupling to the navigation host.</p>
 */
public class FoldableNavHostFragment extends Fragment implements FoldableNavHost, RequestConfigListener {
    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    protected static final String KEY_GRAPH_ID = "android-support-nav:fragment:graphId";
    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    protected static final String KEY_START_DESTINATION_ARGS =
            "android-support-nav:fragment:startDestinationArgs";
    private static final String KEY_NAV_CONTROLLER_STATE =
            "android-support-nav:fragment:navControllerState";
    private static final String KEY_DEFAULT_NAV_HOST = "android-support-nav:fragment:defaultHost";

    /**
     * Find a {@link FoldableNavController} given a local {@link Fragment}.
     *
     * <p>This method will locate the {@link FoldableNavController} associated with this Fragment,
     * looking first for a {@link FoldableNavHostFragment} along the given Fragment's parent chain.
     * If a {@link FoldableNavController} is not found, this method will look for one along this
     * Fragment's {@link Fragment#getView() view hierarchy} as specified by
     * {@link FoldableNavigation#findNavController(View)}.</p>
     *
     * @param fragment the locally scoped Fragment for navigation
     * @return the locally scoped {@link FoldableNavController} for navigating from this {@link Fragment}
     * @throws IllegalStateException if the given Fragment does not correspond with a
     *                               {@link FoldableNavHost} or is not within a NavHost.
     */
    @NonNull
    public static FoldableNavController findNavController(@NonNull Fragment fragment) {
        Fragment findFragment = fragment;
        while (findFragment != null) {
            if (findFragment instanceof FoldableNavHostFragment) {
                return ((FoldableNavHostFragment) findFragment).getNavController();
            }
            Fragment primaryNavFragment = findFragment.getParentFragmentManager()
                    .getPrimaryNavigationFragment();
            if (primaryNavFragment instanceof FoldableNavHostFragment) {
                return ((FoldableNavHostFragment) primaryNavFragment).getNavController();
            }
            findFragment = findFragment.getParentFragment();
        }
        // Try looking for one associated with the view instead, if applicable
        View view = fragment.getView();
        if (view != null) {
            return FoldableNavigation.findNavController(view);
        }
        // For DialogFragments, look at the dialog's decor view
        Dialog dialog = fragment instanceof DialogFragment
                ? ((DialogFragment) fragment).getDialog()
                : null;
        if (dialog != null && dialog.getWindow() != null) {
            return FoldableNavigation.findNavController(dialog.getWindow().getDecorView());
        }
        throw new IllegalStateException("Fragment " + fragment
                + " does not have a NavController set");
    }

    private FoldableNavHostController mNavController;
    private Boolean mIsPrimaryBeforeOnCreate = null;
    private View mViewParent;
    // State that will be saved and restored
    private int mGraphId;
    private boolean mDefaultNavHost;

    private FoldableFragmentManagerWrapper mFoldableFragmentManager;
    private RequestConfigParams mRequestConfigParams;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final Executor mUiThreadExecutor = mUiHandler::post;
    private WindowInfoTrackerCallbackAdapter mWindowInfoAdapter;
    private final Consumer<WindowLayoutInfo> mWindowInfoConsumer = windowLayoutInfo -> {
        mFoldableFragmentManager.setScreenMode(WindowLayoutInfoExtensionsKt.getScreenMode(windowLayoutInfo));
    };

    /**
     * Create a new NavHostFragment instance with an inflated {@link FoldableNavGraph} resource.
     *
     * @param graphResId resource id of the navigation graph to inflate
     * @return a new NavHostFragment instance
     */
    @NonNull
    public static FoldableNavHostFragment create(@NavigationRes int graphResId) {
        return create(graphResId, null);
    }

    /**
     * Create a new NavHostFragment instance with an inflated {@link FoldableNavGraph} resource.
     *
     * @param graphResId           Resource id of the navigation graph to inflate.
     * @param startDestinationArgs Arguments to send to the start destination of the graph.
     * @return A new NavHostFragment instance.
     */
    @NonNull
    public static FoldableNavHostFragment create(@NavigationRes int graphResId,
                                                 @Nullable Bundle startDestinationArgs) {
        Bundle b = null;
        if (graphResId != 0) {
            b = new Bundle();
            b.putInt(KEY_GRAPH_ID, graphResId);
        }
        if (startDestinationArgs != null) {
            if (b == null) {
                b = new Bundle();
            }
            b.putBundle(KEY_START_DESTINATION_ARGS, startDestinationArgs);
        }
        final FoldableNavHostFragment result = new FoldableNavHostFragment();
        if (b != null) {
            result.setArguments(b);
        }
        return result;
    }

    /**
     * Returns the {@link FoldableNavController navigation controller} for this navigation host.
     * This method will return null until this host fragment's {@link #onCreate(Bundle)}
     * has been called and it has had an opportunity to restore from a previous instance state.
     *
     * @return this host's navigation controller
     * @throws IllegalStateException if called before {@link #onCreate(Bundle)}
     */
    @NonNull
    @Override
    public final FoldableNavController getNavController() {
        if (mNavController == null) {
            throw new IllegalStateException("NavController is not available before onCreate()");
        }
        return mNavController;
    }

    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // TODO This feature should probably be a first-class feature of the Fragment system,
        // but it can stay here until we can add the necessary attr resources to
        // the fragment lib.
        if (mDefaultNavHost) {
            getParentFragmentManager().beginTransaction()
                    .setPrimaryNavigationFragment(this)
                    .commit();
        }

        mWindowInfoAdapter =
                new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(requireActivity()));
    }

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        final Context context = requireContext();
        mNavController = new FoldableNavHostController(context);
        mNavController.setLifecycleOwner(this);
        if (context instanceof OnBackPressedDispatcherOwner) {
            mNavController.setOnBackPressedDispatcher(
                    ((OnBackPressedDispatcherOwner) context).getOnBackPressedDispatcher());
            // Otherwise, caller must register a dispatcher on the controller explicitly
            // by overriding onCreateNavHostController()
        }
        // Set the default state - this will be updated whenever
        // onPrimaryNavigationFragmentChanged() is called
        mNavController.enableOnBackPressed(
                mIsPrimaryBeforeOnCreate != null && mIsPrimaryBeforeOnCreate);
        mIsPrimaryBeforeOnCreate = null;
        mNavController.setViewModelStore(getViewModelStore());
        onCreateNavHostController(mNavController);
        Bundle navState = null;
        if (savedInstanceState != null) {
            navState = savedInstanceState.getBundle(KEY_NAV_CONTROLLER_STATE);
            if (savedInstanceState.getBoolean(KEY_DEFAULT_NAV_HOST, false)) {
                mDefaultNavHost = true;
                getParentFragmentManager().beginTransaction()
                        .setPrimaryNavigationFragment(this)
                        .commit();
            }
            mGraphId = savedInstanceState.getInt(KEY_GRAPH_ID);
        }
        if (navState != null) {
            // Navigation controller state overrides arguments
            mNavController.restoreState(navState);
        }
        if (mGraphId != 0) {
            // Set from onInflate()
            mNavController.setGraph(mGraphId);
        } else {
            // See if it was set by NavHostFragment.create()
            final Bundle args = getArguments();
            final int graphId = args != null ? args.getInt(KEY_GRAPH_ID) : 0;
            final Bundle startDestinationArgs = args != null
                    ? args.getBundle(KEY_START_DESTINATION_ARGS)
                    : null;
            if (graphId != 0) {
                mNavController.setGraph(graphId, startDestinationArgs);
            }
        }
        // We purposefully run this last as this will trigger the onCreate() of
        // child fragments, which may be relying on having the NavController already
        // created and having its state restored by that point.
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mWindowInfoAdapter.addWindowLayoutInfoListener(requireActivity(), mUiThreadExecutor, mWindowInfoConsumer);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWindowInfoAdapter.removeWindowLayoutInfoListener(mWindowInfoConsumer);
    }

    /**
     * Callback for when the {@link FoldableNavHostController} is created. If you
     * support any custom destination types, their {@link FoldableNavigator} should be added here to
     * ensure it is available before the navigation graph is inflated / set.
     * <p>
     * This provides direct access to the host specific methods available on
     * {@link FoldableNavHostController} such as
     * {@link FoldableNavHostController#setOnBackPressedDispatcher(OnBackPressedDispatcher)}.
     * <p>
     * By default, this adds a {@link FoldableDialogFragmentNavigator} and {@link FoldableFragmentNavigator}.
     * <p>
     * This is only called once in {@link #onCreate(Bundle)} and should not be called directly by
     * subclasses.
     *
     * @param navHostController The newly created {@link FoldableNavHostController} that will be
     *                          returned by {@link #getNavController()} after
     */
    @SuppressWarnings("deprecation")
    @CallSuper
    protected void onCreateNavHostController(@NonNull FoldableNavHostController navHostController) {
        onCreateNavController(navHostController);
    }

    /**
     * Callback for when the {@link #getNavController() NavController} is created. If you
     * support any custom destination types, their {@link FoldableNavigator} should be added here to
     * ensure it is available before the navigation graph is inflated / set.
     * <p>
     * By default, this adds a {@link FoldableDialogFragmentNavigator} and {@link FoldableFragmentNavigator}.
     * <p>
     * This is only called once in {@link #onCreate(Bundle)} and should not be called directly by
     * subclasses.
     *
     * @param navController The newly created {@link FoldableNavController}.
     * @deprecated Override {@link #onCreateNavHostController(FoldableNavHostController)} to gain
     * access to the full {@link FoldableNavHostController} that is created by this NavHostFragment.
     */
    @SuppressWarnings({"DeprecatedIsStillUsed", "deprecation"})
    @Deprecated
    @CallSuper
    protected void onCreateNavController(@NonNull FoldableNavController navController) {
        navController.getNavigatorProvider().addNavigator(
                new FoldableDialogFragmentNavigator(requireContext(), getChildFragmentManager()));
        navController.getNavigatorProvider().addNavigator(createFragmentNavigator());
    }

    // TODO: DialogFragmentNavigator should use FragmentOnAttachListener from Fragment 1.3
    @SuppressWarnings("deprecation")
    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        FoldableDialogFragmentNavigator dialogFragmentNavigator =
                mNavController.getNavigatorProvider().getNavigator(FoldableDialogFragmentNavigator.class);
        dialogFragmentNavigator.onAttachFragment(childFragment);
    }

    @CallSuper
    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        if (mNavController != null) {
            mNavController.enableOnBackPressed(isPrimaryNavigationFragment);
        } else {
            mIsPrimaryBeforeOnCreate = isPrimaryNavigationFragment;
        }
    }

    /**
     * Create the FoldableFragmentNavigator that this FoldableNavHostFragment will use. By default, this uses
     * {@link FoldableFragmentNavigator}, which replaces the entire contents of the FoldableNavHostFragment.
     * <p>
     * This is only called once in {@link #onCreate(Bundle)} and should not be called directly by
     * subclasses.
     *
     * @return a new instance of a FoldableFragmentNavigator
     * @deprecated Use {@link #onCreateNavController(FoldableNavController)}
     */
    @Deprecated
    @NonNull
    protected FoldableNavigator<? extends FoldableFragmentNavigator.Destination> createFragmentNavigator() {
        mFoldableFragmentManager = new FoldableFragmentManagerWrapper(getChildFragmentManager(), this);
        return new FoldableFragmentNavigator(requireContext(), mFoldableFragmentManager,
                getContainerId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View containerView = inflater.inflate(R.layout.fragment_foldable_nav_host, container, false);
        containerView.setId(getContainerId());

        return containerView;
    }

    /**
     * We specifically can't use {@link View#NO_ID} as the container ID (as we use
     * {@link androidx.fragment.app.FragmentTransaction#add(int, Fragment)} under the hood),
     * so we need to make sure we return a valid ID when asked for the container ID.
     *
     * @return a valid ID to be used to contain child fragments
     */
    private int getContainerId() {
        int id = getId();
        if (id != 0 && id != View.NO_ID) {
            return id;
        }
        // Fallback to using our own ID if this Fragment wasn't added via
        // add(containerViewId, Fragment)
        return R.id.nav_host_fragment_container;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFoldableFragmentManager.onRestoreInstanceState(savedInstanceState);
        if (!(view instanceof ViewGroup)) {
            throw new IllegalStateException("created host view " + view + " is not a ViewGroup");
        }
        FoldableNavigation.setViewNavController(view, mNavController);
        // When added programmatically, we need to set the NavController on the parent - i.e.,
        // the View that has the ID matching this NavHostFragment.
        if (view.getParent() != null) {
            mViewParent = (View) view.getParent();
            if (mViewParent.getId() == getId()) {
                FoldableNavigation.setViewNavController(mViewParent, mNavController);
            }
        }

        RequestConfigParams requestConfigParams = mRequestConfigParams;
        if (requestConfigParams != null) {
            if (view instanceof FoldableLayout) {
                view.post(() -> FoldableLayoutExtensionsKt.changeConfiguration((FoldableLayout) view, requestConfigParams));
            }
            mRequestConfigParams = null;
        }
    }

    @CallSuper
    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs,
                          @Nullable Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        final TypedArray navHost = context.obtainStyledAttributes(attrs,
                androidx.navigation.R.styleable.NavHost);
        final int graphId = navHost.getResourceId(
                androidx.navigation.R.styleable.NavHost_navGraph, 0);
        if (graphId != 0) {
            mGraphId = graphId;
        }
        navHost.recycle();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavHostFragment);
        final boolean defaultHost = a.getBoolean(R.styleable.NavHostFragment_defaultNavHost, false);
        if (defaultHost) {
            mDefaultNavHost = true;
        }
        a.recycle();
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle navState = mNavController.saveState();
        if (navState != null) {
            outState.putBundle(KEY_NAV_CONTROLLER_STATE, navState);
        }
        if (mDefaultNavHost) {
            outState.putBoolean(KEY_DEFAULT_NAV_HOST, true);
        }
        if (mGraphId != 0) {
            outState.putInt(KEY_GRAPH_ID, mGraphId);
        }
        mFoldableFragmentManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mViewParent != null && FoldableNavigation.findNavController(mViewParent) == mNavController) {
            FoldableNavigation.setViewNavController(mViewParent, null);
        }
        mViewParent = null;
    }

    @Override
    public void changeConfiguration(@NotNull RequestConfigParams params) {
        View view = getView();
        if (view == null) {
            mRequestConfigParams = params;
            return;
        }

        if (view instanceof FoldableLayout) {
            view.post(() -> FoldableLayoutExtensionsKt.changeConfiguration((FoldableLayout) view, params));
        }
    }
}
