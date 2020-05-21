/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.microsoft.device.surfaceduo.display.R

/**
 * Class responsible for the logic of displaying the layout containers depending on the screen state.
 * The class automatically handles the resize of the layout containers if the device rotates and
 * also the position of the hinge.
 */
internal class SurfaceDuoLayoutStatusHandler internal constructor(
    private val activity: Activity,
    private val rootView: SurfaceDuoLayout,
    private val surfaceDuoLayoutConfig: SurfaceDuoLayout.Config
) {
    private var screenMode = ScreenMode.NOT_DEFINED

    private var singleScreenView: View? = null
    private var dualScreenStartView: View? = null
    private var dualScreenEndView: View? = null
    private var dualPortraitSingleLayoutView: View? = null
    private var dualLandscapeSingleLayoutView: View? = null

    /**
     * On initializing the class object the code will inflate the layout resources
     * and start to create the behaviour of SurfaceDuoLayout.
     */
    init {
        if (surfaceDuoLayoutConfig.singleScreenLayoutId != View.NO_ID) {
            singleScreenView = LayoutInflater.from(activity)
                .inflate(surfaceDuoLayoutConfig.singleScreenLayoutId, this.rootView, false)
        }
        if (surfaceDuoLayoutConfig.dualScreenStartLayoutId != View.NO_ID) {
            dualScreenStartView = LayoutInflater.from(activity)
                .inflate(surfaceDuoLayoutConfig.dualScreenStartLayoutId, this.rootView, false)
        }
        if (surfaceDuoLayoutConfig.dualScreenEndLayoutId != View.NO_ID) {
            dualScreenEndView = LayoutInflater.from(activity)
                .inflate(surfaceDuoLayoutConfig.dualScreenEndLayoutId, this.rootView, false)
        }
        if (surfaceDuoLayoutConfig.dualPortraitSingleLayoutId != View.NO_ID) {
            dualPortraitSingleLayoutView = LayoutInflater.from(activity)
                .inflate(surfaceDuoLayoutConfig.dualPortraitSingleLayoutId, this.rootView, false)
        }
        if (surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId != View.NO_ID) {
            dualLandscapeSingleLayoutView = LayoutInflater.from(activity)
                .inflate(surfaceDuoLayoutConfig.dualLandscapeSingleLayoutId, this.rootView, false)
        }
        addViewsDependingOnScreenMode()

    }

    private fun addViewsDependingOnScreenMode() {
        screenMode = if (ScreenHelper.isDualMode(activity)) {
            addDualScreenBehaviour()
            ScreenMode.DUAL_SCREEN
        } else {
            addSingleScreenBehaviour()
            ScreenMode.SINGLE_SCREEN
        }
    }

    /**
     * Called when the activity handles a configuration change.
     *
     * The function will take the containers inside SurfaceDuoLayout
     * and change the width and height of them accordingly.
     */
    internal fun onConfigurationChanged(
        surfaceDuoLayout: SurfaceDuoLayout,
        newConfig: Configuration?
    ) {
        newConfig?.let {
            if (it.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (ScreenHelper.isDualMode(surfaceDuoLayout.context as Activity) &&
                screenMode == ScreenMode.DUAL_SCREEN) {
                    // DOUBLE_LANDSCAPE
                    if (surfaceDuoLayoutConfig.isDualLandscapeSingleContainer ||
                        dualLandscapeSingleLayoutView != null) {
                        surfaceDuoLayout.updateConfigCreator().reInflate()
                    } else {
                        if (surfaceDuoLayoutConfig.isDualPortraitSingleContainer) {
                            surfaceDuoLayout.updateConfigCreator().reInflate()
                        } else {
                            refreshDualScreenContainersState(surfaceDuoLayout)
                        }
                    }
                } else if (screenMode == ScreenMode.SINGLE_SCREEN) {
                    surfaceDuoLayout.findViewById<FrameLayout>(R.id.single_screen_container_id)
                        .requestLayout()
                } else {
                    Log.d(
                        SurfaceDuoLayoutStatusHandler::class.java.name,
                        "Screen mode is undefined"
                    )
                }
            } else if (it.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (ScreenHelper.isDualMode(surfaceDuoLayout.context as Activity) &&
                    screenMode == ScreenMode.DUAL_SCREEN) {
                    // DOUBLE_PORTRAIT
                    if (surfaceDuoLayoutConfig.isDualPortraitSingleContainer ||
                        dualPortraitSingleLayoutView != null) {
                        surfaceDuoLayout.updateConfigCreator().reInflate()
                    } else {
                        if (surfaceDuoLayoutConfig.isDualLandscapeSingleContainer) {
                            surfaceDuoLayout.updateConfigCreator().reInflate()
                        } else {
                            refreshDualScreenContainersState(surfaceDuoLayout)
                        }
                    }
                } else if (screenMode == ScreenMode.SINGLE_SCREEN) {
                    surfaceDuoLayout.findViewById<FrameLayout>(R.id.single_screen_container_id)
                        .requestLayout()
                } else {
                    Log.d(
                        SurfaceDuoLayoutStatusHandler::class.java.name,
                        "Screen mode is undefined"
                    )
                }
            } else {
                Log.d(
                    SurfaceDuoLayoutStatusHandler::class.java.name,
                    "New configuration orientation is undefined"
                )
            }
        }
    }

    private fun refreshDualScreenContainersState(surfaceDuoLayout: SurfaceDuoLayout) {
        // Get new containers dimensions
        val screenRectangles = ScreenHelper.getScreenRectangles(activity)
        val screenRectangleStart = screenRectangles[0]
        val screenRectangleEnd = screenRectangles[1]

        // Find Hinge and add new width and height
        val hinge = surfaceDuoLayout.findViewById<View>(R.id.hinge_id)
        ScreenHelper.getHinge(activity)?.let { hingeRectangle ->
            hinge.layoutParams = LinearLayout.LayoutParams(
                hingeRectangle.width(),
                hingeRectangle.height()
            )
        }

        // Set new Orientation
        when (surfaceDuoLayout.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                // DOUBLE_PORTRAIT
                surfaceDuoLayout.orientation = LinearLayout.HORIZONTAL

                // Find StartLayoutContainer and add new width and height
                val start = surfaceDuoLayout
                    .findViewById<FrameLayout>(R.id.dual_screen_start_container_id)
                start.layoutParams = LinearLayout.LayoutParams(
                    screenRectangleStart.width(),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                // Find EndLayoutContainer and add new width and height
                val end = surfaceDuoLayout
                    .findViewById<FrameLayout>(R.id.dual_screen_end_container_id)
                end.layoutParams = LinearLayout.LayoutParams(
                    screenRectangleEnd.width(),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                // DOUBLE_LANDSCAPE
                surfaceDuoLayout.orientation = LinearLayout.VERTICAL

                // Find StartLayoutContainer and add new width and height
                val start = surfaceDuoLayout
                    .findViewById<FrameLayout>(R.id.dual_screen_start_container_id)
                start.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
                ).apply {
                    weight = 1F
                }

                // Find EndLayoutContainer and add new width and height
                val end = surfaceDuoLayout
                    .findViewById<FrameLayout>(R.id.dual_screen_end_container_id)
                end.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    screenRectangleEnd.height()
                )
            }
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

        val singleScreenContainer = createSingleContainer(R.id.single_screen_container_id)
        if (singleScreenView != null) {
            singleScreenContainer.addView(singleScreenView)
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
     *
     * Contains the logic for:
     * 1) (dual screen landscape) dual portrait - single container
     * 2) (dual screen landscape) dual portrait - dual container
     * 3) (dual screen portrait) dual landscape - single container
     * 4) (dual screen portrait) dual landscape - dual container
     *
     * Depending on the configuration and the rotation of the device, the function will create
     * a single or two containers for the entire screen.
     *
     * After that the code will add the inflated views to the containers and add everything to
     * the root view.
     */
    private fun addDualScreenBehaviour() {
        val screenRectangles = ScreenHelper.getScreenRectangles(activity)
        val screenRect1 = screenRectangles[0]
        val screenRect2 = screenRectangles[1]

        if (!screenRect1.isEmpty && !screenRect2.isEmpty) {
            rootView.removeAllViews()

            when (ScreenHelper.getCurrentRotation(activity)) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    if (surfaceDuoLayoutConfig.isDualPortraitSingleContainer ||
                        dualPortraitSingleLayoutView != null) {
                        val singleContainer = createSingleContainer(
                            R.id.dual_portrait_single_container_id
                        )
                        dualPortraitSingleLayoutView?.let {
                            if (!surfaceDuoLayoutConfig.isDualPortraitSingleContainer) {
                                surfaceDuoLayoutConfig.isDualPortraitSingleContainer = true
                            }
                            singleContainer.addView(it)
                        }
                        rootView.addView(singleContainer)
                    } else {
                        addDualScreenContainersAndViews(
                            LinearLayout.HORIZONTAL,
                            screenRect1,
                            screenRect2
                        )
                    }
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    if (surfaceDuoLayoutConfig.isDualLandscapeSingleContainer ||
                            dualLandscapeSingleLayoutView != null) {
                        val singleContainer = createSingleContainer(
                            R.id.dual_landscape_single_container_id
                        )
                        dualLandscapeSingleLayoutView?.let {
                            if (!surfaceDuoLayoutConfig.isDualLandscapeSingleContainer) {
                                surfaceDuoLayoutConfig.isDualLandscapeSingleContainer = true
                            }
                            singleContainer.addView(it)
                        }
                        rootView.addView(singleContainer)
                    } else {
                        addDualScreenContainersAndViews(
                            LinearLayout.VERTICAL,
                            screenRect1,
                            screenRect2
                        )
                    }
                }
            }
        } else {
            Log.e("ScreenStatusHandler",
                    "Could NOT retrieve dual screens dimensions"
            )
        }
    }

    private fun addDualScreenContainersAndViews(
        linearLayoutOrientation: Int,
        screenRect1: Rect,
        screenRect2: Rect
    ) {
        rootView.orientation = linearLayoutOrientation

        // Hinge
        val hinge = View(rootView.context)
        hinge.id = R.id.hinge_id
        ScreenHelper.getHinge(activity)?.let {
            hinge.layoutParams = FrameLayout.LayoutParams(it.width(), it.height())
        }
        hinge.background = ColorDrawable(ContextCompat.getColor(activity.baseContext, R.color.black))

        // Start and End Layouts
        val dualScreenStartContainer = FrameLayout(rootView.context)
        dualScreenStartContainer.id = R.id.dual_screen_start_container_id
        val dualScreenEndContainer = FrameLayout(rootView.context)
        dualScreenEndContainer.id = R.id.dual_screen_end_container_id

        if (linearLayoutOrientation == LinearLayout.VERTICAL) {
            // DOUBLE_LANDSCAPE
            dualScreenStartContainer.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1F
            }
            dualScreenEndContainer.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                screenRect2.height()
            )
        } else {
            // DOUBLE_PORTRAIT
            dualScreenStartContainer.layoutParams = LinearLayout.LayoutParams(
                screenRect1.width(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            dualScreenEndContainer.layoutParams = LinearLayout.LayoutParams(
                screenRect2.width(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        dualScreenStartView?.let {
            dualScreenStartContainer.addView(it)
        }
        dualScreenEndView?.let {
            dualScreenEndContainer.addView(it)
        }

        rootView.addView(dualScreenStartContainer)
        rootView.addView(hinge)
        rootView.addView(dualScreenEndContainer)
    }
}
