/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.microsoft.device.surfaceduo.display.R
import java.lang.IllegalStateException

/**
 * Class that is the root view of the layout containers for different screen modes.
 * The class takes as parameters the layout ids for the views that will be added inside of the
 * containers and then creates a SurfaceDuoLayoutStatusHandler to handle the logic for each screen
 * state.
 */
class SurfaceDuoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var screenManager: SurfaceDuoScreenManager? = SurfaceDuoScreenManager.instance

    init {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SurfaceDuoLayout, 0, 0)

        val dualScreenEndLayoutId: Int
        val dualScreenStartLayoutId: Int
        val singleScreenLayoutId: Int
        try {
            singleScreenLayoutId = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_single_screen_layout_id,
                View.NO_ID
            )
            dualScreenStartLayoutId = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_dual_screen_start_layout_id,
                View.NO_ID
            )
            dualScreenEndLayoutId = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_dual_screen_end_layout_id,
                View.NO_ID
            )
        } finally {
            styledAttributes.recycle()
        }

        screenManager?.let{
            if (it.isScreenManagerInitialized) {
                SurfaceDuoLayoutStatusHandler(
                    this.context as Activity,
                    this,
                    it,
                    singleScreenLayoutId,
                    dualScreenStartLayoutId,
                    dualScreenEndLayoutId
                )
            } else {
                throw IllegalStateException("SurfaceDuoScreenManager is not initialized in Application class.")
            }

        }
    }
}
