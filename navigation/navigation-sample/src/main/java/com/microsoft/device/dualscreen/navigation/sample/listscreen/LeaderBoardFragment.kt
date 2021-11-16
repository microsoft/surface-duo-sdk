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

package com.microsoft.device.dualscreen.navigation.sample.listscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findFoldableNavController
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.navigation.sample.R
import com.microsoft.device.dualscreen.navigation.sample.databinding.FragmentLeaderboardBinding
import com.microsoft.device.dualscreen.navigation.sample.databinding.ListViewItemBinding

/**
 * Shows a static leaderBoard with multiple users.
 */
class LeaderBoardFragment : Fragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewAdapter = LeaderBoardAdapter(Array(10) { "Person ${it + 1}" })

        binding.leaderboardList.run {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LeaderBoardAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val binding: ListViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListViewItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        with(holder) {
            with(binding) {
                userNameText.text = dataSet[position]
                userAvatarImage.setImageResource(listOfAvatars[position % listOfAvatars.size])
                root.setOnClickListener {
                    val bundle = bundleOf(USERNAME_KEY to dataSet[position])

                    root.findFoldableNavController().navigate(
                        R.id.action_leaderboard_to_userProfile,
                        bundle
                    )
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    companion object {
        const val USERNAME_KEY = "userName"
    }
}

private val listOfAvatars = listOf(
    R.drawable.avatar_1_raster,
    R.drawable.avatar_2_raster,
    R.drawable.avatar_3_raster,
    R.drawable.avatar_4_raster,
    R.drawable.avatar_5_raster,
    R.drawable.avatar_6_raster
)
