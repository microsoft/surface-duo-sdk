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
class SurfaceDuoLayoutStatusHandler internal constructor(
    private val activity: Activity,
    private val rootView: SurfaceDuoLayout,
    singleScreenLayout: Int,
    dualScreenLayoutStart: Int,
    dualScreenLayoutEnd: Int
) {
    companion object {
        private const val STAT_BAR_SIZE = "status_bar_height"
        private const val NAV_BAR_BOTTOM_GESTURE_SIZE = "application_bar_height"
        private const val SIZE_RESOURCE_TYPE = "dimen"
        private const val DEFAULT_RESOURCE_PACKAGE = "android"
    }

    private var screenMode = ScreenMode.SINGLE_SCREEN

    private var singleScreenView: View? = null
    private var dualScreenStartView: View? = null
    private var dualScreenEndView: View? = null

    private val actionbarHeight: Int
        get() {
            if (activity is AppCompatActivity) {
                activity.supportActionBar?.let {
                    return if (it.isShowing) {
                        val styledAttributes = activity.getTheme()
                            .obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
                        styledAttributes.getDimension(0, 0f).toInt()
                    } else { 0 }
                } ?: kotlin.run { return 0 }
            } else if (activity.actionBar != null && activity.actionBar!!.isShowing) {
                val styledAttributes =
                    activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
                return styledAttributes.getDimension(0, 0f).toInt()
            } else { return 0 }
        }

    private val statusBarHeight: Int
        get() {
            val resourceId = activity.resources
                .getIdentifier(
                    STAT_BAR_SIZE,
                    SIZE_RESOURCE_TYPE,
                    DEFAULT_RESOURCE_PACKAGE
                )
            return if (resourceId > 0) {
                activity.resources.getDimensionPixelSize(resourceId)
            } else { 0 }
        }

    private val navigationBarHeight: Int
        get() {
            val resourceId: Int = activity.resources.getIdentifier(
                NAV_BAR_BOTTOM_GESTURE_SIZE,
                SIZE_RESOURCE_TYPE,
                DEFAULT_RESOURCE_PACKAGE
                )
            return if (resourceId > 0) {
                activity.resources.getDimensionPixelSize(resourceId) / 2
            } else { 0 }
        }

    /**
     * On initializing the class object the code will inflate the layout resources
     * and add a ScreenModeListener to the SurfaceDuoScreenManager class.
     */
    init {
        if (singleScreenLayout != -1) {
            singleScreenView = LayoutInflater.from(activity)
                .inflate(singleScreenLayout, this.rootView, false)
        }
        if (dualScreenLayoutStart != -1) {
            dualScreenStartView = LayoutInflater.from(activity)
                .inflate(dualScreenLayoutStart, this.rootView, false)
        }
        if (dualScreenLayoutEnd != -1) {
            dualScreenEndView = LayoutInflater.from(activity)
                .inflate(dualScreenLayoutEnd, this.rootView, false)
        }
        addViewsDependingOnScreenMode()
    }

    private fun addViewsDependingOnScreenMode() {
        screenMode = if (ScreenHelper.isDualMode(activity)) {
            addViewsForDualScreenMode()
            ScreenMode.DUAL_SCREEN
        } else {
            addViewsForSingleScreenMode()
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
                // DOUBLE_LANDSCAPE
                if (ScreenHelper.isDualMode(surfaceDuoLayout.context as Activity) &&
                    screenMode == ScreenMode.DUAL_SCREEN) {
                    refreshDualScreenContainersState(
                        surfaceDuoLayout,
                        LinearLayout.VERTICAL,
                        R.id.dual_screen_start_container_id
                    )
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
                // DOUBLE_PORTRAIT
                if (ScreenHelper.isDualMode(surfaceDuoLayout.context as Activity) &&
                    screenMode == ScreenMode.DUAL_SCREEN) {
                    refreshDualScreenContainersState(
                        surfaceDuoLayout,
                        LinearLayout.HORIZONTAL,
                        R.id.dual_screen_end_container_id
                    )
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

    private fun refreshDualScreenContainersState(surfaceDuoLayout: SurfaceDuoLayout, orientation: Int, containerId: Int) {
        // Set new Orientation
        surfaceDuoLayout.orientation = orientation

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

        if (orientation == LinearLayout.VERTICAL) {
            // DOUBLE_LANDSCAPE

            // Find StartLayoutContainer and add new width and height
            val start = surfaceDuoLayout.findViewById<FrameLayout>(containerId)
            start.layoutParams = LinearLayout.LayoutParams(
                screenRectangleStart.width() - navigationBarHeight,
                screenRectangleStart.height() - actionbarHeight - statusBarHeight
            )

            // Find EndLayoutContainer and add new width and height
            val end = surfaceDuoLayout
                .findViewById<FrameLayout>(R.id.dual_screen_end_container_id)
            end.layoutParams = LinearLayout.LayoutParams(
                screenRectangleEnd.width() - navigationBarHeight,
                screenRectangleEnd.height()
            )
        } else if (orientation == LinearLayout.HORIZONTAL) {
            // DOUBLE_PORTRAIT

            // Find StartLayoutContainer and add new width and height
            val start = surfaceDuoLayout
                .findViewById<FrameLayout>(R.id.dual_screen_start_container_id)
            start.layoutParams = LinearLayout.LayoutParams(
                screenRectangleStart.width(),
                screenRectangleStart.height() - statusBarHeight - actionbarHeight - navigationBarHeight
            )

            // Find EndLayoutContainer and add new width and height
            val end = surfaceDuoLayout
                .findViewById<FrameLayout>(R.id.dual_screen_end_container_id)
            end.layoutParams = LinearLayout.LayoutParams(
                screenRectangleEnd.width(),
                screenRectangleEnd.height() - statusBarHeight - actionbarHeight - navigationBarHeight
            )
        }
    }

    /**
     * Function that is called by the SurfaceDuoScreenManager when the device is in SingleScreen mode.
     * It will create a layout container, add the inflated view to it
     * and the add the container to the root view.
     */
    private fun addViewsForSingleScreenMode() {
        rootView.removeAllViews()

        val singleScreenContainer = FrameLayout(rootView.context)
        singleScreenContainer.id = R.id.single_screen_container_id
        singleScreenContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        if (singleScreenView != null) {
            singleScreenContainer.addView(singleScreenView)
        }

        rootView.addView(singleScreenContainer)
    }

    /**
     * Function that is called by the SurfaceDuoScreenManager when the device is in DualScreen mode.
     * It will create a start screen container, hinge view and end screen container.
     * After that the code will add the inflated views to the containers and add everything to
     * the root view.
     */
    private fun addViewsForDualScreenMode() {
        val screenRectangles = ScreenHelper.getScreenRectangles(activity)
        val screenRect1 = screenRectangles[0]
        val screenRect2 = screenRectangles[1]

        if (!screenRect1.isEmpty && !screenRect2.isEmpty) {
            rootView.removeAllViews()

            when (ScreenHelper.getCurrentRotation(activity)) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    addDualScreens(LinearLayout.HORIZONTAL, screenRect1, screenRect2)
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    addDualScreens(LinearLayout.VERTICAL, screenRect1, screenRect2)
                }
            }
        } else {
            Log.e("ScreenStatusHandler",
                    "Could NOT retrieve dual screens dimensions"
            )
        }
    }

    private fun addDualScreens(linearLayoutOrientation: Int, screenRect1: Rect, screenRect2: Rect) {
        rootView.orientation = linearLayoutOrientation

        // Create Start Layout
        val dualScreenStartContainer = FrameLayout(rootView.context)
        dualScreenStartContainer.id = R.id.dual_screen_start_container_id
        if (linearLayoutOrientation == LinearLayout.VERTICAL) {
            dualScreenStartContainer.layoutParams = FrameLayout.LayoutParams(
                screenRect1.width() - navigationBarHeight,
                screenRect1.height() - actionbarHeight - statusBarHeight
            )
        } else {
            dualScreenStartContainer.layoutParams = FrameLayout.LayoutParams(
                screenRect1.width(),
                screenRect1.height() - statusBarHeight - actionbarHeight - navigationBarHeight
            )
        }
        dualScreenStartView?.let {
            dualScreenStartContainer.addView(it)
        }

        // Hinge
        val hinge = View(rootView.context)
        hinge.id = R.id.hinge_id
        ScreenHelper.getHinge(activity)?.let {
            hinge.layoutParams = FrameLayout.LayoutParams(it.width(), it.height())
        }
        hinge.background = ColorDrawable(ContextCompat.getColor(activity.baseContext, R.color.black))

        // Create End Layout
        val dualScreenEndContainer = FrameLayout(rootView.context)
        dualScreenEndContainer.id = R.id.dual_screen_end_container_id
        if (linearLayoutOrientation == LinearLayout.VERTICAL) {
            dualScreenEndContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect2.width() - navigationBarHeight,
                    screenRect2.height()
            )
        } else {
            dualScreenEndContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect2.width(),
                    screenRect2.height() - statusBarHeight - actionbarHeight - navigationBarHeight
            )
        }
        dualScreenEndView?.let {
            dualScreenEndContainer.addView(it)
        }

        rootView.addView(dualScreenStartContainer)
        rootView.addView(hinge)
        rootView.addView(dualScreenEndContainer)
    }
}
