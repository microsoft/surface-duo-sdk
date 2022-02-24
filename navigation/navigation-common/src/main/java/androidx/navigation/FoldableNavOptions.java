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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.microsoft.device.dualscreen.navigation.LaunchScreen;

/**
 * NavOptions stores special options for navigate actions
 */
public final class FoldableNavOptions implements Parcelable {
    private boolean mSingleTop;
    @IdRes
    private int mPopUpTo;
    private boolean mPopUpToInclusive;
    @AnimRes @AnimatorRes
    private int mEnterAnim;
    @AnimRes @AnimatorRes
    private int mExitAnim;
    @AnimRes @AnimatorRes
    private int mPopEnterAnim;
    @AnimRes @AnimatorRes
    private int mPopExitAnim;
    private LaunchScreen mLaunchScreen;

    @Override
    public String toString() {
        return "[singleTop = " + mSingleTop +
                ", popupTo = " + mPopUpTo +
                ", popupToInclusive = " + mPopUpToInclusive +
                ", enterAnim = " + mEnterAnim +
                ", exitAnim = " + mExitAnim +
                ", popEnterAnim = " + mPopEnterAnim +
                ", popExitAnim = " + mPopExitAnim +
                ", launchScreen = " + mLaunchScreen.name() + "]";
    }

    FoldableNavOptions(boolean singleTop, @IdRes int popUpTo, boolean popUpToInclusive,
                       @AnimRes @AnimatorRes int enterAnim, @AnimRes @AnimatorRes int exitAnim,
                       @AnimRes @AnimatorRes int popEnterAnim, @AnimRes @AnimatorRes int popExitAnim, LaunchScreen openOn) {
        mSingleTop = singleTop;
        mPopUpTo = popUpTo;
        mPopUpToInclusive = popUpToInclusive;
        mEnterAnim = enterAnim;
        mExitAnim = exitAnim;
        mPopEnterAnim = popEnterAnim;
        mPopExitAnim = popExitAnim;
        mLaunchScreen = openOn;
    }

    /**
     * Whether this navigation action should launch as single-top (i.e., there will be at most
     * one copy of a given destination on the top of the back stack).
     * <p>
     * This functions similarly to how {@link android.content.Intent#FLAG_ACTIVITY_SINGLE_TOP}
     * works with activites.
     */
    public boolean shouldLaunchSingleTop() {
        return mSingleTop;
    }

    /**
     * The destination to pop up to before navigating. When set, all non-matching destinations
     * should be popped from the back stack.
     * @return the destinationId to pop up to, clearing all intervening destinations
     * @see Builder#setPopUpTo
     * @see #isPopUpToInclusive
     */
    @IdRes
    public int getPopUpTo() {
        return mPopUpTo;
    }

    /**
     * Whether the destination set in {@link #getPopUpTo} should be popped from the back stack.
     * @see Builder#setPopUpTo
     * @see #getPopUpTo
     */
    public boolean isPopUpToInclusive() {
        return mPopUpToInclusive;
    }

    /**
     * The custom enter Animation/Animator that should be run.
     * @return the resource id of a Animation or Animator or -1 if none.
     */
    @AnimRes @AnimatorRes
    public int getEnterAnim() {
        return mEnterAnim;
    }

    /**
     * The custom exit Animation/Animator that should be run.
     * @return the resource id of a Animation or Animator or -1 if none.
     */
    @AnimRes @AnimatorRes
    public int getExitAnim() {
        return mExitAnim;
    }

    /**
     * The custom enter Animation/Animator that should be run when this destination is
     * popped from the back stack.
     * @return the resource id of a Animation or Animator or -1 if none.
     */
    @AnimRes @AnimatorRes
    public int getPopEnterAnim() {
        return mPopEnterAnim;
    }

    /**
     * The custom exit Animation/Animator that should be run when this destination is
     * popped from the back stack.
     * @return the resource id of a Animation or Animator or -1 if none.
     */
    @AnimRes @AnimatorRes
    public int getPopExitAnim() {
        return mPopExitAnim;
    }

    public LaunchScreen getLaunchScreen() {
        return mLaunchScreen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSingleTop ? 1 : 0);
        dest.writeInt(mPopUpTo);
        dest.writeInt(mPopUpToInclusive ? 1 : 0);
        dest.writeInt(mEnterAnim);
        dest.writeInt(mExitAnim);
        dest.writeInt(mPopEnterAnim);
        dest.writeInt(mPopExitAnim);
        dest.writeInt(mLaunchScreen.getValue());
    }

    FoldableNavOptions(Parcel in) {
        mSingleTop = in.readInt() == 1;
        mPopUpTo = in.readInt();
        mPopUpToInclusive = in.readInt() == 1;
        mEnterAnim = in.readInt();
        mExitAnim = in.readInt();
        mPopEnterAnim = in.readInt();
        mPopExitAnim = in.readInt();
        mLaunchScreen = LaunchScreen.fromValue(in.readInt());
    }

    public static final Creator<FoldableNavOptions> CREATOR = new Creator<FoldableNavOptions>() {
        @Override
        public FoldableNavOptions createFromParcel(Parcel in) {
            return new FoldableNavOptions(in);
        }

        @Override
        public FoldableNavOptions[] newArray(int size) {
            return new FoldableNavOptions[size];
        }
    };

    /**
     * Builder for constructing new instances of NavOptions.
     */
    public static final class Builder {
        boolean mSingleTop;
        @IdRes
        int mPopUpTo = -1;
        boolean mPopUpToInclusive;
        @AnimRes @AnimatorRes
        int mEnterAnim = -1;
        @AnimRes @AnimatorRes
        int mExitAnim = -1;
        @AnimRes @AnimatorRes
        int mPopEnterAnim = -1;
        @AnimRes @AnimatorRes
        int mPopExitAnim = -1;
        LaunchScreen mLaunchScreen;

        public Builder() {
        }

        /**
         * Launch a navigation target as single-top if you are making a lateral navigation
         * between instances of the same target (e.g. detail pages about similar data items)
         * that should not preserve history.
         *
         * @param singleTop true to launch as single-top
         */
        @NonNull
        public Builder setLaunchSingleTop(boolean singleTop) {
            mSingleTop = singleTop;
            return this;
        }

        /**
         * Pop up to a given destination before navigating. This pops all non-matching destinations
         * from the back stack until this destination is found.
         *
         * @param destinationId The destination to pop up to, clearing all intervening destinations.
         * @param inclusive true to also pop the given destination from the back stack.
         * @return this Builder
         * @see FoldableNavOptions#getPopUpTo
         * @see FoldableNavOptions#isPopUpToInclusive
         */
        @NonNull
        public Builder setPopUpTo(@IdRes int destinationId, boolean inclusive) {
            mPopUpTo = destinationId;
            mPopUpToInclusive = inclusive;
            return this;
        }

        /**
         * Sets a custom Animation or Animator resource for the enter animation.
         *
         * <p>Note: Animator resources are not supported for navigating to a new Activity</p>
         * @param enterAnim Custom animation to run
         * @return this Builder
         * @see FoldableNavOptions#getEnterAnim()
         */
        @NonNull
        public Builder setEnterAnim(@AnimRes @AnimatorRes int enterAnim) {
            mEnterAnim = enterAnim;
            return this;
        }

        /**
         * Sets a custom Animation or Animator resource for the exit animation.
         *
         * <p>Note: Animator resources are not supported for navigating to a new Activity</p>
         * @param exitAnim Custom animation to run
         * @return this Builder
         * @see FoldableNavOptions#getExitAnim()
         */
        @NonNull
        public Builder setExitAnim(@AnimRes @AnimatorRes int exitAnim) {
            mExitAnim = exitAnim;
            return this;
        }

        /**
         * Sets a custom Animation or Animator resource for the enter animation
         * when popping off the back stack.
         *
         * <p>Note: Animator resources are not supported for navigating to a new Activity</p>
         * @param popEnterAnim Custom animation to run
         * @return this Builder
         * @see FoldableNavOptions#getPopEnterAnim()
         */
        @NonNull
        public Builder setPopEnterAnim(@AnimRes @AnimatorRes int popEnterAnim) {
            mPopEnterAnim = popEnterAnim;
            return this;
        }

        /**
         * Sets a custom Animation or Animator resource for the exit animation
         * when popping off the back stack.
         *
         * <p>Note: Animator resources are not supported for navigating to a new Activity</p>
         * @param popExitAnim Custom animation to run
         * @return this Builder
         * @see FoldableNavOptions#getPopExitAnim()
         */
        @NonNull
        public Builder setPopExitAnim(@AnimRes @AnimatorRes int popExitAnim) {
            mPopExitAnim = popExitAnim;
            return this;
        }

        public Builder setLaunchScreen(LaunchScreen openOn) {
            this.mLaunchScreen = openOn;
            return this;
        }

        /**
         * @return a constructed NavOptions
         */
        @NonNull
        public FoldableNavOptions build() {
            return new FoldableNavOptions(mSingleTop, mPopUpTo, mPopUpToInclusive,
                    mEnterAnim, mExitAnim, mPopEnterAnim, mPopExitAnim, mLaunchScreen);
        }
    }
}
