/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BaseInterpolator
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.microsoft.device.dualscreen.core.DisplayPosition
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode
import com.microsoft.device.dualscreen.core.isPortrait
import com.microsoft.device.dualscreen.core.isSpannedInDualScreen

/**
 * A sub class of the Bottom Navigation View that positions it's children on the start, end or both screens when the application is spanned on both screens.
 */
open class SurfaceDuoBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private var singleScreenWidth = -1
    private var totalScreenWidth = -1
    private var hingeWidth = -1

    private var displayPosition = DisplayPosition.DUAL
    private var screenMode = ScreenMode.DUAL_SCREEN

    private var useAnimations = true
    private var animationInterpolator: BaseInterpolator = AccelerateDecelerateInterpolator()

    private var setEmptyAreaToTransparent = false
    private var initialBackground: Drawable? = null

    var surfaceDuoDisplayPosition: DisplayPosition
        get() {
            return displayPosition
        }
        set(value) {
            updateDisplayPosition(value)
        }

    var surfaceDuoUseAnimation: Boolean
        get() {
            return useAnimations
        }
        set(value) {
            useAnimations = value
        }

    var surfaceDuoAnimationInterpolator: BaseInterpolator
        get() {
            return animationInterpolator
        }
        set(value) {
            animationInterpolator = value
        }

    var surfaceDuoTransparentBackground: Boolean
        get() {
            return setEmptyAreaToTransparent
        }
        set(value) {
            setEmptyAreaToTransparent = value
            tryUpdateBackground()
        }

    init {
        getMultiScreenParameters(context)
        extractAttributes(attrs)
        tryUpdateBackground()
    }

    private fun getMultiScreenParameters(context: Context) {
        ScreenHelper.getHinge(context)?.let {
            singleScreenWidth = it.left
        }

        ScreenHelper.getWindowRect(context).let {
            totalScreenWidth = it.right
        }

        ScreenHelper.getHinge(context)?.let {
            hingeWidth = it.right - it.left
        }
    }

    private fun extractAttributes(attrs: AttributeSet?) {
        val styledAttributes =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SurfaceDuoBottomNavigationView,
                0,
                0
            )
        try {
            displayPosition = DisplayPosition.fromId(
                styledAttributes.getInt(
                    R.styleable.SurfaceDuoBottomNavigationView_display_position,
                    DisplayPosition.DUAL.ordinal
                )
            )
            screenMode = ScreenMode.fromId(
                styledAttributes.getResourceId(
                    R.styleable.SurfaceDuoBottomNavigationView_tools_application_mode,
                    ScreenMode.DUAL_SCREEN.ordinal
                )
            )
            setEmptyAreaToTransparent =
                styledAttributes.getBoolean(
                    R.styleable.SurfaceDuoBottomNavigationView_setEmptyAreaToTransparent,
                    setEmptyAreaToTransparent
                )
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun updateDisplayPosition(displayPosition: DisplayPosition) {
        if (!isSpannedInDualScreen(screenMode) || isPortrait()) {
            return
        }
        if (this.displayPosition == displayPosition) {
            return
        }

        if (!useAnimations) {
            this.displayPosition = displayPosition
            requestLayout()
            tryUpdateBackground()
            return
        }

        if (displayPosition == DisplayPosition.DUAL) {
            this.displayPosition = displayPosition
            requestLayout()
            tryUpdateBackground()
            return
        }

        animateToNewPosition(displayPosition)
        this.displayPosition = displayPosition
        tryUpdateBackground()
    }

    private fun animateToNewPosition(displayPosition: DisplayPosition) {
        getChildAt(0)?.let { buttons ->
            val xPosition = if (displayPosition == DisplayPosition.END) {
                (hingeWidth + singleScreenWidth).toFloat()
            } else {
                0f
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                buttons.translationX = xPosition
            } else {
                buttons.animate()
                    .setInterpolator(surfaceDuoAnimationInterpolator)
                    .translationX(xPosition)
            }
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
        val desiredLength = when (displayPosition) {
            DisplayPosition.START -> singleScreenWidth
            DisplayPosition.END -> singleScreenWidth
            else -> totalScreenWidth
        }

        val translationX =
            when (displayPosition) {
                DisplayPosition.START -> 0
                DisplayPosition.END -> singleScreenWidth + hingeWidth
                else -> (singleScreenWidth - hingeWidth) / 2
            }

        val child = getChildAt(0)
        val remeasure =
            child.measuredWidth != desiredLength || child.translationX != translationX.toFloat()
        if (remeasure) {
            val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom,
                child.layoutParams.height
            )
            val childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(desiredLength, MeasureSpec.EXACTLY)
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            val params = child.layoutParams as LayoutParams
            params.gravity = Gravity.START
            child.translationX = translationX.toFloat()
        }
    }

    override fun setBackground(background: Drawable?) {
        if (initialBackground == null) {
            initialBackground = background
        }
        super.setBackground(background)
    }

    private fun tryUpdateBackground() {
        if (!isSpannedInDualScreen(screenMode) || isPortrait() || childCount != 1 || !setEmptyAreaToTransparent) {
            if (background != initialBackground) {
                background = initialBackground
            }
        } else {
            updateBackground()
        }
    }

    private fun updateBackground() {
        when (displayPosition) {
            DisplayPosition.START,
            DisplayPosition.END -> {
                this.background =
                    ContextCompat.getDrawable(context, R.drawable.background_transparent)
                getChildAt(0).background = initialBackground
            }
            DisplayPosition.DUAL -> {
                this.background = initialBackground
                getChildAt(0).background =
                    ContextCompat.getDrawable(context, R.drawable.background_transparent)
            }
        }
    }
}