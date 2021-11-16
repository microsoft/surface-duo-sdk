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

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A NavigationProvider stores a set of {@link FoldableNavigator}s that are valid ways to navigate
 * to a destination.
 */
@SuppressLint("TypeParameterUnusedInFormals")
public class FoldableNavigatorProvider {
    private static final HashMap<Class<?>, String> sAnnotationNames = new HashMap<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateName(String name) {
        return name != null && !name.isEmpty();
    }

    @NonNull
    static String getNameForNavigator(@NonNull Class<? extends FoldableNavigator> navigatorClass) {
        String name = sAnnotationNames.get(navigatorClass);
        if (name == null) {
            FoldableNavigator.Name annotation = navigatorClass.getAnnotation(FoldableNavigator.Name.class);
            name = annotation != null ? annotation.value() : null;
            if (!validateName(name)) {
                throw new IllegalArgumentException("No @FoldableNavigator.Name annotation found for "
                        + navigatorClass.getSimpleName());
            }
            sAnnotationNames.put(navigatorClass, name);
        }
        return name;
    }

    private final HashMap<String, FoldableNavigator<? extends FoldableNavDestination>> mNavigators =
            new HashMap<>();

    /**
     * Retrieves a registered {@link FoldableNavigator} using the name provided by the
     * {@link FoldableNavigator.Name Navigator.Name annotation}.
     *
     * @param navigatorClass class of the navigator to return
     * @return the registered navigator with the given {@link FoldableNavigator.Name}
     * @throws IllegalArgumentException if the Navigator does not have a
     *                                  {@link FoldableNavigator.Name Navigator.Name annotation}
     * @throws IllegalStateException    if the Navigator has not been added
     * @see #addNavigator(FoldableNavigator)
     */
    @NonNull
    public final <T extends FoldableNavigator<?>> T getNavigator(@NonNull Class<T> navigatorClass) {
        String name = getNameForNavigator(navigatorClass);
        return getNavigator(name);
    }

    /**
     * Retrieves a registered {@link FoldableNavigator} by name.
     *
     * @param name name of the navigator to return
     * @return the registered navigator with the given name
     * @throws IllegalStateException if the Navigator has not been added
     * @see #addNavigator(String, FoldableNavigator)
     */
    @SuppressWarnings("unchecked")
    @CallSuper
    @NonNull
    public <T extends FoldableNavigator<?>> T getNavigator(@NonNull String name) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("navigator name cannot be an empty string");
        }
        FoldableNavigator<? extends FoldableNavDestination> navigator = mNavigators.get(name);
        if (navigator == null) {
            throw new IllegalStateException("Could not find Navigator with name \"" + name
                    + "\". You must call NavController.addNavigator() for each navigation type.");
        }
        return (T) navigator;
    }

    /**
     * Register a navigator using the name provided by the
     * {@link FoldableNavigator.Name Navigator.Name annotation}. {@link FoldableNavDestination destinations} may
     * refer to any registered navigator by name for inflation. If a navigator by this name is
     * already registered, this new navigator will replace it.
     *
     * @param navigator navigator to add
     * @return the previously added Navigator for the name provided by the
     * {@link FoldableNavigator.Name Navigator.Name annotation}, if any
     */
    @Nullable
    public final FoldableNavigator<? extends FoldableNavDestination> addNavigator(
            @NonNull FoldableNavigator<? extends FoldableNavDestination> navigator) {
        String name = getNameForNavigator(navigator.getClass());
        return addNavigator(name, navigator);
    }

    /**
     * Register a navigator by name. {@link NavDestination destinations} may refer to any
     * registered navigator by name for inflation. If a navigator by this name is already
     * registered, this new navigator will replace it.
     *
     * @param name      name for this navigator
     * @param navigator navigator to add
     * @return the previously added Navigator for the given name, if any
     */
    @CallSuper
    @Nullable
    public FoldableNavigator<? extends FoldableNavDestination> addNavigator(@NonNull String name,
                                                                            @NonNull FoldableNavigator<? extends FoldableNavDestination> navigator) {
        if (!validateName(name)) {
            throw new IllegalArgumentException("navigator name cannot be an empty string");
        }
        return mNavigators.put(name, navigator);
    }

    Map<String, FoldableNavigator<? extends FoldableNavDestination>> getNavigators() {
        return mNavigators;
    }
}