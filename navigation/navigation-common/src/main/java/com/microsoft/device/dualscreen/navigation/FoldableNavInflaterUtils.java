/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.navigation;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.navigation.common.R;

import com.microsoft.device.dualscreen.navigation.LaunchScreen;

/**
 * Utilities class for {@link androidx.navigation.FoldableNavInflater}
 */
public final class FoldableNavInflaterUtils {
    /**
     * Inflate {@link LaunchScreen} from a resource
     *
     * @param res   {@link Resources} instance for the application package
     * @param attrs attrs to parse during inflation
     * @return the {@link LaunchScreen} from the given attrs
     */
    public static LaunchScreen getLaunchScreen(@NonNull Resources res, @NonNull AttributeSet attrs) {
        final TypedArray a = res.obtainAttributes(attrs, R.styleable.FoldableNavigation);
        int launchScreenValue = a.getInt(R.styleable.FoldableNavigation_launchScreen, LaunchScreen.DEFAULT.getValue());
        a.recycle();
        return LaunchScreen.fromValue(launchScreenValue);
    }
}
