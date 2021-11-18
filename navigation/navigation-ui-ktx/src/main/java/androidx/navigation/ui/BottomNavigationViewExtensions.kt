/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.navigation.ui

import androidx.navigation.FoldableNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Sets up a [BottomNavigationView] for use with a [FoldableNavController]. This will call
 * [android.view.MenuItem.onNavDestinationSelected] when a menu item is selected.
 *
 * The selected item in the NavigationView will automatically be updated when the destination
 * changes.
 */
public fun BottomNavigationView.setupWithFoldableNavController(navController: FoldableNavController) {
    FoldableNavigationUI.setupWithFoldableNavController(this, navController)
}
