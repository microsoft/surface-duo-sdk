/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation.sample.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findFoldableNavController
import com.microsoft.device.dualscreen.navigation.sample.R
import com.microsoft.device.dualscreen.navigation.sample.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            findFoldableNavController().navigate(R.id.action_register)
        }

        binding.btnWelcome.setOnClickListener {
            findFoldableNavController().navigate(R.id.action_welcome)
        }

        binding.btnPersons.setOnClickListener {
            findFoldableNavController().navigate(R.id.action_persons)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
