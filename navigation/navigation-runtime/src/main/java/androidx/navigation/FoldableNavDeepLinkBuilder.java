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
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.TaskStackBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Class used to construct deep links to a particular destination in a {@link FoldableNavGraph}.
 *
 * <p>When this deep link is triggered:
 * <ol>
 *     <li>The task is cleared.</li>
 *     <li>The destination and all of its parents will be on the back stack.</li>
 *     <li>Calling {@link FoldableNavController#navigateUp()} will navigate to the parent of the
 *     destination.</li>
 * </ol></p>
 * <p>
 * The parent of the destination is the {@link FoldableNavGraph#getStartDestination() start destination}
 * of the containing {@link FoldableNavGraph navigation graph}. In the cases where the destination is
 * the start destination of its containing navigation graph, the start destination of its
 * grandparent is used.
 * <p>
 * You can construct an instance directly with {@link #FoldableNavDeepLinkBuilder(Context)} or build one
 * using an existing {@link FoldableNavController} via {@link FoldableNavController#createDeepLink()}.
 */
public final class FoldableNavDeepLinkBuilder {
    private static class DeepLinkDestination {
        private final int mDestinationId;
        @Nullable
        private final Bundle mArguments;

        DeepLinkDestination(int destinationId, @Nullable Bundle arguments) {
            mDestinationId = destinationId;
            mArguments = arguments;
        }

        public int getDestinationId() {
            return mDestinationId;
        }

        @Nullable
        public Bundle getArguments() {
            return mArguments;
        }
    }

    private final Context mContext;
    private final Intent mIntent;
    private FoldableNavGraph mGraph;
    private ArrayList<DeepLinkDestination> mDestinations = new ArrayList<>();
    private Bundle mGlobalArgs;

    /**
     * Construct a new FoldableNavDeepLinkBuilder.
     * <p>
     * If the context passed in here is not an {@link Activity}, this method will use
     * {@link android.content.pm.PackageManager#getLaunchIntentForPackage(String)} as the
     * default activity to launch, if available.
     *
     * @param context Context used to create deep links
     * @see #setComponentName
     */
    public FoldableNavDeepLinkBuilder(@NonNull Context context) {
        mContext = context;
        if (mContext instanceof Activity) {
            mIntent = new Intent(mContext, mContext.getClass());
        } else {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(
                    mContext.getPackageName());
            mIntent = launchIntent != null ? launchIntent : new Intent();
        }
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    /**
     * @see FoldableNavController#createDeepLink()
     */
    FoldableNavDeepLinkBuilder(@NonNull FoldableNavController navController) {
        this(navController.getContext());
        mGraph = navController.getGraph();
    }

    /**
     * Sets an explicit Activity to be started by the deep link created by this class.
     *
     * @param activityClass The Activity to start. This Activity should have a {@link FoldableNavController}
     *                      which uses the same {@link FoldableNavGraph} used to construct this
     *                      deep link.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setComponentName(@NonNull Class<? extends Activity> activityClass) {
        return setComponentName(new ComponentName(mContext, activityClass));
    }

    /**
     * Sets an explicit Activity to be started by the deep link created by this class.
     *
     * @param componentName The Activity to start. This Activity should have a {@link FoldableNavController}
     *                      which uses the same {@link FoldableNavGraph} used to construct this
     *                      deep link.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setComponentName(@NonNull ComponentName componentName) {
        mIntent.setComponent(componentName);
        return this;
    }

    /**
     * Sets the graph that contains the {@link #setDestination(int) deep link destination}.
     *
     * @param navGraphId ID of the {@link FoldableNavGraph} containing the deep link destination
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setGraph(@NavigationRes int navGraphId) {
        return setGraph(new FoldableNavInflater(mContext, new PermissiveNavigatorProvider())
                .inflate(navGraphId));
    }

    /**
     * Sets the graph that contains the {@link #setDestination(int) deep link destination}.
     * <p>
     * If you do not have access to a {@link FoldableNavController}, you can create a
     * {@link FoldableNavigatorProvider} and use that to programmatically construct a navigation
     * graph or use {@link FoldableNavInflater#FoldableNavInflater(Context, FoldableNavigatorProvider) NavInflater}.
     *
     * @param navGraph The {@link FoldableNavGraph} containing the deep link destination
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setGraph(@NonNull FoldableNavGraph navGraph) {
        mGraph = navGraph;
        verifyAllDestinations();
        return this;
    }

    /**
     * Sets the destination id to deep link to. Any destinations previous added via
     * {@link #addDestination(int)} are cleared, effectively resetting this object
     * back to only this single destination.
     *
     * @param destId destination ID to deep link to.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setDestination(@IdRes int destId) {
        return setDestination(destId, null);
    }

    /**
     * Sets the destination id to deep link to. Any destinations previous added via
     * {@link #addDestination(int)} are cleared, effectively resetting this object
     * back to only this single destination.
     *
     * @param destId destination ID to deep link to.
     * @param args   Arguments to pass to this destination and any synthetic back stack created
     *               due to this destination being added.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setDestination(@IdRes int destId, @Nullable Bundle args) {
        mDestinations.clear();
        mDestinations.add(new DeepLinkDestination(destId, args));
        if (mGraph != null) {
            verifyAllDestinations();
        }
        return this;
    }

    /**
     * Add a new destination id to deep link to. This builds off any previous calls to this method
     * or calls to {@link #setDestination(int)}, building the minimal synthetic back stack of
     * start destinations between the previous deep link destination and the newly added
     * deep link destination.
     *
     * @param destId destination ID to deep link to.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder addDestination(@IdRes int destId) {
        return addDestination(destId, null);
    }

    /**
     * Add a new destination id to deep link to. This builds off any previous calls to this method
     * or calls to {@link #setDestination(int)}, building the minimal synthetic back stack of
     * start destinations between the previous deep link destination and the newly added
     * deep link destination.
     *
     * @param destId destination ID to deep link to.
     * @param args   Arguments to pass to this destination and any synthetic back stack created
     *               due to this destination being added.
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder addDestination(@IdRes int destId, @Nullable Bundle args) {
        mDestinations.add(new DeepLinkDestination(destId, args));
        if (mGraph != null) {
            verifyAllDestinations();
        }
        return this;
    }

    @Nullable
    private FoldableNavDestination findDestination(@IdRes int destId) {
        ArrayDeque<FoldableNavDestination> possibleDestinations = new ArrayDeque<>();
        possibleDestinations.add(mGraph);
        while (!possibleDestinations.isEmpty()) {
            FoldableNavDestination destination = possibleDestinations.poll();
            if (destination.getId() == destId) {
                return destination;
            } else if (destination instanceof FoldableNavGraph) {
                for (FoldableNavDestination child : (FoldableNavGraph) destination) {
                    possibleDestinations.add(child);
                }
            }
        }
        return null;
    }

    private void verifyAllDestinations() {
        for (DeepLinkDestination destination : mDestinations) {
            int destId = destination.getDestinationId();
            FoldableNavDestination node = findDestination(destId);
            if (node == null) {
                final String dest = FoldableNavDestination.getDisplayName(mContext, destId);
                throw new IllegalArgumentException("Navigation destination " + dest
                        + " cannot be found in the navigation graph " + mGraph);
            }
        }
    }

    private void fillInIntent() {
        ArrayList<Integer> deepLinkIds = new ArrayList<>();
        ArrayList<Bundle> deepLinkArgs = new ArrayList<>();
        FoldableNavDestination previousDestination = null;
        for (DeepLinkDestination destination : mDestinations) {
            int destId = destination.getDestinationId();
            Bundle arguments = destination.getArguments();
            FoldableNavDestination node = findDestination(destId);
            if (node == null) {
                final String dest = FoldableNavDestination.getDisplayName(mContext, destId);
                throw new IllegalArgumentException("Navigation destination " + dest
                        + " cannot be found in the navigation graph " + mGraph);
            }
            for (int id : node.buildDeepLinkIds(previousDestination)) {
                deepLinkIds.add(id);
                deepLinkArgs.add(arguments);
            }
            previousDestination = node;
        }
        int[] idArray = new int[deepLinkIds.size()];
        int index = 0;
        for (Integer id : deepLinkIds) {
            idArray[index++] = id;
        }
        mIntent.putExtra(FoldableNavController.KEY_DEEP_LINK_IDS, idArray);
        mIntent.putParcelableArrayListExtra(FoldableNavController.KEY_DEEP_LINK_ARGS, deepLinkArgs);
    }

    /**
     * Set optional arguments to send onto every destination created by this deep link.
     *
     * @param args arguments to pass to each destination
     * @return this object for chaining
     */
    @NonNull
    public FoldableNavDeepLinkBuilder setArguments(@Nullable Bundle args) {
        mGlobalArgs = args;
        mIntent.putExtra(FoldableNavController.KEY_DEEP_LINK_EXTRAS, args);
        return this;
    }

    /**
     * Construct the full {@link TaskStackBuilder task stack} needed to deep link to the given
     * destination.
     * <p>
     * You must have {@link #setGraph set a FoldableNavGraph} and {@link #setDestination set a destination}
     * before calling this method.
     * </p>
     *
     * @return a {@link TaskStackBuilder} which can be used to
     * {@link TaskStackBuilder#startActivities() send the deep link} or
     * {@link TaskStackBuilder#getPendingIntent(int, int) create a PendingIntent} to deep link to
     * the given destination.
     */
    @NonNull
    public TaskStackBuilder createTaskStackBuilder() {
        if (mGraph == null) {
            throw new IllegalStateException("You must call setGraph() "
                    + "before constructing the deep link");
        } else if (mDestinations.isEmpty()) {
            throw new IllegalStateException("You must call setDestination() or addDestination() "
                    + "before constructing the deep link");
        }
        fillInIntent();
        // We create a copy of the Intent to ensure the Intent does not have itself
        // as an extra. This also prevents developers from modifying the internal Intent
        // via taskStackBuilder.editIntentAt()
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext)
                .addNextIntentWithParentStack(new Intent(mIntent));
        for (int index = 0; index < taskStackBuilder.getIntentCount(); index++) {
            // Attach the original Intent to each Activity so that they can know
            // they were constructed in response to a deep link
            taskStackBuilder.editIntentAt(index)
                    .putExtra(FoldableNavController.KEY_DEEP_LINK_INTENT, mIntent);
        }
        return taskStackBuilder;
    }

    /**
     * Construct a {@link PendingIntent} to the {@link #setDestination(int) deep link destination}.
     * <p>
     * This constructs the entire {@link #createTaskStackBuilder() task stack} needed.
     * <p>
     * You must have {@link #setGraph set a FoldableNavGraph} and {@link #setDestination set a destination}
     * before calling this method.
     * </p>
     *
     * @return a PendingIntent constructed with
     * {@link TaskStackBuilder#getPendingIntent(int, int)} to deep link to the
     * given destination
     */
    @NonNull
    public PendingIntent createPendingIntent() {
        int requestCode = 0;
        if (mGlobalArgs != null) {
            for (String key : mGlobalArgs.keySet()) {
                Object value = mGlobalArgs.get(key);
                requestCode = 31 * requestCode + (value != null ? value.hashCode() : 0);
            }
        }
        for (DeepLinkDestination destination : mDestinations) {
            int destId = destination.getDestinationId();
            requestCode = 31 * requestCode + destId;
            Bundle arguments = destination.getArguments();
            if (arguments != null) {
                for (String key : arguments.keySet()) {
                    Object value = arguments.get(key);
                    requestCode = 31 * requestCode + (value != null ? value.hashCode() : 0);
                }
            }
        }
        return createTaskStackBuilder()
                .getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * A {@link FoldableNavigatorProvider} that only parses the basics: {@link FoldableNavGraph navigation graphs}
     * and {@link FoldableNavDestination destinations}, effectively only getting the base destination
     * information.
     */
    @SuppressWarnings("unchecked")
    private static class PermissiveNavigatorProvider extends FoldableNavigatorProvider {
        /**
         * A Navigator that only parses the {@link FoldableNavDestination} attributes.
         */
        private final FoldableNavigator<FoldableNavDestination> mDestNavigator = new FoldableNavigator<FoldableNavDestination>() {
            @NonNull
            @Override
            public FoldableNavDestination createDestination() {
                return new FoldableNavDestination("permissive");
            }

            @Nullable
            @Override
            public FoldableNavDestination navigate(@NonNull FoldableNavDestination destination,
                                                   @Nullable Bundle args, @Nullable FoldableNavOptions navOptions,
                                                   @Nullable Extras navigatorExtras) {
                throw new IllegalStateException("navigate is not supported");
            }

            @Override
            public boolean popBackStack(boolean withTransition) {
                throw new IllegalStateException("popBackStack is not supported");
            }
        };

        PermissiveNavigatorProvider() {
            addNavigator(new FoldableNavGraphNavigator(this));
        }

        @NonNull
        @Override
        public FoldableNavigator<? extends FoldableNavDestination> getNavigator(@NonNull String name) {
            try {
                return super.getNavigator(name);
            } catch (IllegalStateException e) {
                return mDestNavigator;
            }
        }
    }
}