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

package androidx.navigation.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.customview.widget.Openable;
import androidx.navigation.FoldableActivityNavigator;
import androidx.navigation.FoldableNavController;
import androidx.navigation.FoldableNavDestination;
import androidx.navigation.FoldableNavGraph;
import androidx.navigation.FoldableNavOptions;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.microsoft.device.dualscreen.navigation.LaunchScreen;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Class which hooks up elements typically in the 'chrome' of your application such as global
 * navigation patterns like a navigation drawer or bottom nav bar with your {@link FoldableNavController}.
 */
public final class FoldableNavigationUI {

    // No instances. Static utilities only.
    private FoldableNavigationUI() {
    }

    /**
     * Attempt to navigate to the {@link FoldableNavDestination} associated with the given MenuItem. This
     * MenuItem should have been added via one of the helper methods in this class.
     *
     * <p>Importantly, it assumes the {@link MenuItem#getItemId() menu item id} matches a valid
     * {@link FoldableNavDestination#getAction(int) action id} or
     * {@link FoldableNavDestination#getId() destination id} to be navigated to.</p>
     * <p>
     * By default, the back stack will be popped back to the navigation graph's start destination.
     * Menu items that have <code>android:menuCategory="secondary"</code> will not pop the back
     * stack.
     *
     * @param item The selected MenuItem.
     * @param navController The FoldableNavController that hosts the destination.
     * @return True if the {@link FoldableNavController} was able to navigate to the destination
     * associated with the given MenuItem.
     */
    public static boolean onNavDestinationSelected(@NonNull MenuItem item,
            @NonNull FoldableNavController navController) {
        FoldableNavOptions.Builder builder = new FoldableNavOptions.Builder()
                .setLaunchSingleTop(true);
        if (navController.getCurrentDestination().getParent().findNode(item.getItemId())
                instanceof FoldableActivityNavigator.Destination) {
            builder.setEnterAnim(R.anim.nav_default_enter_anim)
                    .setExitAnim(R.anim.nav_default_exit_anim)
                    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                    .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                    .setLaunchScreen(LaunchScreen.DEFAULT);

        } else {
            builder.setEnterAnim(R.animator.nav_default_enter_anim)
                    .setExitAnim(R.animator.nav_default_exit_anim)
                    .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
                    .setPopExitAnim(R.animator.nav_default_pop_exit_anim)
                    .setLaunchScreen(LaunchScreen.DEFAULT);
        }
        if ((item.getOrder() & Menu.CATEGORY_SECONDARY) == 0) {
            builder.setPopUpTo(findStartDestination(navController.getGraph()).getId(), false);
        }
        FoldableNavOptions options = builder.build();
        try {
            //TODO provide proper API instead of using Exceptions as Control-Flow.
            navController.navigate(item.getItemId(), null, options);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Handles the Up button by delegating its behavior to the given FoldableNavController. This should
     * generally be called from {@link AppCompatActivity#onSupportNavigateUp()}.
     * <p>If you do not have a {@link Openable} layout, you should call
     * {@link FoldableNavController#navigateUp()} directly.
     *
     * @param navController The FoldableNavController that hosts your content.
     * @param openableLayout The Openable layout that should be opened if you are on the topmost
     *                       level of the app.
     * @return True if the {@link FoldableNavController} was able to navigate up.
     */
    public static boolean navigateUp(@NonNull FoldableNavController navController,
            @Nullable Openable openableLayout) {
        return navigateUp(navController, new FoldableAppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(openableLayout)
                .build());
    }

    /**
     * Handles the Up button by delegating its behavior to the given FoldableNavController. This is
     * an alternative to using {@link FoldableNavController#navigateUp()} directly when the given
     * {@link FoldableAppBarConfiguration} needs to be considered when determining what should happen
     * when the Up button is pressed.
     * <p>
     * In cases where no Up action is available, the
     * {@link FoldableAppBarConfiguration#getFallbackOnNavigateUpListener()} will be called to provide
     * additional control.
     *
     * @param navController The FoldableNavController that hosts your content.
     * @param configuration Additional configuration options for determining what should happen
     *                      when the Up button is pressed.
     * @return True if the {@link FoldableNavController} was able to navigate up.
     */
    public static boolean navigateUp(@NonNull FoldableNavController navController,
            @NonNull FoldableAppBarConfiguration configuration) {
        Openable openableLayout = configuration.getOpenableLayout();
        FoldableNavDestination currentDestination = navController.getCurrentDestination();
        Set<Integer> topLevelDestinations = configuration.getTopLevelDestinations();
        if (openableLayout != null && currentDestination != null
                && matchDestinations(currentDestination, topLevelDestinations)) {
            openableLayout.open();
            return true;
        } else {
            if (navController.navigateUp()) {
                return true;
            } else if (configuration.getFallbackOnNavigateUpListener() != null) {
                return configuration.getFallbackOnNavigateUpListener().onNavigateUp();
            } else {
                return false;
            }
        }
    }

    /**
     * Sets up the ActionBar returned by {@link AppCompatActivity#getSupportActionBar()} for use
     * with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the action bar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On all other destinations, the ActionBar will show the Up button.
     * Call {@link FoldableNavController#navigateUp()} to handle the Up button.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param activity The activity hosting the action bar that should be kept in sync with changes
     *                 to the FoldableNavController.
     * @param navController The FoldableNavController that supplies the secondary menu. Navigation actions
     *                      on this FoldableNavController will be reflected in the title of the action bar.
     * @see #setupActionBarWithFoldableNavController(AppCompatActivity, FoldableNavController, FoldableAppBarConfiguration)
     */
    public static void setupActionBarWithFoldableNavController(@NonNull AppCompatActivity activity,
                                                               @NonNull FoldableNavController navController) {
        setupActionBarWithFoldableNavController(activity, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph())
                        .build());
    }

    /**
     * Sets up the ActionBar returned by {@link AppCompatActivity#getSupportActionBar()} for use
     * with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the action bar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On the start destination of your navigation graph, the ActionBar will show
     * the drawer icon if the given Openable layout is non null. On all other destinations,
     * the ActionBar will show the Up button.
     * Call {@link #navigateUp(FoldableNavController, Openable)} to handle the Up button.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param activity The activity hosting the action bar that should be kept in sync with changes
     *                 to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the action bar.
     * @param openableLayout The Openable layout that should be toggled from the home button
     * @see #setupActionBarWithFoldableNavController(AppCompatActivity, FoldableNavController, FoldableAppBarConfiguration)
     */
    public static void setupActionBarWithFoldableNavController(@NonNull AppCompatActivity activity,
                                                               @NonNull FoldableNavController navController,
                                                               @Nullable Openable openableLayout) {
        setupActionBarWithFoldableNavController(activity, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph())
                        .setOpenableLayout(openableLayout)
                        .build());
    }

    /**
     * Sets up the ActionBar returned by {@link AppCompatActivity#getSupportActionBar()} for use
     * with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the action bar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The {@link FoldableAppBarConfiguration} you provide controls how the Navigation button is
     * displayed.
     * Call {@link #navigateUp(FoldableNavController, FoldableAppBarConfiguration)} to handle the Up button.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     *  @param activity The activity hosting the action bar that should be kept in sync with changes
     *                 to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the action bar.
     * @param configuration Additional configuration options for customizing the behavior of the
     *                      ActionBar
     */
    public static void setupActionBarWithFoldableNavController(@NonNull AppCompatActivity activity,
                                                               @NonNull FoldableNavController navController,
                                                               @NonNull FoldableAppBarConfiguration configuration) {
        navController.addOnDestinationChangedListener(
                new FoldableActionBarOnDestinationChangedListener(activity, configuration));
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On all other destinations, the Toolbar will show the Up button. This
     * method will call {@link FoldableNavController#navigateUp()} when the Navigation button
     * is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController that supplies the secondary menu. Navigation actions
     *                      on this FoldableNavController will be reflected in the title of the Toolbar.
     * @see #setupWithFoldableNavController(Toolbar, FoldableNavController, FoldableAppBarConfiguration)
     */
    public static void setupWithFoldableNavController(@NonNull Toolbar toolbar,
                                                      @NonNull FoldableNavController navController) {
        setupWithFoldableNavController(toolbar, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph()).build());
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On the start destination of your navigation graph, the Toolbar will show
     * the drawer icon if the given Openable layout is non null. On all other destinations,
     * the Toolbar will show the Up button. This method will call
     * {@link #navigateUp(FoldableNavController, Openable)} when the Navigation button is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param openableLayout The Openable layout that should be toggled from the Navigation button
     * @see #setupWithFoldableNavController(Toolbar, FoldableNavController, FoldableAppBarConfiguration)
     */
    public static void setupWithFoldableNavController(@NonNull Toolbar toolbar,
                                                      @NonNull final FoldableNavController navController,
                                                      @Nullable final Openable openableLayout) {
        setupWithFoldableNavController(toolbar, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph())
                        .setOpenableLayout(openableLayout)
                        .build());
    }

    /**
     * Sets up a {@link Toolbar} for use with a {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the Toolbar will automatically be updated when
     * the destination changes (assuming there is a valid {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The {@link FoldableAppBarConfiguration} you provide controls how the Navigation button is
     * displayed and what action is triggered when the Navigation button is tapped. This method
     * will call {@link #navigateUp(FoldableNavController, FoldableAppBarConfiguration)} when the Navigation button
     * is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param configuration Additional configuration options for customizing the behavior of the
     *                      Toolbar
     */
    public static void setupWithFoldableNavController(@NonNull Toolbar toolbar,
                                                      @NonNull final FoldableNavController navController,
                                                      @NonNull final FoldableAppBarConfiguration configuration) {
        navController.addOnDestinationChangedListener(
                new FoldableToolbarOnDestinationChangedListener(toolbar, configuration));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUp(navController, configuration);
            }
        });
    }

    /**
     * Sets up a {@link CollapsingToolbarLayout} and {@link Toolbar} for use with a
     * {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the CollapsingToolbarLayout will automatically be
     * updated when the destination changes (assuming there is a valid
     * {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On all other destinations, the Toolbar will show the Up button. This
     * method will call {@link FoldableNavController#navigateUp()} when the Navigation button
     * is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param collapsingToolbarLayout The CollapsingToolbarLayout that should be kept in sync with
     *                                changes to the FoldableNavController.
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController that supplies the secondary menu. Navigation actions
     *                      on this FoldableNavController will be reflected in the title of the Toolbar.
     */
    public static void setupWithFoldableNavController(
            @NonNull CollapsingToolbarLayout collapsingToolbarLayout,
            @NonNull Toolbar toolbar,
            @NonNull FoldableNavController navController) {
        setupWithFoldableNavController(collapsingToolbarLayout, toolbar, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph()).build());
    }

    /**
     * Sets up a {@link CollapsingToolbarLayout} and {@link Toolbar} for use with a
     * {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the CollapsingToolbarLayout will automatically be
     * updated when the destination changes (assuming there is a valid
     * {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The start destination of your navigation graph is considered the only top level
     * destination. On the start destination of your navigation graph, the Toolbar will show
     * the drawer icon if the given Openable layout is non null. On all other destinations,
     * the Toolbar will show the Up button. This method will call
     * {@link #navigateUp(FoldableNavController, Openable)} when the Navigation button is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param collapsingToolbarLayout The CollapsingToolbarLayout that should be kept in sync with
     *                                changes to the FoldableNavController.
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param openableLayout The Openable layout that should be toggled from the Navigation button
     */
    public static void setupWithFoldableNavController(
            @NonNull CollapsingToolbarLayout collapsingToolbarLayout,
            @NonNull Toolbar toolbar,
            @NonNull final FoldableNavController navController,
            @Nullable final Openable openableLayout) {
        setupWithFoldableNavController(collapsingToolbarLayout, toolbar, navController,
                new FoldableAppBarConfiguration.Builder(navController.getGraph())
                        .setOpenableLayout(openableLayout)
                        .build());
    }

    /**
     * Sets up a {@link CollapsingToolbarLayout} and {@link Toolbar} for use with a
     * {@link FoldableNavController}.
     *
     * <p>By calling this method, the title in the CollapsingToolbarLayout will automatically be
     * updated when the destination changes (assuming there is a valid
     * {@link FoldableNavDestination#getLabel label}).
     *
     * <p>The {@link FoldableAppBarConfiguration} you provide controls how the Navigation button is
     * displayed and what action is triggered when the Navigation button is tapped. This method
     * will call {@link #navigateUp(FoldableNavController, FoldableAppBarConfiguration)} when the Navigation button
     * is clicked.
     *
     * <p>Destinations that implement {@link androidx.navigation.FloatingWindow} will be ignored.
     *
     * @param collapsingToolbarLayout The CollapsingToolbarLayout that should be kept in sync with
     *                                changes to the FoldableNavController.
     * @param toolbar The Toolbar that should be kept in sync with changes to the FoldableNavController.
     * @param navController The FoldableNavController whose navigation actions will be reflected
     *                      in the title of the Toolbar.
     * @param configuration Additional configuration options for customizing the behavior of the
     *                      Toolbar
     */
    public static void setupWithFoldableNavController(
            @NonNull CollapsingToolbarLayout collapsingToolbarLayout,
            @NonNull Toolbar toolbar,
            @NonNull final FoldableNavController navController,
            @NonNull final FoldableAppBarConfiguration configuration) {
        navController.addOnDestinationChangedListener(
                new FoldableCollapsingToolbarOnDestinationChangedListener(
                        collapsingToolbarLayout, toolbar, configuration));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUp(navController, configuration);
            }
        });
    }

    /**
     * Sets up a {@link NavigationView} for use with a {@link FoldableNavController}. This will call
     * {@link #onNavDestinationSelected(MenuItem, FoldableNavController)} when a menu item is selected.
     * The selected item in the NavigationView will automatically be updated when the destination
     * changes.
     * <p>
     * If the {@link NavigationView} is directly contained with an {@link Openable} layout,
     * it will be closed when a menu item is selected.
     * <p>
     * Similarly, if the {@link NavigationView} has a {@link BottomSheetBehavior} associated with
     * it (as is the case when using a {@link com.google.android.material.bottomsheet.BottomSheetDialog}),
     * the bottom sheet will be hidden when a menu item is selected.
     *
     * @param navigationView The NavigationView that should be kept in sync with changes to the
     *                       FoldableNavController.
     * @param navController The FoldableNavController that supplies the primary and secondary menu.
     *                      Navigation actions on this FoldableNavController will be reflected in the
     *                      selected item in the NavigationView.
     */
    public static void setupWithFoldableNavController(@NonNull final NavigationView navigationView,
                                                      @NonNull final FoldableNavController navController) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        boolean handled = onNavDestinationSelected(item, navController);
                        if (handled) {
                            ViewParent parent = navigationView.getParent();
                            if (parent instanceof Openable) {
                                ((Openable) parent).close();
                            } else {
                                BottomSheetBehavior bottomSheetBehavior =
                                        findBottomSheetBehavior(navigationView);
                                if (bottomSheetBehavior != null) {
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                }
                            }
                        }
                        return handled;
                    }
                });
        final WeakReference<NavigationView> weakReference = new WeakReference<>(navigationView);
        navController.addOnDestinationChangedListener(
                new FoldableNavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull FoldableNavController controller,
                                                     @NonNull FoldableNavDestination destination, @Nullable Bundle arguments) {
                        NavigationView view = weakReference.get();
                        if (view == null) {
                            navController.removeOnDestinationChangedListener(this);
                            return;
                        }
                        Menu menu = view.getMenu();
                        for (int h = 0, size = menu.size(); h < size; h++) {
                            MenuItem item = menu.getItem(h);
                            item.setChecked(matchDestination(destination, item.getItemId()));
                        }
                    }
                });
    }

    /**
     * Walks up the view hierarchy, trying to determine if the given View is contained within
     * a bottom sheet.
     */
    @SuppressWarnings("WeakerAccess")
    static BottomSheetBehavior findBottomSheetBehavior(@NonNull View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                return findBottomSheetBehavior((View) parent);
            }
            return null;
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehavior)) {
            // We hit a CoordinatorLayout, but the View doesn't have the BottomSheetBehavior
            return null;
        }
        return (BottomSheetBehavior) behavior;
    }

    /**
     * Sets up a {@link BottomNavigationView} for use with a {@link FoldableNavController}. This will call
     * {@link #onNavDestinationSelected(MenuItem, FoldableNavController)} when a menu item is selected. The
     * selected item in the BottomNavigationView will automatically be updated when the destination
     * changes.
     *
     * @param bottomNavigationView The BottomNavigationView that should be kept in sync with
     *                             changes to the FoldableNavController.
     * @param navController The FoldableNavController that supplies the primary menu.
     *                      Navigation actions on this FoldableNavController will be reflected in the
     *                      selected item in the BottomNavigationView.
     */
    public static void setupWithFoldableNavController(
            @NonNull final BottomNavigationView bottomNavigationView,
            @NonNull final FoldableNavController navController) {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        return onNavDestinationSelected(item, navController);
                    }
                });
        final WeakReference<BottomNavigationView> weakReference =
                new WeakReference<>(bottomNavigationView);
        navController.addOnDestinationChangedListener(
                new FoldableNavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull FoldableNavController controller,
                                                     @NonNull FoldableNavDestination destination, @Nullable Bundle arguments) {
                        BottomNavigationView view = weakReference.get();
                        if (view == null) {
                            navController.removeOnDestinationChangedListener(this);
                            return;
                        }
                        Menu menu = view.getMenu();
                        for (int h = 0, size = menu.size(); h < size; h++) {
                            MenuItem item = menu.getItem(h);
                            if (matchDestination(destination, item.getItemId())) {
                                item.setChecked(true);
                            }
                        }
                    }
                });
    }

    /**
     * Determines whether the given <code>destId</code> matches the FoldableNavDestination. This handles
     * both the default case (the destination's id matches the given id) and the nested case where
     * the given id is a parent/grandparent/etc of the destination.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static boolean matchDestination(@NonNull FoldableNavDestination destination,
            @IdRes int destId) {
        FoldableNavDestination currentDestination = destination;
        while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
            currentDestination = currentDestination.getParent();
        }
        return currentDestination.getId() == destId;
    }

    /**
     * Determines whether the given <code>destinationIds</code> match the FoldableNavDestination. This
     * handles both the default case (the destination's id is in the given ids) and the nested
     * case where the given ids is a parent/grandparent/etc of the destination.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static boolean matchDestinations(@NonNull FoldableNavDestination destination,
            @NonNull Set<Integer> destinationIds) {
        FoldableNavDestination currentDestination = destination;
        do {
            if (destinationIds.contains(currentDestination.getId())) {
                return true;
            }
            currentDestination = currentDestination.getParent();
        } while (currentDestination != null);
        return false;
    }

    /**
     * Finds the actual start destination of the graph, handling cases where the graph's starting
     * destination is itself a FoldableNavGraph.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static FoldableNavDestination findStartDestination(@NonNull FoldableNavGraph graph) {
        FoldableNavDestination startDestination = graph;
        while (startDestination instanceof FoldableNavGraph) {
            FoldableNavGraph parent = (FoldableNavGraph) startDestination;
            startDestination = parent.findNode(parent.getStartDestination());
        }
        return startDestination;
    }
}
