/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample.bottomnavigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.device.dualscreen.sample.bottomnavigation.databinding.FragmentNumbersBinding

class NumbersFragment : Fragment() {
    companion object {
        const val ARG_NAME = "arg_name"

        fun newInstance(title: String) = NumbersFragment().apply {
            arguments = bundleOf(ARG_NAME to title)
        }
    }

    private lateinit var binding: FragmentNumbersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNumbersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            fragmentName.text = getString(R.string.key_fragment, getTitle())

            recyclerView.hasFixedSize()
            recyclerView.adapter = NumbersAdapter()
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun getTitle() = arguments?.getString(ARG_NAME)
}