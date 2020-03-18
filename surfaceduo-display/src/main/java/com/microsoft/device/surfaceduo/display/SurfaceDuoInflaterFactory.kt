/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.surfaceduo.display

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

/**
 * Class that takes a SurfaceDuoScreenManager object as parameter and will pass it to the
 * SurfaceDuoLayout layout when it will be created.
 */
internal class SurfaceDuoInflaterFactory(
    private val surfaceDuoScreenManager: SurfaceDuoScreenManager
) : LayoutInflater.Factory2 {

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return getView(name, context, attrs)
    }

    private fun getView(name: String, context: Context, attrs: AttributeSet): View? {
        return if (name == SurfaceDuoLayout::class.java.name) {
            SurfaceDuoLayout(context, surfaceDuoScreenManager, attrs)
        } else { null }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        return getView(name, context, attrs)
    }
}
