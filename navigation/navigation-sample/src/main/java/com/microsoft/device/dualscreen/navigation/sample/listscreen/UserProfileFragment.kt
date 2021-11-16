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
 *  Portions Copyright (c) Microsoft Corporation
 */

package com.microsoft.device.dualscreen.navigation.sample.listscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.microsoft.device.dualscreen.navigation.sample.databinding.FragmentUserProfileBinding
import com.microsoft.device.dualscreen.navigation.sample.listscreen.LeaderBoardAdapter.Companion.USERNAME_KEY

/**
 * Shows a profile screen for a user, taking the name from the arguments.
 */
class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString(USERNAME_KEY) ?: "Ali Connors"
        binding.profileUserName.text = name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
