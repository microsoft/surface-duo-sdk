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

package com.microsoft.device.dualscreen.navigation.sample.homescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.FoldableNavController
import androidx.navigation.fragment.findFoldableNavController
import com.microsoft.device.dualscreen.navigation.sample.R
import com.microsoft.device.dualscreen.navigation.sample.databinding.FragmentTitleBinding

/**
 * Shows the main title screen with a button that navigates to [AboutFragment].
 */
class TitleFragment : Fragment() {
    private var _binding: FragmentTitleBinding? = null
    private val binding get() = _binding!!
    private val navController: FoldableNavController by lazy { findFoldableNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aboutBtn.setOnClickListener {
            navController.navigate(R.id.action_title_to_about)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
