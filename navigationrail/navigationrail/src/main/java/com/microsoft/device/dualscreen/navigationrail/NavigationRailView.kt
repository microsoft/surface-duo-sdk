/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigationrail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BaseInterpolator
import androidx.activity.ComponentActivity
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.customview.view.AbsSavedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.navigationrail.NavigationRailMenuView
import com.google.android.material.navigationrail.NavigationRailView
import com.microsoft.device.dualscreen.utils.wm.DisplayPosition
import com.microsoft.device.dualscreen.utils.wm.OnVerticalSwipeListener
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.extractFoldingFeatureRect
import com.microsoft.device.dualscreen.utils.wm.getWindowVisibleDisplayFrame
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureHorizontal
import com.microsoft.device.dualscreen.utils.wm.isInDualMode
import com.microsoft.device.dualscreen.utils.wm.isSeparating
import com.microsoft.device.dualscreen.utils.wm.locationOnScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val MARGIN_LOWERING_FACTOR = 0.95

/**
 * A sub class of the [NavigationRailView] that can position its children in different ways when the application is spanned on both screens.
 * Using the [arrangeButtons] and [setMenuGravity] the children can be split in any way between the two screens.
 * Animations can be used when changing the arrangement of the buttons on the two screen.
 */
class NavigationRailView : NavigationRailView {
    constructor(context: Context) : super(context) {
        this.registerWindowInfoFlow()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.registerWindowInfoFlow()
        extractAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.registerWindowInfoFlow()
        extractAttributes(context, attrs)
    }

    private var screenMode = ScreenMode.DUAL_SCREEN
    private var job: Job? = null
    private var windowLayoutInfo: WindowLayoutInfo? = null

    private var topBtnCount: Int = -1
    private var bottomBtnCount: Int = -1

    private fun normalizeFoldingFeatureRectForView(): Rect {
        return windowLayoutInfo.extractFoldingFeatureRect().apply {
            offset(-locationOnScreen.x, 0)
        }
    }

    private fun registerWindowInfoFlow() {
        val activity = (context as? ComponentActivity)
            ?: throw RuntimeException("Context must implement androidx.activity.ComponentActivity!")
        job = activity.lifecycleScope.launch(Dispatchers.Main) {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity)
                    .collect { info ->
                        windowLayoutInfo = info
                        onInfoLayoutChanged()
                    }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    private fun onInfoLayoutChanged() {
        setScreenParameters()

        val changeBounds: Transition = ChangeBounds()
        changeBounds.duration = 300L
        changeBounds.interpolator = PathInterpolatorCompat.create(0.2f, 0f, 0f, 1f)
        TransitionManager.beginDelayedTransition(this@NavigationRailView, changeBounds)
        requestLayout()
    }

    private var onSwipeListener: OnVerticalSwipeListener =
        object : OnVerticalSwipeListener(context) {
            override fun onSwipeTop() {
                super.onSwipeTop()
                if (allowFlingGesture) {
                    menuGravity = Gravity.TOP
                }
            }

            override fun onSwipeBottom() {
                super.onSwipeBottom()
                if (allowFlingGesture) {
                    menuGravity = Gravity.BOTTOM
                }
            }
        }

    /**
     * Use an animation to move the buttons when the menu gravity is changed  or [arrangeButtons] is called.
     * By default the [AccelerateDecelerateInterpolator] is used.
     */
    var useAnimation: Boolean = true

    /**
     * Set the interpolator for the animation when menu gravity is changed or [arrangeButtons] is called.
     * By default the [AccelerateDecelerateInterpolator] is used.
     */
    var animationInterpolator: BaseInterpolator = AccelerateDecelerateInterpolator()

    /**
     * Allows the buttons to be moved to [DisplayPosition.START] or [DisplayPosition.END] with a swipe gesture.
     */
    var allowFlingGesture: Boolean = true

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
        } finally {
            styledAttributes.recycle()
        }
    }

    private var topScreenHeight = -1
    private var bottomScreenHeight = -1
    private var appWindowFrameHeight = -1
    private var hingeHeight = -1
    private var statusBarHeight = -1

    private var screenSize = Point()
    private var appWindowPosition = Rect()
    private var hingePosition = Rect()
    private var shouldRedrawMenu = true

    private fun setScreenParameters() {
        if (!isIntersectingHorizontalHinge()) {
            return
        }
        context.getWindowVisibleDisplayFrame().let { windowRect ->
            appWindowPosition = windowRect
            appWindowFrameHeight = windowRect.height()

            normalizeFoldingFeatureRectForView().let { hingeRect ->
                hingePosition = hingeRect

                val windowBounds = WindowMetricsCalculator.getOrCreate()
                    .computeCurrentWindowMetrics(context as Activity).bounds
                screenSize = Point(windowBounds.width(), windowBounds.height())

                statusBarHeight = screenSize.y - appWindowFrameHeight
                hingeHeight = hingeRect.height()
                topScreenHeight = hingeRect.top - windowRect.top
                bottomScreenHeight = windowRect.bottom - hingeRect.bottom
                shouldRedrawMenu = true
            }
        }
    }

    private fun isIntersectingHorizontalHinge(): Boolean {
        normalizeFoldingFeatureRectForView().let {
            return windowLayoutInfo.isFoldingFeatureHorizontal() &&
                (this.absY() + this.height > it.bottom)
        }
    }

    /**
     * Determines if the buttons should be split to avoid overlapping over the foldable feature.
     */
    private fun shouldSplitButtons(): Boolean {
        return windowLayoutInfo.isInDualMode() &&
            windowLayoutInfo.isFoldingFeatureHorizontal() &&
            windowLayoutInfo.isSeparating() &&
            isIntersectingHorizontalHinge()
    }

    /**
     * Sets the menu gravity and splits the menu buttons to avoid overlapping the folding feature
     */
    override fun setMenuGravity(gravity: Int) {
        super.setMenuGravity(gravity)
        topBtnCount = -1
        bottomBtnCount = -1
        shouldRedrawMenu = true
        requestLayout()
    }

    /**
     * When the application is in spanned mode the buttons can be split between the screens.
     * @param startBtnCount - how many buttons should be positioned on the top screen
     * @param endBtnCount - how many buttons should be positioned on the bottom screen
     */
    fun arrangeButtons(startBtnCount: Int, endBtnCount: Int) {
        getChildMenu()?.let { child ->
            if (!shouldSplitButtons()) {
                if (child.doesChildCountMatch(startBtnCount, endBtnCount)) {
                    syncBtnCount(startBtnCount, endBtnCount)
                }
                return
            }

            shouldRedrawMenu = true
            menuGravity = Gravity.CENTER
            syncBtnCount(startBtnCount, endBtnCount)
            requestLayout()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (shouldRedrawMenu) {
            super.onLayout(changed, left, top, right, bottom)
            getChildMenu()?.let { childMenu ->
                if (!shouldSplitButtons()) {
                    return
                }

                val childMenuTop = calculateMenuTopMargin()
                childMenu.layout(left, childMenuTop, right, bottom)
                childMenu.positionButtonsByGravity()
                shouldRedrawMenu = !shouldRedrawMenu
            }
        }
    }

    /**
     * Positions the buttons inside the [NavigationRailMenuView] depending on the selected gravity
     * and the foldable feature.
     */
    private fun NavigationRailMenuView.positionButtonsByGravity() {
        when (getGravity()) {
            Gravity.CENTER_VERTICAL -> {
                positionButtonsInCenter()
            }
            Gravity.BOTTOM -> {
                positionButtonsOnBottom()
            }
            Gravity.TOP -> {
                positionButtonsOnTop()
            }
        }
    }

    private fun NavigationRailMenuView.positionButtonsOnTop() {
        val childMenuTop = calculateMenuTopMargin()
        val defaultChildHeight = getChildAt(0).measuredHeight
        val availableHeightOnTopScreen = topScreenHeight - childMenuTop
        val skipAnimation = shouldSkipAnimation(this)

        // If the gravity is [Gravity.TOP] check if the buttons can fit
        // in a single screen by reducing the margins between them.
        if (defaultChildHeight * childCount * MARGIN_LOWERING_FACTOR <= availableHeightOnTopScreen &&
            defaultChildHeight * childCount > availableHeightOnTopScreen
        ) {
            for (i in 0 until childCount) {
                val newChildHeight = availableHeightOnTopScreen / childCount
                val childTop = i * newChildHeight
                setButtonPosition(getChildAt(i), childTop, skipAnimation)
            }
            return
        } else {
            // If the buttons can't fit on the top screen, move some on the bottom screen.
            var topStartingPosition = getChildAt(0).top
            val childrenAboveHinge =
                (availableHeightOnTopScreen / defaultChildHeight).coerceAtMost(childCount)

            for (i in 0 until childrenAboveHinge) {
                val childTop = i * defaultChildHeight + topStartingPosition

                val child = getChildAt(i)
                child.layout(child.left, 0, child.right, defaultChildHeight)
                setButtonPosition(child, childTop, skipAnimation)
            }

            if (childrenAboveHinge < childCount) {
                topStartingPosition = availableHeightOnTopScreen + hingeHeight

                for (i in 0 until childCount - childrenAboveHinge) {
                    val childTop =
                        +i * defaultChildHeight + topStartingPosition
                    val child = getChildAt(i + childrenAboveHinge)
                    child.layout(child.left, 0, child.right, 0 + defaultChildHeight)

                    setButtonPosition(child, childTop, skipAnimation)
                }
            }
        }
    }

    private fun NavigationRailMenuView.positionButtonsOnBottom() {
        val availableHeightOnBottomScreen = appWindowPosition.bottom - hingePosition.bottom
        val defaultChildHeight = getChildAt(0).measuredHeight
        val skipAnimation = shouldSkipAnimation(this)

        val startingPosition = this.height - availableHeightOnBottomScreen
        val newChildHeight =
            (availableHeightOnBottomScreen / childCount).coerceAtMost(defaultChildHeight)
        for (i in 0 until childCount) {
            val childTop = i * newChildHeight + startingPosition

            val child = getChildAt(i)
            child.layout(child.left, 0, child.right, 0 + defaultChildHeight)
            setButtonPosition(child, childTop, skipAnimation)
        }
        return
    }

    private fun NavigationRailMenuView.positionButtonsInCenter() {
        if (topBtnCount == -1 || bottomBtnCount == -1) {
            topBtnCount = childCount / 2 + childCount % 2
            bottomBtnCount = childCount / 2
        }

        val buttonsCount = topBtnCount + bottomBtnCount
        if (buttonsCount == 0) {
            return
        }

        if (topBtnCount == 0) {
            positionButtonsOnBottom()
            return
        }
        if (bottomBtnCount == 0) {
            positionButtonsOnTop()
            return
        }

        val childMenuTop = calculateMenuTopMargin()
        val defaultChildHeight = getChildAt(0).measuredHeight
        val availableHeightOnTopScreen = topScreenHeight - childMenuTop
        val skipAnimation = shouldSkipAnimation(this)

        var btnMargin = 0

        // Calculate a negative button margin if there is not enough space on the top
        if (defaultChildHeight * topBtnCount > availableHeightOnTopScreen) {
            btnMargin =
                (availableHeightOnTopScreen - defaultChildHeight * topBtnCount) / topBtnCount
        }

        val topStartingPosition =
            availableHeightOnTopScreen - topBtnCount * (defaultChildHeight + btnMargin)

        for (i in 0 until topBtnCount) {
            val childTop = topStartingPosition + i * (defaultChildHeight + btnMargin)
            val child = getChildAt(i)
            child.layout(child.left, 0, child.right, 0 + defaultChildHeight)
            setButtonPosition(child, childTop, skipAnimation)
        }

        val availableHeightOnBottomScreen = appWindowPosition.bottom - hingePosition.bottom
        val bottomStartingPosition = this.height - availableHeightOnBottomScreen

        // Calculate a negative button margin if there is not enough space on the bottom
        if (defaultChildHeight * bottomBtnCount > availableHeightOnBottomScreen) {
            btnMargin =
                (availableHeightOnBottomScreen - defaultChildHeight * bottomBtnCount) / bottomBtnCount
        }

        for (i in topBtnCount until buttonsCount) {
            val childTop = bottomStartingPosition +
                (i - topBtnCount) * (defaultChildHeight + btnMargin)
            val child = getChildAt(i)
            child.layout(child.left, 0, child.right, 0 + defaultChildHeight)
            setButtonPosition(child, childTop, skipAnimation)
        }
    }

    /**
     * Sets the position inside the [NavigationRailMenuView] and triggers the translation animations.
     */
    private fun setButtonPosition(
        child: View,
        childTop: Int,
        skipAnimation: Boolean
    ) {
        if (skipAnimation || !useAnimation || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            child.translationY = childTop.toFloat()
        } else {
            child.animate()
                .setInterpolator(animationInterpolator)
                .translationY(childTop.toFloat())
        }
    }

    /**
     * Returns the margin of the [NavigationRailMenuView] from the top of it's parent.
     * This is useful when the [NavigationRailView] contains a header view.
     */
    private fun calculateMenuTopMargin(): Int {
        return headerView?.let {
            val topMargin =
                resources.getDimensionPixelSize(com.google.android.material.R.dimen.mtrl_navigation_rail_margin)
            it.bottom + topMargin
        } ?: 0
    }

    private fun getGravity() = menuGravity and Gravity.VERTICAL_GRAVITY_MASK

    private fun getChildMenu(): NavigationRailMenuView? {
        for (i in 0..childCount) {
            val child = getChildAt(i)
            if (child is NavigationRailMenuView) {
                return child
            }
        }
        return null
    }

    /**
     * Checks if the newly requested positioning for the buttons matches the existing one.
     */
    private fun NavigationRailMenuView.doesChildCountMatch(
        startBtnCount: Int,
        endBtnCount: Int
    ): Boolean {
        return startBtnCount >= 0 && endBtnCount >= 0 && childCount == startBtnCount + endBtnCount
    }

    /**
     * Skip the animation on the first layout pass.
     */
    private fun shouldSkipAnimation(menu: NavigationRailMenuView): Boolean {
        for (i in 0 until menu.childCount) {
            if (menu.getChildAt(i).left != 0) {
                return true
            }
        }
        return false
    }

    /**
     * Synchronize the [startBtnCount] and [endBtnCount].
     */
    private fun syncBtnCount(startBtnCount: Int, endBtnCount: Int) {
        this.topBtnCount = startBtnCount
        this.bottomBtnCount = endBtnCount
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

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable = super.onSaveInstanceState()
        val state = SavedState(superState)
        state.useAnimation = this.useAnimation
        state.allowFlingGesture = this.allowFlingGesture
        state.menuGravity = getGravity()
        state.topBtnCount = this.topBtnCount
        state.bottomBtnCount = this.bottomBtnCount
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                this.useAnimation = state.useAnimation
                this.allowFlingGesture = state.allowFlingGesture
                menuGravity = state.menuGravity
                this.topBtnCount = state.topBtnCount
                this.bottomBtnCount = state.bottomBtnCount
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    internal class SavedState : AbsSavedState {
        var useAnimation: Boolean = true
        var allowFlingGesture: Boolean = true
        var menuGravity: Int = Gravity.TOP
        var topBtnCount: Int = -1
        var bottomBtnCount: Int = -1

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            useAnimation = source.readInt() == 1
            allowFlingGesture = source.readInt() == 1
            menuGravity = source.readInt()
            topBtnCount = source.readInt()
            bottomBtnCount = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (useAnimation) 1 else 0)
            out.writeInt(if (allowFlingGesture) 1 else 0)
            out.writeInt(menuGravity)
            out.writeInt(topBtnCount)
            out.writeInt(bottomBtnCount)
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

fun View.absY(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[1]
}