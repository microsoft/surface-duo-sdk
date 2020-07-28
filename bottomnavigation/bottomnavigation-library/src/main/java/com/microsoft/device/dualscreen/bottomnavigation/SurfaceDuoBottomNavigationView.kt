/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.children
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
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
            context.theme.obtainStyledAttributes(attrs, R.styleable.SurfaceDuoBottomNavigationView, 0, 0)
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
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun setScreenParameters(context: Context) {
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!isSpannedInDualScreen(screenMode) || isPortrait()) {
            return
        }

        children.filterIsInstance(BottomNavigationMenuView::class.java).forEach { menu ->
            when (displayPosition) {
                DisplayPosition.START -> {
                    val screenWidth = (right - left - hingeWidth) / 2
                    val childLeft = (screenWidth - menu.measuredWidth) / 2
                    val childRight = childLeft + menu.measuredWidth
                    menu.layout(childLeft, 0, childRight, menu.measuredHeight)
                }
                DisplayPosition.END -> {
                    val screenWidth = (right - left - hingeWidth) / 2
                    val padding = (screenWidth - menu.measuredWidth) / 2
                    val childLeft = screenWidth + hingeWidth + padding
                    val childRight = (right - left) - padding
                    menu.layout(childLeft, 0, childRight, menu.measuredHeight)
                }
                DisplayPosition.DUAL -> {
                }
            }
        }
    }
}