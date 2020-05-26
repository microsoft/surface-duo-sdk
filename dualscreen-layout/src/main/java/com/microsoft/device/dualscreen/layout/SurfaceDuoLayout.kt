/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.microsoft.device.surfaceduo.display.R

/**
 * Class that is the root view of the layout containers for different screen modes.
 * The class takes as parameters the layout ids for the views that will be added inside of the
 * containers and then creates a SurfaceDuoLayoutStatusHandler to handle the logic for each screen
 * state.
 */
open class SurfaceDuoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var surfaceDuoLayoutStatusHandler: SurfaceDuoLayoutStatusHandler

    init {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SurfaceDuoLayout, 0, 0)

        val dualScreenEndLayoutId: Int
        val dualScreenStartLayoutId: Int
        val singleScreenLayoutId: Int
        val showInDualScreenEnd: Int
        val showInDualScreenStart: Int
        val showInSingleScreen: Int
        val screenMode: ScreenMode
        val hingeColor: HingeColor
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
            showInSingleScreen = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_show_in_single_screen,
                View.NO_ID
            )
            showInDualScreenStart = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_show_in_dual_screen_start,
                View.NO_ID
            )
            showInDualScreenEnd = styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_show_in_dual_screen_end,
                View.NO_ID
            )
            screenMode = ScreenMode.fromId(
                styledAttributes.getResourceId(
                    R.styleable.SurfaceDuoLayout_tools_screen_mode,
                    ScreenMode.SINGLE_SCREEN.id
                )
            )
            hingeColor = HingeColor.fromId(
                styledAttributes.getResourceId(
                    R.styleable.SurfaceDuoLayout_tools_hinge_color,
                    HingeColor.BLACK.id
                )
            )
        } finally {
            styledAttributes.recycle()
        }

        if (this.isInEditMode) {
            val singleScreenId: Int =
                if (showInSingleScreen != View.NO_ID) {
                    showInSingleScreen
                } else {
                    singleScreenLayoutId
                }

            val dualScreenStartId: Int =
                if (showInDualScreenStart != View.NO_ID) {
                    showInDualScreenStart
                } else {
                    dualScreenStartLayoutId
                }

            val dualScreenEndId: Int =
                if (showInDualScreenEnd != View.NO_ID) {
                    showInDualScreenEnd
                } else {
                    dualScreenEndLayoutId
                }
            PreviewRenderer(
                singleScreenId,
                dualScreenStartId,
                dualScreenEndId,
                screenMode,
                hingeColor
            )
        } else {
            createView(singleScreenLayoutId, dualScreenStartLayoutId, dualScreenEndLayoutId)
        }
    }

    private fun createView(
        singleScreenLayoutId: Int,
        dualScreenStartLayoutId: Int,
        dualScreenEndLayoutId: Int
    ) {
        surfaceDuoLayoutStatusHandler = SurfaceDuoLayoutStatusHandler(
            this.context as Activity,
            this,
            singleScreenLayoutId,
            dualScreenStartLayoutId,
            dualScreenEndLayoutId
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        surfaceDuoLayoutStatusHandler.onConfigurationChanged(this, newConfig)
    }

    private inner class PreviewRenderer(
        singleScreenLayoutId: Int,
        dualScreenStartLayoutId: Int,
        dualScreenEndLayoutId: Int,
        screenMode: ScreenMode,
        hingeColor: HingeColor
    ) {

        init {
            when (screenMode) {
                ScreenMode.SINGLE_SCREEN -> {
                    val singleScreenView = LayoutInflater
                        .from(context)
                        .inflate(singleScreenLayoutId, null)
                    singleScreenView.layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    this@SurfaceDuoLayout.orientation = VERTICAL
                    this@SurfaceDuoLayout.addView(singleScreenView)
                }
                ScreenMode.DUAL_SCREEN -> {
                    this@SurfaceDuoLayout.weightSum = 2F
                    this@SurfaceDuoLayout.layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )
                    val hinge = FrameLayout(context)

                    when (hingeColor) {
                        HingeColor.BLACK -> hinge.background = ColorDrawable(
                            ContextCompat.getColor(context, R.color.black)
                        )
                        HingeColor.WHITE -> hinge.background = ColorDrawable(
                            ContextCompat.getColor(context, R.color.white)
                        )
                    }

                    val dualScreenStartView = LayoutInflater
                        .from(context)
                        .inflate(dualScreenStartLayoutId, null)
                    val dualScreenEndView = LayoutInflater
                        .from(context)
                        .inflate(dualScreenEndLayoutId, null)

                    if (
                        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    ) {
                        this@SurfaceDuoLayout.orientation = HORIZONTAL

                        hinge.layoutParams = LayoutParams(
                            84,
                            LayoutParams.MATCH_PARENT
                        )
                        val param = LayoutParams(
                            0,
                            LayoutParams.MATCH_PARENT,
                            1F
                        )
                        dualScreenStartView.layoutParams = param
                        dualScreenEndView.layoutParams = param
                    } else if (
                        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    ) {
                        this@SurfaceDuoLayout.orientation = VERTICAL

                        hinge.layoutParams = LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            84
                        )
                        val param = LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            0,
                            1F
                        )
                        dualScreenStartView.layoutParams = param
                        dualScreenEndView.layoutParams = param
                    }

                    this@SurfaceDuoLayout.addView(dualScreenStartView)
                    this@SurfaceDuoLayout.addView(hinge)
                    this@SurfaceDuoLayout.addView(dualScreenEndView)
                }
            }
        }
    }
}
