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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ActivityNavigator implements cross-activity navigation.
 */
@FoldableNavigator.Name("activity")
public class FoldableActivityNavigator extends FoldableNavigator<FoldableActivityNavigator.Destination> {
    private static final String EXTRA_NAV_SOURCE =
            "android-support-navigation:FoldableActivityNavigator:source";
    private static final String EXTRA_NAV_CURRENT =
            "android-support-navigation:FoldableActivityNavigator:current";
    private static final String EXTRA_POP_ENTER_ANIM =
            "android-support-navigation:FoldableActivityNavigator:popEnterAnim";
    private static final String EXTRA_POP_EXIT_ANIM =
            "android-support-navigation:FoldableActivityNavigator:popExitAnim";

    private static final String LOG_TAG = "ActivityNavigator";

    private Context mContext;
    private Activity mHostActivity;

    public FoldableActivityNavigator(@NonNull Context context) {
        mContext = context;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                mHostActivity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
    }

    /**
     * Apply any pop animations in the Intent of the given Activity to a pending transition.
     * This should be used in place of {@link Activity#overridePendingTransition(int, int)}
     * to get the appropriate pop animations.
     * @param activity An activity started from the {@link FoldableActivityNavigator}.
     * @see NavOptions#getPopEnterAnim()
     * @see NavOptions#getPopExitAnim()
     */
    public static void applyPopAnimationsToPendingTransition(@NonNull Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        int popEnterAnim = intent.getIntExtra(EXTRA_POP_ENTER_ANIM, -1);
        int popExitAnim = intent.getIntExtra(EXTRA_POP_EXIT_ANIM, -1);
        if (popEnterAnim != -1 || popExitAnim != -1) {
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            activity.overridePendingTransition(popEnterAnim, popExitAnim);
        }
    }

    @NonNull
    final Context getContext() {
        return mContext;
    }

    @NonNull
    @Override
    public Destination createDestination() {
        return new Destination(this);
    }

    @Override
    public boolean popBackStack(boolean withTransition) {
        if (mHostActivity != null) {
            mHostActivity.finish();
            return true;
        }
        return false;
    }

    @SuppressLint("WrongConstant")
    @Nullable
    @Override
    public FoldableNavDestination navigate(@NonNull Destination destination, @Nullable Bundle args,
                                           @Nullable FoldableNavOptions navOptions, @Nullable FoldableNavigator.Extras navigatorExtras) {
        if (destination.getIntent() == null) {
            throw new IllegalStateException("Destination " + destination.getId()
                    + " does not have an Intent set.");
        }
        Intent intent = new Intent(destination.getIntent());
        if (args != null) {
            intent.putExtras(args);
            String dataPattern = destination.getDataPattern();
            if (!TextUtils.isEmpty(dataPattern)) {
                // Fill in the data pattern with the args to build a valid URI
                StringBuffer data = new StringBuffer();
                Pattern fillInPattern = Pattern.compile("\\{(.+?)\\}");
                Matcher matcher = fillInPattern.matcher(dataPattern);
                while (matcher.find()) {
                    String argName = matcher.group(1);
                    if (args.containsKey(argName)) {
                        matcher.appendReplacement(data, "");
                        //noinspection ConstantConditions
                        data.append(Uri.encode(args.get(argName).toString()));
                    } else {
                        throw new IllegalArgumentException("Could not find " + argName + " in "
                                + args + " to fill data pattern " + dataPattern);
                    }
                }
                matcher.appendTail(data);
                intent.setData(Uri.parse(data.toString()));
            }
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            intent.addFlags(extras.getFlags());
        }
        if (!(mContext instanceof Activity)) {
            // If we're not launching from an Activity context we have to launch in a new task.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (navOptions != null && navOptions.shouldLaunchSingleTop()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        if (mHostActivity != null) {
            final Intent hostIntent = mHostActivity.getIntent();
            if (hostIntent != null) {
                final int hostCurrentId = hostIntent.getIntExtra(EXTRA_NAV_CURRENT, 0);
                if (hostCurrentId != 0) {
                    intent.putExtra(EXTRA_NAV_SOURCE, hostCurrentId);
                }
            }
        }
        final int destId = destination.getId();
        intent.putExtra(EXTRA_NAV_CURRENT, destId);
        Resources resources = getContext().getResources();
        if (navOptions != null) {
            int popEnterAnim = navOptions.getPopEnterAnim();
            int popExitAnim = navOptions.getPopExitAnim();
            if ((popEnterAnim != -1
                    && resources.getResourceTypeName(popEnterAnim).equals("animator"))
                    || (popExitAnim != -1
                    && resources.getResourceTypeName(popExitAnim).equals("animator"))) {
                Log.w(LOG_TAG, "Activity destinations do not support Animator resource. Ignoring "
                        + "popEnter resource " + resources.getResourceName(popEnterAnim) + " and "
                        + "popExit resource " + resources.getResourceName(popExitAnim) + "when "
                        + "launching " + destination);
            } else {
                // For use in applyPopAnimationsToPendingTransition()
                intent.putExtra(EXTRA_POP_ENTER_ANIM, popEnterAnim);
                intent.putExtra(EXTRA_POP_EXIT_ANIM, popExitAnim);
            }
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            ActivityOptionsCompat activityOptions = extras.getActivityOptions();
            if (activityOptions != null) {
                ActivityCompat.startActivity(mContext, intent, activityOptions.toBundle());
            } else {
                mContext.startActivity(intent);
            }
        } else {
            mContext.startActivity(intent);
        }
        if (navOptions != null && mHostActivity != null) {
            int enterAnim = navOptions.getEnterAnim();
            int exitAnim = navOptions.getExitAnim();
            if (enterAnim != -1 || exitAnim != -1) {
                if (resources.getResourceTypeName(enterAnim).equals("animator")
                        || resources.getResourceTypeName(exitAnim).equals("animator")
                ) {
                    Log.w(LOG_TAG, "Activity destinations do not support Animator resource. "
                            + "Ignoring " + "enter resource " + resources.getResourceName(enterAnim)
                            + " and exit resource " + resources.getResourceName(exitAnim) + "when "
                            + "launching " + destination);
                } else {
                    enterAnim = enterAnim != -1 ? enterAnim : 0;
                    exitAnim = exitAnim != -1 ? exitAnim : 0;
                    mHostActivity.overridePendingTransition(enterAnim, exitAnim);
                }
            }
        }

        // You can't pop the back stack from the caller of a new Activity,
        // so we don't add this navigator to the controller's back stack
        return null;
    }

    /**
     * NavDestination for activity navigation
     */
    @FoldableNavDestination.ClassType(Activity.class)
    public static class Destination extends FoldableNavDestination {
        private Intent mIntent;
        private String mDataPattern;

        /**
         * Construct a new activity destination. This destination is not valid until you set the
         * Intent via {@link #setIntent(Intent)} or one or more of the other set method.
         *
         *
         * @param navigatorProvider The {@link FoldableNavController} which this destination
         *                          will be associated with.
         */
        public Destination(@NonNull FoldableNavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(FoldableActivityNavigator.class));
        }

        /**
         * Construct a new activity destination. This destination is not valid until you set the
         * Intent via {@link #setIntent(Intent)} or one or more of the other set method.
         *
         * @param activityNavigator The {@link FoldableActivityNavigator} which this destination
         *                          will be associated with. Generally retrieved via a
         *                          {@link FoldableNavController}'s
         *                          {@link FoldableNavigatorProvider#getNavigator(Class)} method.
         */
        public Destination(@NonNull FoldableNavigator<? extends Destination> activityNavigator) {
            super(activityNavigator);
        }

        @CallSuper
        @Override
        public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs) {
            super.onInflate(context, attrs);
            TypedArray a = context.getResources().obtainAttributes(attrs,
                    R.styleable.ActivityNavigator);
            String targetPackage = a.getString(R.styleable.ActivityNavigator_targetPackage);
            if (targetPackage != null) {
                targetPackage = targetPackage.replace(FoldableNavInflater.APPLICATION_ID_PLACEHOLDER,
                        context.getPackageName());
            }
            setTargetPackage(targetPackage);
            String className = a.getString(R.styleable.ActivityNavigator_android_name);
            if (className != null) {
                if (className.charAt(0) == '.') {
                    className = context.getPackageName() + className;
                }
                setComponentName(new ComponentName(context, className));
            }
            setAction(a.getString(R.styleable.ActivityNavigator_action));
            String data = a.getString(R.styleable.ActivityNavigator_data);
            if (data != null) {
                setData(Uri.parse(data));
            }
            setDataPattern(a.getString(R.styleable.ActivityNavigator_dataPattern));
            a.recycle();
        }

        /**
         * Set the Intent to start when navigating to this destination.
         * @param intent Intent to associated with this destination.
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setIntent(@Nullable Intent intent) {
            mIntent = intent;
            return this;
        }

        /**
         * Gets the Intent associated with this destination.
         * @return
         */
        @Nullable
        public final Intent getIntent() {
            return mIntent;
        }

        /**
         * Set an explicit application package name that limits
         * the components this destination will navigate to.
         * <p>
         * When inflated from XML, you can use <code>${applicationId}</code> as the
         * package name to automatically use {@link Context#getPackageName()}.
         *
         * @param packageName packageName to set
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setTargetPackage(@Nullable String packageName) {
            if (mIntent == null) {
                mIntent = new Intent();
            }
            mIntent.setPackage(packageName);
            return this;
        }

        /**
         * Get the explicit application package name associated with this destination, if any
         */
        @Nullable
        public final String getTargetPackage() {
            if (mIntent == null) {
                return null;
            }
            return mIntent.getPackage();
        }

        /**
         * Set an explicit {@link ComponentName} to navigate to.
         *
         * @param name The component name of the Activity to start.
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setComponentName(@Nullable ComponentName name) {
            if (mIntent == null) {
                mIntent = new Intent();
            }
            mIntent.setComponent(name);
            return this;
        }

        /**
         * Get the explicit {@link ComponentName} associated with this destination, if any
         * @return
         */
        @Nullable
        public final ComponentName getComponent() {
            if (mIntent == null) {
                return null;
            }
            return mIntent.getComponent();
        }

        /**
         * Sets the action sent when navigating to this destination.
         * @param action The action string to use.
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setAction(@Nullable String action) {
            if (mIntent == null) {
                mIntent = new Intent();
            }
            mIntent.setAction(action);
            return this;
        }

        /**
         * Get the action used to start the Activity, if any
         */
        @Nullable
        public final String getAction() {
            if (mIntent == null) {
                return null;
            }
            return mIntent.getAction();
        }

        /**
         * Sets a static data URI that is sent when navigating to this destination.
         *
         * <p>To use a dynamic URI that changes based on the arguments passed in when navigating,
         * use {@link #setDataPattern(String)}, which will take precedence when arguments are
         * present.</p>
         *
         * @param data A static URI that should always be used.
         * @see #setDataPattern(String)
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setData(@Nullable Uri data) {
            if (mIntent == null) {
                mIntent = new Intent();
            }
            mIntent.setData(data);
            return this;
        }

        /**
         * Get the data URI used to start the Activity, if any
         */
        @Nullable
        public final Uri getData() {
            if (mIntent == null) {
                return null;
            }
            return mIntent.getData();
        }

        /**
         * Sets a dynamic data URI pattern that is sent when navigating to this destination.
         *
         * <p>If a non-null arguments Bundle is present when navigating, any segments in the form
         * <code>{argName}</code> will be replaced with a URI encoded string from the arguments.</p>
         * @param dataPattern A URI pattern with segments in the form of <code>{argName}</code> that
         *                    will be replaced with URI encoded versions of the Strings in the
         *                    arguments Bundle.
         * @see #setData
         * @return this {@link Destination}
         */
        @NonNull
        public final Destination setDataPattern(@Nullable String dataPattern) {
            mDataPattern = dataPattern;
            return this;
        }

        /**
         * Gets the dynamic data URI pattern, if any
         */
        @Nullable
        public final String getDataPattern() {
            return mDataPattern;
        }

        @Override
        boolean supportsActions() {
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            ComponentName componentName = getComponent();
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (componentName != null) {
                sb.append(" class=");
                sb.append(componentName.getClassName());
            } else {
                String action = getAction();
                if (action != null) {
                    sb.append(" action=");
                    sb.append(action);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Extras that can be passed to ActivityNavigator to customize what
     * {@link ActivityOptionsCompat} and flags are passed through to the call to
     * {@link ActivityCompat#startActivity(Context, Intent, Bundle)}.
     */
    public static final class Extras implements FoldableNavigator.Extras {
        private final int mFlags;
        private final ActivityOptionsCompat mActivityOptions;

        Extras(int flags, @Nullable ActivityOptionsCompat activityOptions) {
            mFlags = flags;
            mActivityOptions = activityOptions;
        }

        /**
         * Gets the <code>Intent.FLAG_ACTIVITY_</code> flags that should be added to the Intent.
         */
        public int getFlags() {
            return mFlags;
        }

        /**
         * Gets the {@link ActivityOptionsCompat} that should be used with
         * {@link ActivityCompat#startActivity(Context, Intent, Bundle)}.
         */
        @Nullable
        public ActivityOptionsCompat getActivityOptions() {
            return mActivityOptions;
        }

        /**
         * Builder for constructing new {@link Extras} instances. The resulting instances are
         * immutable.
         */
        public static final class Builder {
            private int mFlags;
            private ActivityOptionsCompat mActivityOptions;

            /**
             * Adds one or more <code>Intent.FLAG_ACTIVITY_</code> flags
             *
             * @param flags the flags to add
             * @return this {@link Builder}
             */
            @NonNull
            public Builder addFlags(int flags) {
                mFlags |= flags;
                return this;
            }

            /**
             * Sets the {@link ActivityOptionsCompat} that should be used with
             * {@link ActivityCompat#startActivity(Context, Intent, Bundle)}.
             *
             * @param activityOptions The {@link ActivityOptionsCompat} to pass through
             * @return this {@link Builder}
             */
            @NonNull
            public Builder setActivityOptions(@NonNull ActivityOptionsCompat activityOptions) {
                mActivityOptions = activityOptions;
                return this;
            }

            /**
             * Constructs the final {@link Extras} instance.
             *
             * @return An immutable {@link Extras} instance.
             */
            @NonNull
            public Extras build() {
                return new Extras(mFlags, mActivityOptions);
            }
        }
    }
}
