/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.recyclerview.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.device.dualscreen.sample_duolayoutmanager.R

class NumbersAdapter : RecyclerView.Adapter<NumbersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recyclerview, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = numberDataSet[position]
    }

    override fun getItemCount() = numberDataSet.size

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}
