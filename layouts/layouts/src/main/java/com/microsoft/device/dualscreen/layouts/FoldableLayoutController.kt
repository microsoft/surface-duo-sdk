/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import androidx.window.layout.FoldingFeature
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.getScreenRectangles
import com.microsoft.device.dualscreen.utils.wm.getWindowRect
import com.microsoft.device.dualscreen.utils.wm.hasChild
import com.microsoft.device.dualscreen.utils.wm.locationOnScreen
import com.microsoft.device.dualscreen.utils.wm.normalizeWindowRect

const val OCCLUSION_THRESHOLD = 1

/**
 * Class responsible for the logic of displaying the layout containers depending on the screen state.
 * The class automatically handles the resize of the layout containers if the device rotates and
 * also the position of the hinge.
 */
internal class FoldableLayoutController constructor(
    private val rootView: FoldableLayout,
    private var layoutConfig: FoldableLayout.Config
) {
    private val firstContainer = FrameLayout(rootView.context).apply {
        id = R.id.first_container_id
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }.also {
        rootView.addView(it)
    }
    private val secondContainer = FrameLayout(rootView.context).apply {
        id = R.id.second_container_id
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
    }.also {
        rootView.addView(it)
    }

    var foldingFeature: FoldingFeature? = null
        set(value) {
            field = value
            addContent(value.screenMode)
        }

    internal val isChangingContent: Boolean
        get() = _isChangingContent
    private var _isChangingContent = false
    private val contentChangedListeners = mutableListOf<FoldableLayout.ContentChangedListener?>()

    internal fun addContentChangedListener(listener: FoldableLayout.ContentChangedListener?) {
        contentChangedListeners.add(listener)
    }

    internal fun removeContentChangedListener(listener: FoldableLayout.ContentChangedListener?) {
        contentChangedListeners.remove(listener)
    }

    private fun contentChanged() {
        contentChangedListeners.forEach { it?.contentChanged() }
        _isChangingContent = false
    }

    /**
     * Add content depending on the given [ScreenMode]
     * @param screenMode The given [ScreenMode]
     */
    private fun addContent(screenMode: ScreenMode) {
        when (screenMode) {
            ScreenMode.SINGLE_SCREEN -> addSingleScreenContent()
            ScreenMode.DUAL_SCREEN -> addDualScreenContent()
        }
    }

    /**
     * Called when a new configuration change was requested.
     *
     * The function will take the containers inside [FoldableLayout]
     * and change the width and height of them according to new [FoldableLayout.Config]
     * @param newConfig The new configuration
     */
    internal fun changeConfiguration(
        newConfig: FoldableLayout.Config,
    ) {
        layoutConfig = newConfig
        addContent(foldingFeature.screenMode)
    }

    /**
     * Inflate content for the single screen mode and add it to the first container.
     */
    private fun addSingleScreenContent() {
        if (layoutConfig.singleScreenLayoutId != View.NO_ID) {
            firstContainer.addContent(layoutConfig.singleScreenLayoutId)
        }
        removeHinge()
        updateDimensionsForSingleScreen()
    }

    /**
     * Inflate content for the dual screen mode and add it to the first and second container.
     */
    private fun addDualScreenContent() {
        when (foldingFeature?.orientation) {
            FoldingFeature.Orientation.HORIZONTAL -> addDualLandscapeContent()
            FoldingFeature.Orientation.VERTICAL -> addDualPortraitContent()
        }
    }

    /**
     * Contains the logic for:
     * 3) (dual screen portrait) dual landscape - single container
     * 4) (dual screen portrait) dual landscape - dual container
     *
     * Depending on the configuration,
     * this function will add the content for the first container or for both containers.
     */
    private fun addDualLandscapeContent() {
        if (layoutConfig.isDualLandscapeSingleContainer ||
            layoutConfig.dualLandscapeSingleLayoutId != View.NO_ID
        ) {
            if (layoutConfig.dualLandscapeSingleLayoutId != View.NO_ID) {
                firstContainer.addContent(layoutConfig.dualLandscapeSingleLayoutId)

                if (!layoutConfig.isDualLandscapeSingleContainer) {
                    layoutConfig.isDualLandscapeSingleContainer = true
                }
            }

            removeHinge()
            updateDimensionsForSingleScreen()
        } else {
            val windowRect =
                normalizeWindowRect(
                    foldingFeature?.bounds,
                    rootView.context.getWindowRect(),
                    VERTICAL
                )
            getScreenRectangles(
                foldingFeature?.bounds,
                windowRect
            )?.let { screenRectList ->
                addDualScreenContent(
                    VERTICAL,
                    screenRectList[0]
                )
            }
        }
    }

    /**
     * Contains the logic for:
     * 1) (dual screen landscape) dual portrait - single container
     * 2) (dual screen landscape) dual portrait - dual container
     *
     * Depending on the configuration,
     * this function will add the content for the first container or for both containers.
     */
    private fun addDualPortraitContent() {
        if (layoutConfig.isDualPortraitSingleContainer ||
            layoutConfig.dualPortraitSingleLayoutId != View.NO_ID
        ) {
            if (layoutConfig.dualPortraitSingleLayoutId != View.NO_ID) {
                firstContainer.addContent(layoutConfig.dualPortraitSingleLayoutId)

                if (!layoutConfig.isDualPortraitSingleContainer) {
                    layoutConfig.isDualPortraitSingleContainer = true
                }
            }

            removeHinge()
            updateDimensionsForSingleScreen()
        } else {
            val windowRect =
                normalizeWindowRect(
                    foldingFeature?.bounds,
                    rootView.context.getWindowRect(),
                    HORIZONTAL
                )
            getScreenRectangles(
                foldingFeature?.bounds,
                windowRect
            )?.let { screenRectList ->
                addDualScreenContent(
                    HORIZONTAL,
                    screenRectList[0]
                )
            }
        }
    }

    /**
     * Inflate content and add it to both containers.
     *
     * @param layoutOrientation The [FoldableLayout] orientation
     * @param startScreenRect The bounds for the first display area
     */
    private fun addDualScreenContent(
        layoutOrientation: Int,
        startScreenRect: Rect
    ) {
        rootView.orientation = layoutOrientation
        updateDimensionsForDualScreen(
            layoutOrientation,
            startScreenRect
        )

        layoutConfig.dualScreenStartLayoutId
            .takeIf { it != View.NO_ID }?.let { resLayoutId ->
                firstContainer.addContent(resLayoutId)
            }
        layoutConfig.dualScreenEndLayoutId
            .takeIf { it != View.NO_ID }?.let { resLayoutId ->
                secondContainer.addContent(resLayoutId)
            }

        addHingeIfNeeded()
    }

    /**
     * Add the hinge view if it's not already added
     */
    private fun addHingeIfNeeded() {
        if (!rootView.hasChild(R.id.hinge_id)) {
            foldingFeature?.generateHingeView(rootView.context)?.let { hingeView ->
                rootView.addView(hingeView, 1)
            }
        }
    }

    /**
     * Removes the hinge view from [FoldableLayout] root view.
     */
    private fun removeHinge() {
        rootView.findViewById<View>(R.id.hinge_id)?.let {
            rootView.removeView(it)
        }
    }

    /**
     * Updates the containers dimensions corresponding to the single screen mode
     */
    private fun updateDimensionsForSingleScreen() {
        firstContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        secondContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = WRAP_CONTENT
            height = MATCH_PARENT
        }
        contentChanged()
    }

    /**
     * Updates the containers dimensions corresponding to the dual screen mode and layout orientation
     *
     * @param linearLayoutOrientation The [FoldableLayout] orientation
     * @param startScreenRect The bounds for the first display area
     */
    private fun updateDimensionsForDualScreen(
        linearLayoutOrientation: Int,
        startScreenRect: Rect
    ) {
        if (linearLayoutOrientation == VERTICAL) {
            resetContainersDimensions()

            rootView.doOnNextLayout {
                updateDimensionsForDualLandscape(startScreenRect)
            }
        } else {
            rootView.doOnNextLayout {
                updateDimensionsForDualPortrait(startScreenRect)
            }
        }
    }

    /**
     * Updates the containers dimensions corresponding to the dual screen mode in portrait orientation
     *
     * @param startScreenRect The bounds for the first screen
     */
    private fun updateDimensionsForDualPortrait(
        startScreenRect: Rect
    ) {
        val leftPositionOnScreen = rootView.locationOnScreen.x
        firstContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = startScreenRect.width() - leftPositionOnScreen
            height = MATCH_PARENT
        }
        secondContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        contentChanged()
    }

    /**
     * Updates the containers dimensions corresponding to the dual screen mode in landscape orientation
     */
    private fun updateDimensionsForDualLandscape(
        startScreenRect: Rect
    ) {
        foldingFeature?.let { foldingFeature ->
            val (leftPositionOnScreen, topPositionOnScreen) = rootView.locationOnScreen
            val dualScreenStartContainerHeight = foldingFeature.bounds.top - topPositionOnScreen

            firstContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                width = startScreenRect.width() - leftPositionOnScreen
                height = dualScreenStartContainerHeight
            }
            secondContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                width = MATCH_PARENT
                height = MATCH_PARENT
            }
        }
        contentChanged()
    }

    /**
     * Updates the containers dimensions to MATCH_PARENT
     */
    private fun resetContainersDimensions() {
        firstContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        secondContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
    }
}

/**
 *  Clears the content, inflate the new content and add it to the view.
 */
private fun ViewGroup.addContent(@LayoutRes layoutResId: Int): View? {
    removeAllViews()
    return LayoutInflater.from(context).inflate(layoutResId, null, false).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }.also {
        addView(it)
    }
}

/**
 * Generates the hinge view
 */
private fun FoldingFeature.generateHingeView(context: Context): View {
    return View(context).apply {
        id = R.id.hinge_id
        layoutParams = FrameLayout.LayoutParams(
            normalizeFoldingFeatureThickness(bounds.width()),
            normalizeFoldingFeatureThickness(bounds.height())
        )
        background = ColorDrawable(ContextCompat.getColor(context, R.color.black))
    }
}

/**
 * When the FoldingFeature thickness is >1px we need to keep the thickness of the FoldingFeature because, most likely, we are dealing with a hinge devices.
 * When the FoldingFeature thickness is <1px we need to set the thickness of the FoldingFeature to 0 so it will not interfere with the application UI because we are dealing with a continuously folding screen.
 * We can't remove the FoldingFeature because other components depend on it.
 */
private fun normalizeFoldingFeatureThickness(thickness: Int) =
    if (thickness <= OCCLUSION_THRESHOLD) {
        0
    } else {
        thickness
    }

/**
 * Returns [ScreenMode.SINGLE_SCREEN] when there is no folding feature, [ScreenMode.DUAL_SCREEN] otherwise.
 */
private val FoldingFeature?.screenMode: ScreenMode
    get() = if (this != null) {
        ScreenMode.DUAL_SCREEN
    } else {
        ScreenMode.SINGLE_SCREEN
    }