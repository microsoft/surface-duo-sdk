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
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.microsoft.device.surfaceduo.display.R


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
        val screenMode: Int
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
            screenMode = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_tools_screen_mode,
                View.NO_ID
            )
        } finally {
            styledAttributes.recycle()
        }

        if (this.isInEditMode) {
            PreviewRenderer(
                this,
                singleScreenLayoutId,
                dualScreenStartLayoutId,
                dualScreenEndLayoutId,
                screenMode
            )
        } else {
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

    enum class ScreenMode {
        SINGLE_SCREEN,
        DUAL_SCREEN
    }

    inner class PreviewRenderer(
        rootView: FrameLayout,
        singleScreenLayoutId: Int,
        dualScreenStartLayoutId: Int,
        dualScreenEndLayoutId: Int,
        screenMode: Int
    ) {

        init {

            when (screenMode) {
                ScreenMode.SINGLE_SCREEN.ordinal -> {
                    val singleScreenView = LayoutInflater.from(context).inflate(singleScreenLayoutId, null)
                    rootView.addView(singleScreenView)
                }
                ScreenMode.DUAL_SCREEN.ordinal -> {
                    val linearLayout = LinearLayout(context)
                    linearLayout.orientation = LinearLayout.HORIZONTAL
                    linearLayout.weightSum = 2F

                    val view = LayoutInflater.from(context).inflate(dualScreenStartLayoutId, null)
                    val view2 = LayoutInflater.from(context).inflate(dualScreenEndLayoutId, null)

                    val param = LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT,
                        1.0f
                    )
                    view.layoutParams = param
                    view2.layoutParams = param

                    linearLayout.addView(view)
                    linearLayout.addView(view2)
                    rootView.addView(linearLayout)
                }
                else -> {
                    throw java.lang.IllegalStateException("No ScreenMode added to preview the layout. Use app:tools_screen_mode=\"single_screen\" or app:tools_screen_mode=\"dual_screen\".")
                }
            }


        }
    }
}
