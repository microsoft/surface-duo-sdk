/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BaseInterpolator
import androidx.customview.view.AbsSavedState
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.microsoft.device.dualscreen.DisplayPosition
import com.microsoft.device.dualscreen.OnSwipeListener
import com.microsoft.device.dualscreen.ScreenInfo
import com.microsoft.device.dualscreen.ScreenInfoListener
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.ScreenMode
import com.microsoft.device.dualscreen.createHalfTransparentBackground
import com.microsoft.device.dualscreen.isPortrait
import com.microsoft.device.dualscreen.isSpannedInDualScreen

/**
 * A sub class of the Bottom Navigation View that can position its children in different ways when the application is spanned on both screens.
 * Using the [arrangeButtons] and [displayPosition] the children can be split in any way between the two screens.
 * Animation can be used when changing the arrangement of the buttons on the two screen.
 * If one of the screens doesn't contain any button, its background can be made transparent.
 */
open class SurfaceDuoBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private var singleScreenWidth = -1
    private var totalScreenWidth = -1
    private var hingeWidth = -1

    private var screenMode = ScreenMode.DUAL_SCREEN
    private var currentScreenInfo: ScreenInfo? = null

    private var initialBackground: Drawable? = null
    private var startBtnCount: Int = -1
    private var endBtnCount: Int = -1
    private var defaultChildWidth = -1

    private var onSwipeListener: OnSwipeListener = object : OnSwipeListener(context) {
        override fun onSwipeLeft() {
            super.onSwipeLeft()
            if (allowFlingGesture) {
                displayPosition = DisplayPosition.START
            }
        }

        override fun onSwipeRight() {
            super.onSwipeRight()
            if (allowFlingGesture) {
                displayPosition = DisplayPosition.END
            }
        }
    }

    private val screenInfoListener = object : ScreenInfoListener {
        override fun onScreenInfoChanged(screenInfo: ScreenInfo) {
            currentScreenInfo = screenInfo
            setScreenParameters(screenInfo)
            tryUpdateBackground()
        }
    }

    /**
     * Determines where to display the bottom navigation buttons when the application is spanned on both screens.
     * The options are [DisplayPosition.START], [DisplayPosition.END] or [DisplayPosition.DUAL]
     */
    var displayPosition: DisplayPosition = DisplayPosition.DUAL
        set(value) {
            updateDisplayPosition(value)
            field = value
        }

    /**
     * Use an animation to move the buttons when the [displayPosition] or [arrangeButtons] is called.
     * By default the [AccelerateDecelerateInterpolator] is used.
     */
    var useAnimation: Boolean = true

    /**
     * Set the interpolator for the animation when [displayPosition] or [arrangeButtons] is called.
     * By default the [AccelerateDecelerateInterpolator] is used.
     */
    var animationInterpolator: BaseInterpolator = AccelerateDecelerateInterpolator()

    /**
     * Allows the buttons to be moved to [DisplayPosition.START] or [DisplayPosition.END] with a swipe gesture.
     */
    var allowFlingGesture: Boolean = true

    /**
     * When the application is spanned and there are no buttons on a screen, the background on that screen will be made transparent.
     */
    var useTransparentBackground: Boolean = true
        set(value) {
            field = value
            tryUpdateBackground()
        }

    init {
        extractAttributes(context, attrs)
        tryUpdateBackground()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ScreenManagerProvider.getScreenManager().removeScreenInfoListener(screenInfoListener)
    }

    private fun extractAttributes(context: Context, attrs: AttributeSet?) {
        val styledAttributes =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ScreenManagerAttrs,
                0,
                0
            )
        try {
            screenMode = ScreenMode.fromId(
                styledAttributes.getResourceId(
                    R.styleable.ScreenManagerAttrs_tools_application_mode,
                    ScreenMode.DUAL_SCREEN.ordinal
                )
            )
            displayPosition = DisplayPosition.fromId(
                styledAttributes.getInt(
                    R.styleable.ScreenManagerAttrs_display_position,
                    DisplayPosition.DUAL.ordinal
                )
            )
            useAnimation =
                styledAttributes.getBoolean(
                    R.styleable.ScreenManagerAttrs_useAnimation,
                    useAnimation
                )
            allowFlingGesture =
                styledAttributes.getBoolean(
                    R.styleable.ScreenManagerAttrs_allowFlingGesture,
                    allowFlingGesture
                )
            useTransparentBackground =
                styledAttributes.getBoolean(
                    R.styleable.ScreenManagerAttrs_useTransparentBackground,
                    useTransparentBackground
                )
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun setScreenParameters(screenInfo: ScreenInfo) {
        screenInfo.getHinge()?.let {
            singleScreenWidth = it.left
        }

        screenInfo.getWindowRect().let {
            totalScreenWidth = it.right
        }

        screenInfo.getHinge()?.let {
            hingeWidth = it.right - it.left
        }
    }

    /**
     * When the application is in spanned mode the buttons can be split between the screens.
     */
    fun arrangeButtons(startBtnCount: Int, endBtnCount: Int) {
        val child = getChildAt(0) as BottomNavigationMenuView
        if (!isSpanned() || isPortrait()) {
            if (child.doesChildCountMatch(startBtnCount, endBtnCount)) {
                syncBtnCount(startBtnCount, endBtnCount)
            }
            return
        }

        if (this.startBtnCount == startBtnCount && this.endBtnCount == endBtnCount) {
            return
        }
        if (child.doesChildCountMatch(startBtnCount, endBtnCount)) {
            syncBtnCount(startBtnCount, endBtnCount)
            child.arrangeOnScreen(0, startBtnCount - 1)
            child.arrangeOnScreen(startBtnCount, startBtnCount + endBtnCount - 1)
            tryUpdateBackground()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!isSpanned() || isPortrait()) {
            return
        }

        val child = getChildAt(0) as BottomNavigationMenuView
        if (child.doesChildCountMatch(startBtnCount, endBtnCount)) {
            child.layout(0, 0, this.width, this.height)
            child.redoLayout(startBtnCount, endBtnCount)
        }
    }

    private fun BottomNavigationMenuView.redoLayout(firstScreen: Int, secondScreen: Int) {
        arrangeOnScreen(0, firstScreen - 1)
        arrangeOnScreen(firstScreen, firstScreen + secondScreen - 1)
    }

    private fun BottomNavigationMenuView.arrangeOnScreen(firstBtnIndex: Int, secondBtnIndex: Int) {
        if (width != totalScreenWidth) {
            layout(0, 0, totalScreenWidth, (parent as SurfaceDuoBottomNavigationView).height)
        }

        val buttonsCount = secondBtnIndex - firstBtnIndex + 1
        if (buttonsCount == 0) {
            return
        }
        val startPoint = if (firstBtnIndex != 0 || displayPosition == DisplayPosition.END) {
            singleScreenWidth + hingeWidth
        } else {
            0
        }

        if (defaultChildWidth == -1) {
            defaultChildWidth = getChildAt(0).measuredWidth
        }
        val screenWidth = singleScreenWidth

        val childWidth = if (buttonsCount * defaultChildWidth > screenWidth) {
            screenWidth / buttonsCount
        } else {
            defaultChildWidth
        }
        val childMargin = (screenWidth - childWidth * buttonsCount) / (buttonsCount + 1)

        val skipAnimation = shouldSkipAnimation(this)
        for (i in firstBtnIndex..secondBtnIndex) {
            val btnNoOnScreen = i - firstBtnIndex
            val child = getChildAt(i)
            val childLeft =
                startPoint + btnNoOnScreen * childWidth + (btnNoOnScreen + 1) * childMargin

            child.left = 0
            setChildLayout(child, child.left, childWidth)
            if (skipAnimation || !useAnimation || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                child.translationX = childLeft.toFloat()
            } else {
                child.animate()
                    .setInterpolator(animationInterpolator)
                    .translationX(childLeft.toFloat())
            }
        }
    }

    private fun BottomNavigationMenuView.doesChildCountMatch(
        startBtnCount: Int,
        endBtnCount: Int
    ): Boolean {
        return startBtnCount >= 0 && endBtnCount >= 0 && childCount == startBtnCount + endBtnCount
    }

    /**
     * Skip the animation on the first layout pass.
     */
    private fun shouldSkipAnimation(menu: BottomNavigationMenuView): Boolean {
        for (i in 0 until menu.childCount) {
            if (menu.getChildAt(i).left != 0) {
                return true
            }
        }
        return false
    }

    private fun setChildLayout(child: View, left: Int, childWidth: Int) {
        child.layout(left, child.top, left + childWidth, child.bottom)
    }

    private fun updateDisplayPosition(newPosition: DisplayPosition) {
        if (!isSpanned() || isPortrait()) {
            return
        }

        if (displayPosition == newPosition) {
            return
        }
        val btnCount = (getChildAt(0) as BottomNavigationMenuView).childCount

        if (newPosition == DisplayPosition.START) {
            arrangeButtons(btnCount, 0)
        }
        if (newPosition == DisplayPosition.END) {
            arrangeButtons(0, btnCount)
        }
    }

    /**
     * Synchronize the [displayPosition] with the [startBtnCount] and [endBtnCount].
     */
    private fun syncBtnCount(startBtnCount: Int, endBtnCount: Int) {
        this.startBtnCount = startBtnCount
        this.endBtnCount = endBtnCount

        if (startBtnCount == 0 && endBtnCount != 0) {
            displayPosition = DisplayPosition.END
        }

        if (endBtnCount == 0 && startBtnCount != 0) {
            displayPosition = DisplayPosition.START
        }

        if (startBtnCount > 0 && endBtnCount > 0) {
            displayPosition = DisplayPosition.DUAL
        }
    }

    private fun isSpanned(): Boolean {
        return currentScreenInfo?.let {
            isSpannedInDualScreen(screenMode, it)
        } ?: kotlin.run { false }
    }

    private fun hasButtonsOnBothScreens(): Boolean {
        return startBtnCount > 0 && endBtnCount > 0
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!allowFlingGesture) {
            return super.onInterceptTouchEvent(ev)
        }
        onSwipeListener.onTouchEvent(ev)

        if (onSwipeListener.onInterceptTouchEvent(ev)) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (!allowFlingGesture) {
            return super.onTouchEvent(ev)
        }
        return onSwipeListener.onTouchEvent(ev)
    }

    override fun setBackground(background: Drawable?) {
        if (initialBackground == null) {
            initialBackground = background
        }
        super.setBackground(background)
    }

    private fun tryUpdateBackground() {
        if (!isSpanned() || isPortrait() ||
            childCount != 1 || !useTransparentBackground || hasButtonsOnBothScreens()
        ) {
            if (background != initialBackground) {
                background = initialBackground
            }
        } else {
            if (displayPosition == DisplayPosition.DUAL) {
                background = initialBackground
            } else {
                background = createHalfTransparentBackground(
                    displayPosition,
                    currentScreenInfo,
                    initialBackground
                )
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        superState?.let {
            val state = SavedState(superState)
            state.useAnimation = this.useAnimation
            state.allowFlingGesture = this.allowFlingGesture
            state.useTransparentBackground = this.useTransparentBackground
            state.displayPosition = this.displayPosition
            state.startBtnCount = this.startBtnCount
            state.endBtnCount = this.endBtnCount
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                this.useAnimation = state.useAnimation
                this.allowFlingGesture = state.allowFlingGesture
                this.useTransparentBackground = state.useTransparentBackground
                this.displayPosition = state.displayPosition
                this.startBtnCount = state.startBtnCount
                this.endBtnCount = state.endBtnCount
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    internal class SavedState : AbsSavedState {
        var useAnimation: Boolean = true
        var allowFlingGesture: Boolean = true
        var useTransparentBackground: Boolean = true
        var displayPosition: DisplayPosition = DisplayPosition.DUAL
        var startBtnCount: Int = -1
        var endBtnCount: Int = -1

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            useAnimation = source.readInt() == 1
            allowFlingGesture = source.readInt() == 1
            useTransparentBackground = source.readInt() == 1
            displayPosition = DisplayPosition.fromId(source.readInt())
            startBtnCount = source.readInt()
            endBtnCount = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (useAnimation) 1 else 0)
            out.writeInt(if (allowFlingGesture) 1 else 0)
            out.writeInt(if (useTransparentBackground) 1 else 0)
            out.writeInt(displayPosition.id)
            out.writeInt(startBtnCount)
            out.writeInt(endBtnCount)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> =
                object : Parcelable.ClassLoaderCreator<SavedState> {
                    override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                        return SavedState(source, loader)
                    }

                    override fun createFromParcel(source: Parcel): SavedState {
                        return SavedState(source, null)
                    }

                    override fun newArray(size: Int): Array<SavedState> {
                        return newArray(size)
                    }
                }
        }
    }
}