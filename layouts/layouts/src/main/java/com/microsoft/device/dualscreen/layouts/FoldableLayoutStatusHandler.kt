/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layouts

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.VERTICAL
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.window.layout.FoldingFeature
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.getScreenRectangles
import com.microsoft.device.dualscreen.utils.wm.getWindowRect

/**
 * Class responsible for the logic of displaying the layout containers depending on the screen state.
 * The class automatically handles the resize of the layout containers if the device rotates and
 * also the position of the hinge.
 */
internal class FoldableLayoutStatusHandler internal constructor(
    private val context: Context,
    private val rootView: FoldableLayout,
    private val foldableLayoutConfig: FoldableLayout.Config
) {
    private var screenMode = ScreenMode.SINGLE_SCREEN
    private var foldingFeature: FoldingFeature? = null

    fun addViewsDependingOnSpanningMode(foldingFeature: FoldingFeature?) {
        this.foldingFeature = foldingFeature
        screenMode = if (foldingFeature.screenMode() == ScreenMode.DUAL_SCREEN) {
            addDualScreenBehaviour(foldingFeature!!)
            ScreenMode.DUAL_SCREEN
        } else {
            addSingleScreenBehaviour()
            ScreenMode.SINGLE_SCREEN
        }
    }

    private fun FoldingFeature?.screenMode(): ScreenMode {
        return if (this != null) {
            ScreenMode.DUAL_SCREEN
        } else {
            ScreenMode.SINGLE_SCREEN
        }
    }

    /**
     * Single screen behaviour function.
     *
     * It will create a layout container, add the inflated view to it
     * and then add the container to the root view.
     */
    private fun addSingleScreenBehaviour() {
        rootView.removeAllViews()

        val singleScreenContainer = createSingleContainer(R.id.first_container_id)
        if (foldableLayoutConfig.singleScreenLayoutId != View.NO_ID) {
            singleScreenContainer.addView(
                LayoutInflater.from(context)
                    .inflate(foldableLayoutConfig.singleScreenLayoutId, this.rootView, false)
            )
        }
        rootView.addView(singleScreenContainer)
    }

    private fun createSingleContainer(containerId: Int): FrameLayout {
        val container = FrameLayout(rootView.context)
        container.id = containerId
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        return container
    }

    /**
     * Dual Screen behaviour function.
     */
    private fun addDualScreenBehaviour(foldingFeature: FoldingFeature) {
        rootView.removeAllViews()
        when (foldingFeature.orientation) {
            FoldingFeature.Orientation.HORIZONTAL -> dualLandscapeLogic(foldingFeature)
            FoldingFeature.Orientation.VERTICAL -> dualPortraitLogic(foldingFeature)
        }
    }

    /**
     * Contains the logic for:
     * 1) (dual screen landscape) dual portrait - single container
     * 2) (dual screen landscape) dual portrait - dual container
     *
     * Depending on the configuration, the function will create
     * a single or two containers for the entire screen.
     *
     * After that the code will add the inflated views to the containers and add everything to
     * the root view.
     */
    private fun dualPortraitLogic(foldingFeature: FoldingFeature) {
        if (foldableLayoutConfig.isDualPortraitSingleContainer ||
            foldableLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID
        ) {
            val singleContainer = createSingleContainer(R.id.first_container_id)
            if (foldableLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID) {
                LayoutInflater.from(context)
                    .inflate(
                        foldableLayoutConfig.dualPortraitSingleLayoutId,
                        this.rootView,
                        false
                    )?.let {
                        if (!foldableLayoutConfig.isDualPortraitSingleContainer) {
                            foldableLayoutConfig.isDualPortraitSingleContainer = true
                        }
                        singleContainer.addView(it)
                    }
            }
            rootView.addView(singleContainer)
        } else {
            getScreenRectangles(
                foldingFeature.bounds,
                context.getWindowRect()
            )?.let { screenRectList ->
                addDualScreenContainersAndViews(
                    HORIZONTAL,
                    screenRectList[0],
                    screenRectList[1],
                    foldingFeature
                )
            }
        }
    }

    /**
     * Contains the logic for:
     * 3) (dual screen portrait) dual landscape - single container
     * 4) (dual screen portrait) dual landscape - dual container
     *
     * Depending on the configuration, the function will create
     * a single or two containers for the entire screen.
     *
     * After that the code will add the inflated views to the containers and add everything to
     * the root view.
     */
    private fun dualLandscapeLogic(foldingFeature: FoldingFeature) {
        if (foldableLayoutConfig.isDualLandscapeSingleContainer ||
            foldableLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID
        ) {
            val singleContainer = createSingleContainer(R.id.first_container_id)
            if (foldableLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID) {
                LayoutInflater.from(context)
                    .inflate(
                        foldableLayoutConfig.dualLandscapeSingleLayoutId,
                        this.rootView,
                        false
                    )?.let {
                        if (!foldableLayoutConfig.isDualLandscapeSingleContainer) {
                            foldableLayoutConfig.isDualLandscapeSingleContainer = true
                        }
                        singleContainer.addView(it)
                    }
            }
            rootView.addView(singleContainer)
        } else {
            getScreenRectangles(
                foldingFeature.bounds,
                context.getWindowRect()
            )?.let { screenRectList ->
                addDualScreenContainersAndViews(
                    VERTICAL,
                    screenRectList[0],
                    screenRectList[1],
                    foldingFeature
                )
            }
        }
    }

    private fun addDualScreenContainersAndViews(
        linearLayoutOrientation: Int,
        screenRectStart: Rect,
        screenRectEnd: Rect,
        foldingFeature: FoldingFeature
    ) {
        rootView.orientation = linearLayoutOrientation

        // Hinge
        val hinge = createHingeView(foldingFeature)
        // Start and End Layouts
        val dualScreenStartContainer = FrameLayout(rootView.context)
        dualScreenStartContainer.id = R.id.first_container_id
        val dualScreenEndContainer = FrameLayout(rootView.context)
        dualScreenEndContainer.id = R.id.second_container_id

        setDualScreenContainersDimensions(
            linearLayoutOrientation,
            dualScreenStartContainer,
            dualScreenEndContainer,
            screenRectStart,
            screenRectEnd,
            foldingFeature
        )

        foldableLayoutConfig.dualScreenStartLayoutId
            .takeIf { it != View.NO_ID }?.let {
                dualScreenStartContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(foldableLayoutConfig.dualScreenStartLayoutId, rootView, false)
                )
            }
        foldableLayoutConfig.dualScreenEndLayoutId
            .takeIf { it != View.NO_ID }?.let {
                dualScreenEndContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(foldableLayoutConfig.dualScreenEndLayoutId, rootView, false)
                )
            }

        rootView.addView(dualScreenStartContainer)
        rootView.addView(hinge)
        rootView.addView(dualScreenEndContainer)
    }

    private fun setDualScreenContainersDimensions(
        linearLayoutOrientation: Int,
        dualScreenStartContainer: FrameLayout,
        dualScreenEndContainer: FrameLayout,
        screenRectStart: Rect,
        screenRectEnd: Rect,
        foldingFeature: FoldingFeature
    ) {
        if (linearLayoutOrientation == VERTICAL) {
            // DUAL_LANDSCAPE
            dualScreenStartContainer.layoutParams =
                LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            dualScreenEndContainer.layoutParams =
                LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            rootView.doOnNextLayout {
                val location = rootView.locationOnScreen
                val dualScreenStartContainerHeight = foldingFeature.bounds.top - location.y
                dualScreenStartContainer.layoutParams =
                    LinearLayout.LayoutParams(MATCH_PARENT, dualScreenStartContainerHeight)
                dualScreenEndContainer.layoutParams =
                    LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        } else {
            // DUAL_PORTRAIT
            dualScreenStartContainer.layoutParams =
                LinearLayout.LayoutParams(screenRectStart.width(), MATCH_PARENT)
            dualScreenEndContainer.layoutParams =
                LinearLayout.LayoutParams(screenRectEnd.width(), MATCH_PARENT)
        }
    }

    private fun createHingeView(foldingFeature: FoldingFeature): View {
        val hinge = View(rootView.context)
        hinge.id = R.id.hinge_id
        foldingFeature.bounds.let {
            hinge.layoutParams = FrameLayout.LayoutParams(it.width(), it.height())
        }
        hinge.background = ColorDrawable(ContextCompat.getColor(context, R.color.black))
        return hinge
    }
}
