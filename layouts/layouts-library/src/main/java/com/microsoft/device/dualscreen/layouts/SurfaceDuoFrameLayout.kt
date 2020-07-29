/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.microsoft.device.dualscreen.core.DisplayPosition
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.core.isPortrait
import com.microsoft.device.dualscreen.core.isSpannedInDualScreen

/**
 * A wrapper layout that positions it's child on the start, end or both screens when the application is spanned on both screens.
 * This class supports only one child.
 */
open class SurfaceDuoFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var hingeWidth = -1

    private var singleScreenWidth = -1
    private var totalScreenWidth = -1

    private var displayPosition = DisplayPosition.DUAL
    private var screenMode = ScreenMode.DUAL_SCREEN

    var surfaceDuoDisplayPosition: DisplayPosition
        get() {
            return displayPosition
        }
        set(value) {
            displayPosition = value
            requestLayout()
        }

    init {
        extractAttributes(context, attrs)
        setScreenParameters(context)
    }

    private fun extractAttributes(context: Context, attrs: AttributeSet?) {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SurfaceDuoFrameLayout, 0, 0)
        try {
            displayPosition = DisplayPosition.fromId(
                styledAttributes.getInt(
                    R.styleable.SurfaceDuoFrameLayout_display_position,
                    DisplayPosition.DUAL.ordinal
                )
            )
            screenMode = ScreenMode.fromId(
                styledAttributes.getResourceId(
                    R.styleable.SurfaceDuoFrameLayout_tools_application_mode,
                    ScreenMode.DUAL_SCREEN.ordinal
                )
            )
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun setScreenParameters(context: Context) {
        ScreenHelper.getHinge(context)?.let {
            hingeWidth = it.right - it.left
        }

        ScreenHelper.getHinge(context)?.let {
            singleScreenWidth = it.left
        }

        ScreenHelper.getWindowRect(context).let {
            totalScreenWidth = it.right
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!isSpannedInDualScreen(screenMode) || isPortrait()) {
            return
        }

        if (childCount == 1) {
            adjustChildMeasurements(heightMeasureSpec)
        }
    }

    private fun adjustChildMeasurements(heightMeasureSpec: Int) {
        val desiredLength = calculateDesiredLength()
        val gravity = calculateGravity()
        val child = getChildAt(0)

        if (shouldRemeasure(child, desiredLength)) {
            val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom,
                child.layoutParams.height
            )
            val childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(desiredLength, MeasureSpec.EXACTLY)
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            val params = child.layoutParams as LayoutParams
            params.gravity = gravity
        }
    }

    private fun calculateDesiredLength(): Int {
        return when (displayPosition) {
            DisplayPosition.START -> singleScreenWidth
            DisplayPosition.END -> singleScreenWidth
            else -> totalScreenWidth
        }
    }

    private fun calculateGravity(): Int {
        return when (displayPosition) {
            DisplayPosition.START -> Gravity.START
            DisplayPosition.END -> Gravity.END
            else -> Gravity.CENTER
        }
    }

    private fun shouldRemeasure(child: View, desiredLength: Int) =
        child.measuredWidth != desiredLength
}
