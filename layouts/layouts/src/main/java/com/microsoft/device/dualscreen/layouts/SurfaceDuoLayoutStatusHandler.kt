/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layouts

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.VERTICAL
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import com.microsoft.device.dualscreen.ScreenMode

/**
 * Class responsible for the logic of displaying the layout containers depending on the screen state.
 * The class automatically handles the resize of the layout containers if the device rotates and
 * also the position of the hinge.
 */
internal class SurfaceDuoLayoutStatusHandler internal constructor(
    private val context: Context,
    private val rootView: SurfaceDuoLayout,
    private val surfaceDuoLayoutConfig: SurfaceDuoLayout.Config
) {
    private var screenMode = ScreenMode.SINGLE_SCREEN
    private var state: ScreenSavedState? = null

    fun addViewsDependingOnSpanningMode(savedState: ScreenSavedState) {
        state = savedState
        screenMode = if (savedState.screenMode == ScreenMode.DUAL_SCREEN) {
            addDualScreenBehaviour(savedState)
            ScreenMode.DUAL_SCREEN
        } else {
            addSingleScreenBehaviour(savedState)
            ScreenMode.SINGLE_SCREEN
        }
    }

    /**
     * Called when the activity handles a configuration change.
     *
     * The function will take the containers inside SurfaceDuoLayout
     * and change the width and height of them according to new [Configuration] and new [ScreenSavedState]
     * @param surfaceDuoLayout The host view
     * @param newConfig The host activity configuration
     * @param newState Contains the screen information like spanning mode and so on
     */
    internal fun onConfigurationChanged(
        surfaceDuoLayout: SurfaceDuoLayout,
        newConfig: Configuration?,
        newState: ScreenSavedState
    ) {
        newConfig?.let {
            if (it.orientation != Configuration.ORIENTATION_UNDEFINED) {
                refreshContent(surfaceDuoLayout, newConfig, newState)
            } else {
                Log.d(
                    SurfaceDuoLayoutStatusHandler::class.java.name,
                    "New configuration orientation is undefined"
                )
            }
        }
    }

    private fun setLayoutOrientation(newConfig: Configuration) {
        rootView.orientation = when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> HORIZONTAL
            Configuration.ORIENTATION_PORTRAIT -> VERTICAL
            else -> HORIZONTAL
        }
    }

    private fun inDualScreenOrientationChange(currentScreenModeIsDualScreen: Boolean): Boolean {
        return currentScreenModeIsDualScreen && screenMode == ScreenMode.DUAL_SCREEN
    }

    private fun inSingleScreenOrientationChange(currentScreenModeIsDualScreen: Boolean): Boolean {
        return currentScreenModeIsDualScreen.not() && screenMode == ScreenMode.SINGLE_SCREEN
    }

    private fun inTransitionFromSingleScreenToDualScreen(currentScreenModeIsDualScreen: Boolean): Boolean {
        return currentScreenModeIsDualScreen && screenMode == ScreenMode.SINGLE_SCREEN
    }

    private fun inTransitionFromDualScreenToSingleScreen(currentScreenModeIsDualScreen: Boolean): Boolean {
        return currentScreenModeIsDualScreen.not() && screenMode == ScreenMode.DUAL_SCREEN
    }

    private fun inDualLandscapeSingleContainer(): Boolean =
        rootView.orientation == VERTICAL && surfaceDuoLayoutConfig.isDualLandscapeSingleContainer

    private fun inDualPortraitSingleContainer(): Boolean =
        rootView.orientation == HORIZONTAL && surfaceDuoLayoutConfig.isDualPortraitSingleContainer

    private fun addHingeAndSecondContainer() {
        // Create Hinge and EndContainer
        val hinge = createHingeView()
        val dualScreenEndContainer = FrameLayout(rootView.context)
        dualScreenEndContainer.id = R.id.second_container_id

        // Set orientation and containers dimensions
        state?.screenRectangles?.let { screenRectList ->
            setDualScreenContainersDimensions(
                rootView.orientation,
                rootView.findViewById(R.id.first_container_id),
                dualScreenEndContainer,
                screenRectList[0],
                screenRectList[1]
            )
        }

        // Inflate view in EndContainer
        surfaceDuoLayoutConfig.dualScreenEndLayoutId
            .takeIf { it != View.NO_ID }?.let {
                dualScreenEndContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(surfaceDuoLayoutConfig.dualScreenEndLayoutId, this.rootView, false)
                )
            }

        rootView.addView(hinge)
        rootView.addView(dualScreenEndContainer)
        screenMode = ScreenMode.DUAL_SCREEN
    }

    private fun removeHingeAndSecondContainer() {
        rootView.removeView(rootView.findViewById(R.id.hinge_id))
        rootView.removeView(rootView.findViewById(R.id.second_container_id))
        rootView.findViewById<FrameLayout>(R.id.first_container_id).layoutParams =
            LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        screenMode = ScreenMode.SINGLE_SCREEN
    }

    /**
     * Refresh the content for [SurfaceDuoLayout] depending on new activity [Configuration] and new [ScreenSavedState] screen info
     */
    private fun refreshContent(
        surfaceDuoLayout: SurfaceDuoLayout,
        newConfig: Configuration,
        newState: ScreenSavedState
    ) {
        val setupNewState = { state = newState }
        val currentScreenModeIsDualScreen = newState.screenMode == ScreenMode.DUAL_SCREEN
        setLayoutOrientation(newConfig)

        when {
            inDualScreenOrientationChange(currentScreenModeIsDualScreen) -> {
                setupNewState()
                refreshDualScreenContainers(surfaceDuoLayout)
            }
            inSingleScreenOrientationChange(currentScreenModeIsDualScreen) -> {
                setupNewState()
                addSingleScreenBehaviourOrRefreshContent(surfaceDuoLayout, newState)
            }
            inTransitionFromSingleScreenToDualScreen(currentScreenModeIsDualScreen) -> {
                setupNewState()
                if (inDualLandscapeSingleContainer() || inDualPortraitSingleContainer()) {
                    addDualScreenBehaviourOrRefreshContent(surfaceDuoLayout, newState)
                } else {
                    addHingeAndSecondContainer()
                }
            }
            inTransitionFromDualScreenToSingleScreen(currentScreenModeIsDualScreen) -> {
                setupNewState()
                removeHingeAndSecondContainer()
            }
            else -> {
                Log.d(
                    SurfaceDuoLayoutStatusHandler::class.java.name,
                    "New Screen configuration is undefined"
                )
            }
        }
    }

    /**
     * Contains the logic for re-inflate of the orientation transitions:
     *  - Single-Container orientation -> Single-Container orientation
     *  - Dual-Container orientation -> Single-Container orientation
     *  - Single-Container orientation -> Dual-Container orientation
     *  - Dual-Container orientation -> Dual-Container orientation
     */
    private fun refreshDualScreenContainers(surfaceDuoLayout: SurfaceDuoLayout) {
        if (inDualLandscapeSingleContainer() || inDualPortraitSingleContainer()) {
            rootView.removeView(rootView.findViewById(R.id.hinge_id))
            rootView.removeView(rootView.findViewById(R.id.second_container_id))
            rootView.findViewById<FrameLayout>(R.id.first_container_id).layoutParams =
                LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        } else {
            rootView.findViewById<FrameLayout>(R.id.second_container_id)?.let {
                refreshDualContainersState(surfaceDuoLayout)
            } ?: run {
                addHingeAndSecondContainer()
            }
        }
    }

    private fun refreshDualContainersState(surfaceDuoLayout: SurfaceDuoLayout) {
        // Find Hinge and add new width and height
        val hinge = surfaceDuoLayout.findViewById<View>(R.id.hinge_id)
        state?.hingeRect?.let { hingeRectangle ->
            hinge.layoutParams =
                LinearLayout.LayoutParams(hingeRectangle.width(), hingeRectangle.height())
        }

        // Set new Orientation
        when (surfaceDuoLayout.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                refreshDualPortraitContainersDimensions(surfaceDuoLayout)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                refreshDualLandscapeContainersDimensions(surfaceDuoLayout)
            }
            else -> {
            }
        }
    }

    private fun refreshDualPortraitContainersDimensions(surfaceDuoLayout: SurfaceDuoLayout) {
        surfaceDuoLayout.orientation = HORIZONTAL

        state?.screenRectangles?.let { screenRectList ->
            // Find StartLayoutContainer and add new width and height
            val start = surfaceDuoLayout.findViewById<FrameLayout>(R.id.first_container_id)
            start.layoutParams = LinearLayout.LayoutParams(screenRectList[0].width(), MATCH_PARENT)

            // Find EndLayoutContainer and add new width and height
            val end = surfaceDuoLayout.findViewById<FrameLayout>(R.id.second_container_id)
            end.layoutParams = LinearLayout.LayoutParams(screenRectList[1].width(), MATCH_PARENT)
        }
    }

    private fun refreshDualLandscapeContainersDimensions(surfaceDuoLayout: SurfaceDuoLayout) {
        surfaceDuoLayout.orientation = VERTICAL

        // Find StartLayoutContainer and add new width and height
        val startContainer = surfaceDuoLayout.findViewById<FrameLayout>(R.id.first_container_id)
        val location = rootView.locationOnScreen
        val startContainerHeight = (state?.hingeRect?.top ?: 0) - location.y
        startContainer.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, startContainerHeight)

        // Find EndLayoutContainer and add new width and height
        val end = surfaceDuoLayout.findViewById<FrameLayout>(R.id.second_container_id)
        end.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * Single screen behaviour function.
     *
     * It will create a layout container, add the inflated view to it
     * and then add the container to the root view.
     */
    private fun addSingleScreenBehaviour(savedState: ScreenSavedState) {
        rootView.removeAllViews()

        val singleScreenContainer = createSingleContainer(R.id.first_container_id)
        if (surfaceDuoLayoutConfig.singleScreenLayoutId != View.NO_ID) {
            singleScreenContainer.addView(
                LayoutInflater.from(context)
                    .inflate(surfaceDuoLayoutConfig.singleScreenLayoutId, this.rootView, false)
            )
        }

        rootView.addView(singleScreenContainer)
    }

    private fun addSingleScreenBehaviourOrRefreshContent(
        surfaceDuoLayout: SurfaceDuoLayout,
        state: ScreenSavedState
    ) {
        if (surfaceDuoLayoutConfig.singleScreenLayoutId != View.NO_ID) {
            addSingleScreenBehaviour(state)
        } else {
            surfaceDuoLayout.findViewById<FrameLayout>(R.id.first_container_id).requestLayout()
        }
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
    private fun addDualScreenBehaviour(state: ScreenSavedState) {
        rootView.removeAllViews()
        when (state.orientation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> dualPortraitLogic(state)
            Surface.ROTATION_90, Surface.ROTATION_270 -> dualLandscapeLogic(state)
        }
    }

    private fun addDualScreenBehaviourOrRefreshContent(
        surfaceDuoLayout: SurfaceDuoLayout,
        state: ScreenSavedState
    ) {
        if (surfaceDuoLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID ||
            surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID
        ) {
            addDualScreenBehaviour(state)
        } else {
            surfaceDuoLayout.findViewById<FrameLayout>(R.id.first_container_id).requestLayout()
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
    private fun dualPortraitLogic(state: ScreenSavedState) {
        if (surfaceDuoLayoutConfig.isDualPortraitSingleContainer ||
            surfaceDuoLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID
        ) {
            val singleContainer = createSingleContainer(R.id.first_container_id)
            if (surfaceDuoLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID) {
                LayoutInflater.from(context)
                    .inflate(
                        surfaceDuoLayoutConfig.dualPortraitSingleLayoutId,
                        this.rootView,
                        false
                    )?.let {
                        if (!surfaceDuoLayoutConfig.isDualPortraitSingleContainer) {
                            surfaceDuoLayoutConfig.isDualPortraitSingleContainer = true
                        }
                        singleContainer.addView(it)
                    }
            }
            rootView.addView(singleContainer)
        } else {
            state.screenRectangles?.let { screenRectList ->
                addDualScreenContainersAndViews(HORIZONTAL, screenRectList[0], screenRectList[1])
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
    private fun dualLandscapeLogic(state: ScreenSavedState) {
        if (surfaceDuoLayoutConfig.isDualLandscapeSingleContainer ||
            surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID
        ) {
            val singleContainer = createSingleContainer(R.id.first_container_id)
            if (surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID) {
                LayoutInflater.from(context)
                    .inflate(
                        surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId,
                        this.rootView,
                        false
                    )?.let {
                        if (!surfaceDuoLayoutConfig.isDualLandscapeSingleContainer) {
                            surfaceDuoLayoutConfig.isDualLandscapeSingleContainer = true
                        }
                        singleContainer.addView(it)
                    }
            }
            rootView.addView(singleContainer)
        } else {
            state.screenRectangles?.let { screenRectList ->
                addDualScreenContainersAndViews(VERTICAL, screenRectList[0], screenRectList[1])
            }
        }
    }

    private fun addDualScreenContainersAndViews(
        linearLayoutOrientation: Int,
        screenRectStart: Rect,
        screenRectEnd: Rect
    ) {
        rootView.orientation = linearLayoutOrientation

        // Hinge
        val hinge = createHingeView()
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
            screenRectEnd
        )

        surfaceDuoLayoutConfig.dualScreenStartLayoutId
            .takeIf { it != View.NO_ID }?.let {
                dualScreenStartContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(surfaceDuoLayoutConfig.dualScreenStartLayoutId, rootView, false)
                )
            }
        surfaceDuoLayoutConfig.dualScreenEndLayoutId
            .takeIf { it != View.NO_ID }?.let {
                dualScreenEndContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(surfaceDuoLayoutConfig.dualScreenEndLayoutId, rootView, false)
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
        screenRectEnd: Rect
    ) {
        if (linearLayoutOrientation == VERTICAL) {
            // DUAL_LANDSCAPE
            dualScreenStartContainer.layoutParams =
                LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            dualScreenEndContainer.layoutParams =
                LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            rootView.doOnNextLayout {
                val location = rootView.locationOnScreen
                val dualScreenStartContainerHeight = (state?.hingeRect?.top ?: 0) - location.y
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

    private fun createHingeView(): View {
        val hinge = View(rootView.context)
        hinge.id = R.id.hinge_id
        state?.hingeRect?.let {
            hinge.layoutParams = FrameLayout.LayoutParams(it.width(), it.height())
        }
        hinge.background = ColorDrawable(ContextCompat.getColor(context, R.color.black))
        return hinge
    }
}
