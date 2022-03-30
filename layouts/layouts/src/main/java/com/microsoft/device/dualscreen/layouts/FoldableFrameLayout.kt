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
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.getFoldingFeature
import com.microsoft.device.dualscreen.utils.wm.getWindowRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A wrapper layout that positions its child on the start, end or both screens when the application is spanned on both screens.
 * This class supports only one child.
 */
open class FoldableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var hingeWidth = -1

    private var singleScreenWidth = -1
    private var totalScreenWidth = -1

    private var displayPosition = DisplayPosition.DUAL
    private var screenMode = ScreenMode.DUAL_SCREEN
    private var job: Job? = null
    private var foldingFeature: FoldingFeature? = null

    var foldableDisplayPosition: DisplayPosition
        get() {
            return displayPosition
        }
        set(value) {
            displayPosition = value
            requestLayout()
        }

    init {
        extractAttributes(context, attrs)
        registerWindowInfoFlow()
    }

    private fun registerWindowInfoFlow() {
        val activity = (context as? ComponentActivity)
            ?: throw RuntimeException("Context must implement androidx.activity.ComponentActivity!")
        job = activity.lifecycleScope.launch(Dispatchers.Main) {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity)
                    .collect { info ->
                        foldingFeature = info.getFoldingFeature()
                        foldingFeature?.let {
                            setScreenParameters(it)
                        }
                    }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    private fun extractAttributes(context: Context, attrs: AttributeSet?) {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ScreenManagerAttrs, 0, 0)
        try {
            displayPosition = DisplayPosition.fromResId(
                styledAttributes.getInt(
                    R.styleable.ScreenManagerAttrs_display_position,
                    DisplayPosition.DUAL.ordinal
                )
            )
            screenMode = ScreenMode.fromId(
                styledAttributes.getResourceId(
                    R.styleable.ScreenManagerAttrs_tools_application_mode,
                    ScreenMode.DUAL_SCREEN.ordinal
                )
            )
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun setScreenParameters(foldingFeature: FoldingFeature) {
        singleScreenWidth = foldingFeature.bounds.left
        totalScreenWidth = context.getWindowRect().right
        foldingFeature.bounds.let {
            hingeWidth = it.right - it.left
        }
    }

    private fun shouldNotSplit(): Boolean {
        return foldingFeature == null || foldingFeature?.orientation == FoldingFeature.Orientation.HORIZONTAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (shouldNotSplit()) {
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
