/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layout

import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
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
    surfaceDuoScreenManager: SurfaceDuoScreenManager,
    singleScreenLayout: Int,
    dualScreenLayoutStart: Int,
    dualScreenLayoutEnd: Int
) : ScreenModeListener {
    companion object {
        private const val STAT_BAR_SIZE = "status_bar_height"
        private const val NAV_BAR_BOTTOM_GESTURE_SIZE = "application_bar_height"
        private const val SIZE_RESOURCE_TYPE = "dimen"
        private const val DEFAULT_RESOURCE_PACKAGE = "android"
    }

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
            val resourceId: Int =  activity.resources.getIdentifier(
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
        surfaceDuoScreenManager.addScreenModeListener(this)
    }

    /**
     * Function that is called by the SurfaceDuoScreenManager when the device is in SingleScreen mode.
     * It will create a layout container, add the inflated view to it
     * and the add the container to the root view.
     */
    override fun onSwitchToSingleScreenMode() {
        rootView.removeAllViews()

        if (singleScreenView != null && singleScreenView!!.parent != null) {
            (singleScreenView!!.parent as ViewGroup).removeAllViews()
        }

        val singleScreenContainer = FrameLayout(rootView.context)
        singleScreenContainer.id = R.id.single_screen_container_id
        if (singleScreenView != null) {
            singleScreenContainer.addView(singleScreenView)
        }

        rootView.addView(singleScreenContainer)
    }

    /**
     * Function that is called by the SurfaceDuoScreenManager when the device is in SingleScreen mode.
     * It will create a start screen container, hinge view and end screen container.
     * After that the code will add the inflated views to the containers and add everything to
     * the root view.
     */
    override fun onSwitchToDualScreenMode() {
        val screenRectangles = ScreenHelper.getScreenRectangles(activity)
        val screenRect1 = screenRectangles[0]
        val screenRect2 = screenRectangles[1]

        if (!screenRect1.isEmpty && !screenRect2.isEmpty) {
            rootView.removeAllViews()

            if (dualScreenStartView != null &&
                dualScreenEndView != null &&
                dualScreenStartView!!.parent != null &&
                dualScreenEndView!!.parent != null) {
                (dualScreenStartView!!.parent as ViewGroup).removeAllViews()
                (dualScreenEndView!!.parent as ViewGroup).removeAllViews()
            }

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
        val linearLayout = LinearLayout(rootView.context)
        linearLayout.orientation = linearLayoutOrientation

        // Create Start Layout
        val spannedStartContainer = FrameLayout(rootView.context)
        spannedStartContainer.id = R.id.dual_screen_start_container_id
        if (linearLayoutOrientation == LinearLayout.VERTICAL) {
            val height = screenRect2.height() - actionbarHeight

            spannedStartContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect1.width() - navigationBarHeight,
                    height - statusBarHeight
            )
        } else {
            spannedStartContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect1.width(),
                    screenRect1.height() - navigationBarHeight - statusBarHeight)
        }
        dualScreenStartView?.let{
            spannedStartContainer.addView(it)
        }

        // Hinge
        val hinge = View(rootView.context)
        ScreenHelper.getHinge(activity)?.let {
            hinge.layoutParams = FrameLayout.LayoutParams(it.width(), it.height())
        }
        hinge.background = ColorDrawable(ContextCompat.getColor(activity.baseContext, R.color.black))

        // Create End Layout
        val spannedEndContainer = FrameLayout(rootView.context)
        spannedEndContainer.id = R.id.dual_screen_end_container_id
        if (linearLayoutOrientation == LinearLayout.VERTICAL) {
            spannedEndContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect2.width() - navigationBarHeight,
                    screenRect1.height()
            )
        } else {
            spannedEndContainer.layoutParams = FrameLayout.LayoutParams(
                    screenRect2.width(),
                    screenRect2.height() - navigationBarHeight - statusBarHeight
            )
        }
        dualScreenEndView?.let {
            spannedEndContainer.addView(it)
        }

        linearLayout.addView(spannedStartContainer)
        linearLayout.addView(hinge)
        linearLayout.addView(spannedEndContainer)
        rootView.addView(linearLayout)
    }
}
