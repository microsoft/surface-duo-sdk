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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.collection.SparseArrayCompat;
import androidx.navigation.common.R;

import com.microsoft.device.dualscreen.navigation.LaunchScreen;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * FoldableNavDestination represents one node within an overall navigation graph.
 *
 * <p>Each destination is associated with a {@link FoldableNavigator} which knows how to navigate to this
 * particular destination.</p>
 *
 * <p>Destinations declare a set of {@link #putAction(int, int) actions} that they
 * support. These actions form a navigation API for the destination; the same actions declared
 * on different destinations that fill similar roles allow application code to navigate based
 * on semantic intent.</p>
 *
 * <p>Each destination has a set of {@link #getArguments() arguments} that will
 * be applied when {@link FoldableNavController#navigate(int, Bundle) navigating} to that destination.
 * Any default values for those arguments can be overridden at the time of navigation.</p>
 */
public class FoldableNavDestination {
    /**
     * This optional annotation allows tooling to offer auto-complete for the
     * <code>android:name</code> attribute. This should match the class type passed to
     * {@link #parseClassFromName(Context, String, Class)} when parsing the
     * <code>android:name</code> attribute.
     */
    @Retention(CLASS)
    @Target({TYPE})
    @SuppressWarnings("UnknownNullness") // TODO https://issuetracker.google.com/issues/112185120
    public @interface ClassType {
        Class<?> value();
    }

    static class DeepLinkMatch implements Comparable<DeepLinkMatch> {
        @NonNull
        private final FoldableNavDestination mDestination;
        @Nullable
        private final Bundle mMatchingArgs;
        private final boolean mIsExactDeepLink;
        private final boolean mHasMatchingAction;
        private final int mMimeTypeMatchLevel;

        DeepLinkMatch(@NonNull FoldableNavDestination destination, @Nullable Bundle matchingArgs,
                      boolean isExactDeepLink, boolean hasMatchingAction, int mimeTypeMatchLevel) {
            mDestination = destination;
            mMatchingArgs = matchingArgs;
            mIsExactDeepLink = isExactDeepLink;
            mHasMatchingAction = hasMatchingAction;
            mMimeTypeMatchLevel = mimeTypeMatchLevel;
        }

        @NonNull
        FoldableNavDestination getDestination() {
            return mDestination;
        }

        @Nullable
        Bundle getMatchingArgs() {
            return mMatchingArgs;
        }

        @Override
        public int compareTo(@NonNull DeepLinkMatch other) {
            // Prefer exact deep links
            if (mIsExactDeepLink && !other.mIsExactDeepLink) {
                return 1;
            } else if (!mIsExactDeepLink && other.mIsExactDeepLink) {
                return -1;
            }
            if (mMatchingArgs != null && other.mMatchingArgs == null) {
                return 1;
            } else if (mMatchingArgs == null && other.mMatchingArgs != null) {
                return -1;
            }
            if (mMatchingArgs != null) {
                int sizeDifference = mMatchingArgs.size() - other.mMatchingArgs.size();
                if (sizeDifference > 0) {
                    return 1;
                } else if (sizeDifference < 0) {
                    return -1;
                }
            }
            if (mHasMatchingAction && !other.mHasMatchingAction) {
                return 1;
            } else if (!mHasMatchingAction && other.mHasMatchingAction) {
                return -1;
            }
            return mMimeTypeMatchLevel - other.mMimeTypeMatchLevel;
        }
    }

    private static final HashMap<String, Class<?>> sClasses = new HashMap<>();

    /**
     * Parse the class associated with this destination from a raw name, generally extracted
     * from the <code>android:name</code> attribute added to the destination's XML. This should
     * be the class providing the visual representation of the destination that the
     * user sees after navigating to this destination.
     * <p>
     * This method does name -> Class caching and should be strongly preferred over doing your
     * own parsing if your {@link Navigator} supports the <code>android:name</code> attribute to
     * give consistent behavior across all Navigators.
     *
     * @param context           Context providing the package name for use with relative class names and the
     *                          ClassLoader
     * @param name              Absolute or relative class name. Null names will be ignored.
     * @param expectedClassType The expected class type
     * @return The parsed class
     * @throws IllegalArgumentException if the class is not found in the provided Context's
     *                                  ClassLoader or if the class is not of the expected type
     */
    @SuppressWarnings("unchecked")
    @NonNull
    protected static <C> Class<? extends C> parseClassFromName(@NonNull Context context,
                                                               @NonNull String name,
                                                               @NonNull Class<? extends C> expectedClassType) {
        if (name.charAt(0) == '.') {
            name = context.getPackageName() + name;
        }
        Class<?> clazz = sClasses.get(name);
        if (clazz == null) {
            try {
                clazz = Class.forName(name, true, context.getClassLoader());
                sClasses.put(name, clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (!expectedClassType.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(name + " must be a subclass of "
                    + expectedClassType);
        }
        return (Class<? extends C>) clazz;
    }

    /**
     * Retrieve a suitable display name for a given id.
     *
     * @param context Context used to resolve a resource's name
     * @param id      The id to get a display name for
     * @return The resource's name if it is a valid id or just the id itself if it is not
     * a valid resource
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @NonNull
    static String getDisplayName(@NonNull Context context, int id) {
        // aapt-generated IDs have the high byte nonzero,
        // so anything below that cannot be a valid resource id
        if (id <= 0x00FFFFFF) {
            return Integer.toString(id);
        }
        try {
            return context.getResources().getResourceName(id);
        } catch (Resources.NotFoundException e) {
            return Integer.toString(id);
        }
    }

    private final String mNavigatorName;
    private FoldableNavGraph mParent;
    private int mId;
    private String mIdName;
    private CharSequence mLabel;
    private ArrayList<NavDeepLink> mDeepLinks;
    private SparseArrayCompat<FoldableNavAction> mActions;
    private HashMap<String, NavArgument> mArguments;
    private LaunchScreen mLaunchScreen = LaunchScreen.DEFAULT;

    /**
     * Get the arguments supported by this destination. Returns a read-only map of argument names
     * to {@link NavArgument} objects that can be used to check the type, default value
     * and nullability of the argument.
     * <p>
     * To add and remove arguments for this FoldableNavDestination
     * use {@link #addArgument(String, NavArgument)} and {@link #removeArgument(String)}.
     *
     * @return Read-only map of argument names to arguments.
     */
    @NonNull
    public final Map<String, NavArgument> getArguments() {
        return mArguments == null ? Collections.<String, NavArgument>emptyMap()
                : Collections.unmodifiableMap(mArguments);
    }

    /**
     * NavDestinations should be created via {@link FoldableNavigator#createDestination}.
     * <p>
     * This constructor requires that the given Navigator has a {@link FoldableNavigator.Name} annotation.
     */
    public FoldableNavDestination(@NonNull FoldableNavigator<? extends FoldableNavDestination> navigator) {
        this(FoldableNavigatorProvider.getNameForNavigator(navigator.getClass()));
    }

    /**
     * NavDestinations should be created via {@link Navigator#createDestination}.
     */
    public FoldableNavDestination(@NonNull String navigatorName) {
        mNavigatorName = navigatorName;
    }

    /**
     * Called when inflating a destination from a resource.
     *
     * @param context local context performing inflation
     * @param attrs   attrs to parse during inflation
     */
    @CallSuper
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs) {
        final TypedArray a = context.getResources().obtainAttributes(attrs,
                R.styleable.Navigator);
        setId(a.getResourceId(R.styleable.Navigator_android_id, 0));
        mIdName = getDisplayName(context, mId);
        setLabel(a.getText(R.styleable.Navigator_android_label));
        a.recycle();
    }

    final void setParent(FoldableNavGraph parent) {
        mParent = parent;
    }

    /**
     * Gets the {@link FoldableNavGraph} that contains this destination. This will be set when a
     * destination is added to a NavGraph via {@link FoldableNavGraph#addDestination}.
     *
     * @return
     */
    @Nullable
    public final FoldableNavGraph getParent() {
        return mParent;
    }

    /**
     * Returns the destination's unique ID. This should be an ID resource generated by
     * the Android resource system.
     *
     * @return this destination's ID
     */
    @IdRes
    public final int getId() {
        return mId;
    }

    /**
     * Sets the destination's unique ID. This should be an ID resource generated by
     * the Android resource system.
     *
     * @param id this destination's new ID
     */
    public final void setId(@IdRes int id) {
        mId = id;
        mIdName = null;
    }

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @NonNull
    public String getDisplayName() {
        if (mIdName == null) {
            mIdName = Integer.toString(mId);
        }
        return mIdName;
    }

    /**
     * Sets the descriptive label of this destination.
     *
     * @param label A descriptive label of this destination.
     */
    public final void setLabel(@Nullable CharSequence label) {
        mLabel = label;
    }

    /**
     * Gets the descriptive label of this destination.
     */
    @Nullable
    public final CharSequence getLabel() {
        return mLabel;
    }

    public void setLaunchScreen(LaunchScreen launchScreen) {
        this.mLaunchScreen = launchScreen;
    }

    public LaunchScreen getLaunchScreen() {
        return mLaunchScreen;
    }

    /**
     * Returns the name associated with this destination's {@link Navigator}.
     *
     * @return the name associated with this destination's navigator
     */
    @NonNull
    public final String getNavigatorName() {
        return mNavigatorName;
    }

    /**
     * Checks the given deep link {@link Uri}, and determines whether it matches a Uri pattern added
     * to the destination by a call to {@link #addDeepLink(String)} . It returns <code>true</code>
     * if the deep link is a valid match, and <code>false</code> otherwise.
     *
     * <p>
     * This should be called prior to {@link FoldableNavController#navigate(Uri)} to ensure the deep link
     * can be navigated to.
     * </p>
     *
     * @param deepLink to the destination reachable from the current NavGraph
     * @return True if the deepLink exists for the destination.
     * @see #addDeepLink(String)
     * @see FoldableNavController#navigate(Uri)
     * @see #hasDeepLink(FoldableNavDeepLinkRequest)
     */
    public boolean hasDeepLink(@NonNull Uri deepLink) {
        return hasDeepLink(new FoldableNavDeepLinkRequest(deepLink, null, null));
    }

    /**
     * Checks the given {@link NavDeepLinkRequest}, and determines whether it matches a
     * {@link NavDeepLink} added to the destination by a call to
     * {@link #addDeepLink(NavDeepLink)}. It returns <code>true</code> if the request is a valid
     * match, and <code>false</code> otherwise.
     *
     * <p>
     * This should be called prior to {@link FoldableNavController#navigate(FoldableNavDeepLinkRequest)} to
     * ensure the deep link can be navigated to.
     * </p>
     *
     * @param deepLinkRequest to the destination reachable from the current NavGraph
     * @return True if the deepLink exists for the destination.
     * @see #addDeepLink(NavDeepLink)
     * @see FoldableNavController#navigate(NavDeepLinkRequest)
     */
    public boolean hasDeepLink(@NonNull FoldableNavDeepLinkRequest deepLinkRequest) {
        return matchDeepLink(deepLinkRequest) != null;
    }

    /**
     * Add a deep link to this destination. Matching Uris sent to
     * {@link FoldableNavController#handleDeepLink(Intent)} or {@link FoldableNavController#navigate(Uri)} will
     * trigger navigating to this destination.
     * <p>
     * In addition to a direct Uri match, the following features are supported:
     * <ul>
     *     <li>Uris without a scheme are assumed as http and https. For example,
     *     <code>www.example.com</code> will match <code>http://www.example.com</code> and
     *     <code>https://www.example.com</code>.</li>
     *     <li>Placeholders in the form of <code>{placeholder_name}</code> matches 1 or more
     *     characters. The String value of the placeholder will be available in the arguments
     *     {@link Bundle} with a key of the same name. For example,
     *     <code>http://www.example.com/users/{id}</code> will match
     *     <code>http://www.example.com/users/4</code>.</li>
     *     <li>The <code>.*</code> wildcard can be used to match 0 or more characters.</li>
     * </ul>
     * These Uris can be declared in your navigation XML files by adding one or more
     * <code>&lt;deepLink app:uri="uriPattern" /&gt;</code> elements as
     * a child to your destination.
     * <p>
     * Deep links added in navigation XML files will automatically replace instances of
     * <code>${applicationId}</code> with the applicationId of your app.
     * Programmatically added deep links should use {@link Context#getPackageName()} directly
     * when constructing the uriPattern.
     *
     * @param uriPattern The uri pattern to add as a deep link
     * @see FoldableNavController#handleDeepLink(Intent)
     * @see FoldableNavController#navigate(Uri)
     * @see #addDeepLink(NavDeepLink)
     */
    public final void addDeepLink(@NonNull String uriPattern) {
        addDeepLink(new NavDeepLink.Builder().setUriPattern(uriPattern).build());
    }

    /**
     * Add a deep link to this destination. Uris that match the given {@link NavDeepLink} uri
     * sent to {@link FoldableNavController#handleDeepLink(Intent)} or
     * {@link FoldableNavController#navigate(FoldableNavDeepLinkRequest)} will trigger navigating to this
     * destination.
     * <p>
     * In addition to a direct Uri match, the following features are supported:
     * <ul>
     *     <li>Uris without a scheme are assumed as http and https. For example,
     *     <code>www.example.com</code> will match <code>http://www.example.com</code> and
     *     <code>https://www.example.com</code>.</li>
     *     <li>Placeholders in the form of <code>{placeholder_name}</code> matches 1 or more
     *     characters. The String value of the placeholder will be available in the arguments
     *     {@link Bundle} with a key of the same name. For example,
     *     <code>http://www.example.com/users/{id}</code> will match
     *     <code>http://www.example.com/users/4</code>.</li>
     *     <li>The <code>.*</code> wildcard can be used to match 0 or more characters.</li>
     * </ul>
     * These Uris can be declared in your navigation XML files by adding one or more
     * <code>&lt;deepLink app:uri="uriPattern" /&gt;</code> elements as
     * a child to your destination.
     * <p>
     * Custom actions and mimetypes are also supported by {@link NavDeepLink} and can be declared
     * in your navigation XML files by adding
     * <code>&lt;app:action="android.intent.action.SOME_ACTION" /&gt;</code> or
     * <code>&lt;app:mimetype="type/subtype" /&gt;</code> as part of your deepLink declaration.
     * <p>
     * Deep link Uris, actions, and mimetypes added in navigation XML files will automatically
     * replace instances of <code>${applicationId}</code> with the applicationId of your app.
     * Programmatically added deep links should use {@link Context#getPackageName()} directly
     * when constructing the uriPattern.
     * <p>
     * When matching deep links for calls to {@link FoldableNavController#handleDeepLink(Intent)} or
     * {@link FoldableNavController#navigate(NavDeepLinkRequest)} the order of precedence is as follows:
     * the deep link with the most matching arguments will be chosen, followed by the deep link
     * with a matching action, followed by the best matching mimeType (e.i. when matching
     * mimeType image/jpg: image/* > *\/jpg > *\/*).
     *
     * @param navDeepLink The NavDeepLink to add as a deep link
     * @see FoldableNavController#handleDeepLink(Intent)
     * @see FoldableNavController#navigate(NavDeepLinkRequest)
     */
    public final void addDeepLink(@NonNull NavDeepLink navDeepLink) {
        if (mDeepLinks == null) {
            mDeepLinks = new ArrayList<>();
        }
        mDeepLinks.add(navDeepLink);
    }

    /**
     * Determines if this FoldableNavDestination has a deep link matching the given Uri.
     *
     * @param navDeepLinkRequest The request to match against all deep links added in
     *                           {@link #addDeepLink(NavDeepLink)}
     * @return The matching {@link FoldableNavDestination} and the appropriate {@link Bundle} of arguments
     * extracted from the Uri, or null if no match was found.
     */
    @Nullable
    DeepLinkMatch matchDeepLink(@NonNull FoldableNavDeepLinkRequest navDeepLinkRequest) {
        if (mDeepLinks == null) {
            return null;
        }
        DeepLinkMatch bestMatch = null;
        for (NavDeepLink deepLink : mDeepLinks) {
            Uri uri = navDeepLinkRequest.getUri();
            Bundle matchingArguments = uri != null
                    ? deepLink.getMatchingArguments(uri, getArguments()) : null;
            String requestAction = navDeepLinkRequest.getAction();
            boolean matchingAction = requestAction != null && requestAction.equals(
                    deepLink.getAction());
            String mimeType = navDeepLinkRequest.getMimeType();
            int mimeTypeMatchLevel = mimeType != null
                    ? deepLink.getMimeTypeMatchRating(mimeType) : -1;
            if (matchingArguments != null || matchingAction || mimeTypeMatchLevel > -1) {
                DeepLinkMatch newMatch = new DeepLinkMatch(this, matchingArguments,
                        deepLink.isExactDeepLink(), matchingAction, mimeTypeMatchLevel);
                if (bestMatch == null || newMatch.compareTo(bestMatch) > 0) {
                    bestMatch = newMatch;
                }
            }
        }
        return bestMatch;
    }

    /**
     * Build an array containing the hierarchy from the root down to this destination.
     *
     * @return An array containing all of the ids from the root to this destination
     */
    @NonNull
    int[] buildDeepLinkIds() {
        return buildDeepLinkIds(null);
    }

    /**
     * Build an array containing the hierarchy from the root down to this destination.
     *
     * @param previousDestination the previous destination we are starting at
     * @return An array containing all of the ids from the previous destination (or the root of
     * the graph if null) to this destination
     */
    @NonNull
    int[] buildDeepLinkIds(@Nullable FoldableNavDestination previousDestination) {
        ArrayDeque<FoldableNavDestination> hierarchy = new ArrayDeque<>();
        FoldableNavDestination current = this;
        do {
            FoldableNavGraph parent = current.getParent();
            if (previousDestination != null && previousDestination.getParent() != null
                    && previousDestination.getParent().findNode(current.getId()) == current) {
                hierarchy.addFirst(current);
                break;
            }
            if (parent == null || parent.getStartDestination() != current.getId()) {
                hierarchy.addFirst(current);
            }
            current = parent;
        } while (current != null);
        int[] deepLinkIds = new int[hierarchy.size()];
        int index = 0;
        for (FoldableNavDestination destination : hierarchy) {
            deepLinkIds[index++] = destination.getId();
        }
        return deepLinkIds;
    }

    /**
     * @return Whether this FoldableNavDestination supports outgoing actions
     * @see #putAction(int, NavAction)
     */
    boolean supportsActions() {
        return true;
    }

    /**
     * Returns the destination ID for a given action. This will recursively check the
     * {@link #getParent() parent} of this destination if the action destination is not found in
     * this destination.
     *
     * @param id action ID to fetch
     * @return destination ID mapped to the given action id, or 0 if none
     */
    @Nullable
    public final FoldableNavAction getAction(@IdRes int id) {
        FoldableNavAction destination = mActions == null ? null : mActions.get(id);
        // Search the parent for the given action if it is not found in this destination
        return destination != null
                ? destination
                : getParent() != null ? getParent().getAction(id) : null;
    }

    /**
     * Sets a destination ID for an action ID.
     *
     * @param actionId action ID to bind
     * @param destId   destination ID for the given action
     */
    public final void putAction(@IdRes int actionId, @IdRes int destId) {
        putAction(actionId, new FoldableNavAction(destId));
    }

    /**
     * Sets a destination ID for an action ID.
     *
     * @param actionId action ID to bind
     * @param action   action to associate with this action ID
     */
    public final void putAction(@IdRes int actionId, @NonNull FoldableNavAction action) {
        if (!supportsActions()) {
            throw new UnsupportedOperationException("Cannot add action " + actionId + " to "
                    + this + " as it does not support actions, indicating that it is a "
                    + "terminal destination in your navigation graph and will never trigger "
                    + "actions.");
        }
        if (actionId == 0) {
            throw new IllegalArgumentException("Cannot have an action with actionId 0");
        }
        if (mActions == null) {
            mActions = new SparseArrayCompat<>();
        }
        mActions.put(actionId, action);
    }

    /**
     * Unsets the destination ID for an action ID.
     *
     * @param actionId action ID to remove
     */
    public final void removeAction(@IdRes int actionId) {
        if (mActions == null) {
            return;
        }
        mActions.remove(actionId);
    }

    /**
     * Sets an argument type for an argument name
     *
     * @param argumentName argument object to associate with destination
     */
    public final void addArgument(@NonNull String argumentName, @NonNull NavArgument argument) {
        if (mArguments == null) {
            mArguments = new HashMap<>();
        }
        mArguments.put(argumentName, argument);
    }

    /**
     * Unsets the argument type for an argument name.
     *
     * @param argumentName argument to remove
     */
    public final void removeArgument(@NonNull String argumentName) {
        if (mArguments == null) {
            return;
        }
        mArguments.remove(argumentName);
    }

    /**
     * Combines the default arguments for this destination with the arguments provided
     * to construct the final set of arguments that should be used to navigate
     * to this destination.
     */
    @Nullable
    Bundle addInDefaultArgs(@Nullable Bundle args) {
        if (args == null && (mArguments == null || mArguments.isEmpty())) {
            return null;
        }
        Bundle defaultArgs = new Bundle();
        if (mArguments != null) {
            for (Map.Entry<String, NavArgument> argument : mArguments.entrySet()) {
                argument.getValue().putDefaultValue(argument.getKey(), defaultArgs);
            }
        }
        if (args != null) {
            defaultArgs.putAll(args);
            if (mArguments != null) {
                for (Map.Entry<String, NavArgument> argument : mArguments.entrySet()) {
                    if (!argument.getValue().verify(argument.getKey(), defaultArgs)) {
                        throw new IllegalArgumentException(
                                "Wrong argument type for '" + argument.getKey()
                                        + "' in argument bundle. "
                                        + argument.getValue().getType().getName() + " expected.");
                    }
                }
            }
        }
        return defaultArgs;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("(");
        if (mIdName == null) {
            sb.append("0x");
            sb.append(Integer.toHexString(mId));
        } else {
            sb.append(mIdName);
        }
        sb.append(")");
        if (mLabel != null) {
            sb.append(" label=");
            sb.append(mLabel);
        }
        return sb.toString();
    }
}