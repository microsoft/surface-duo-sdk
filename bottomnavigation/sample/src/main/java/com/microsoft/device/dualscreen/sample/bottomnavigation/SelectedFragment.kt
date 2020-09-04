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
import kotlinx.android.synthetic.main.fragment_selected.*

class SelectedFragment : Fragment() {
    companion object {
        const val ARG_NAME = "arg_name"

        fun newInstance(title: String) = SelectedFragment().apply {
            arguments = bundleOf(ARG_NAME to title)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragment_name.text = getString(R.string.key_fragment, getTitle())

        recyclerView.hasFixedSize()
        recyclerView.adapter = NumbersAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun getTitle() = arguments?.getString(ARG_NAME)
}