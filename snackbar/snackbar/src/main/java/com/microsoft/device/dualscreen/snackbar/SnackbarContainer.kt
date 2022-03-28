/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.snackbar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.google.android.material.snackbar.Snackbar
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.BOTH
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.END
import com.microsoft.device.dualscreen.snackbar.SnackbarPosition.START
import com.microsoft.device.dualscreen.utils.wm.extractFoldingFeatureRect
import com.microsoft.device.dualscreen.utils.wm.getWindowRect
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import com.microsoft.device.dualscreen.utils.wm.isInDualMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Container used to display a [Snackbar] on the desired display area.
 *
 * How to use it:
 *         Snackbar.make(snackbarContainer.coordinatorLayout, message, LENGTH_LONG)
 *           .show(snackbarContainer, position)
 *
 */
class SnackbarContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    companion object {
        @VisibleForTesting
        const val COORDINATOR_LAYOUT_MARGIN = 20
    }

    val coordinatorLayout: CoordinatorLayout by lazy { CoordinatorLayout(context) }
    private var job: Job? = null
    private var windowLayoutInfo: WindowLayoutInfo? = null
    private val requiredActivity: ComponentActivity by lazy {
        getActivityFromContext() ?: throw RuntimeException("Context must implement androidx.activity.ComponentActivity!")
    }
    private val onReadyListeners = mutableListOf<OnReadyListener?>()

    init {
        addView(coordinatorLayout, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        registerWindowInfoFlow()
    }

    /**
     * Add a listener that will be called when the container is ready to be used.
     *
     * @param listener The listener that will be called when the container is ready to be used.
     */
    fun addOnReadyListener(listener: OnReadyListener) {
        onReadyListeners.add(listener)
    }

    /**
     * Remove a listener for layout changes.
     * @param listener – The listener for layout changes.
     */
    fun removeOnReadyListener(listener: OnReadyListener) {
        onReadyListeners.remove(listener)
    }

    /**
     * Performs the given action when the container is ready to be used.
     * The action will only be invoked once the container is ready and then removed.
     */
    fun doWhenIsReady(runnable: Runnable) {
        if (windowLayoutInfo == null) {
            addOnReadyListener(
                object : OnReadyListener {
                    override fun onReady() {
                        runnable.run()
                        removeOnReadyListener(this)
                    }
                }
            )
        } else {
            runnable.run()
        }
    }

    private fun registerWindowInfoFlow() {
        job = requiredActivity.lifecycleScope.launch(Dispatchers.Main) {
            requiredActivity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                WindowInfoTracker.getOrCreate(requiredActivity)
                    .windowLayoutInfo(requiredActivity)
                    .collect {
                        windowLayoutInfo = it
                        notifyContainerReadyListeners()
                    }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    /**
     * Unwrap the hierarchy of [ContextWrapper]-s until [Activity] is reached.
     * @return Base [Activity] context or [null] if not available.
     */
    private fun getActivityFromContext(): ComponentActivity? {
        var contextBuffer = context
        while (contextBuffer is ContextWrapper) {
            if (contextBuffer is ComponentActivity) {
                return contextBuffer
            }
            contextBuffer = contextBuffer.baseContext
        }
        return null
    }

    /**
     * Updates the position for the [CoordinatorLayout] child depending on the screen mode and [SnackbarPosition] param.
     *
     * @param position the given [SnackbarPosition]
     */
    fun updatePosition(position: SnackbarPosition) {
        when {
            position == BOTH || !windowLayoutInfo.isInDualMode() -> updatePositionForSingleScreen()
            position == START || position == END -> when {
                windowLayoutInfo.isFoldingFeatureVertical() -> updatePositionWhenHorizontalFoldingFeature(position)
                else -> updatePositionWhenVerticalFoldingFeature(position)
            }
        }
    }

    private fun notifyContainerReadyListeners() {
        onReadyListeners.forEach {
            it?.onReady()
        }
    }

    /**
     * Updates the position for the [CoordinatorLayout] child when the device is in single screen mode.
     */
    private fun updatePositionForSingleScreen() {
        val screenWidth = requiredActivity.getWindowRect().width()
        val widthValue = screenWidth - 2 * COORDINATOR_LAYOUT_MARGIN
        coordinatorLayout.updateLayoutParams<LayoutParams> {
            gravity = Gravity.BOTTOM
            width = widthValue
            leftMargin = COORDINATOR_LAYOUT_MARGIN
            rightMargin = COORDINATOR_LAYOUT_MARGIN
            bottomMargin = COORDINATOR_LAYOUT_MARGIN
        }
    }

    /**
     * Updates the position for the [CoordinatorLayout] child when the FoldingFeature is horizontal,
     * depending on the [SnackbarPosition] param.
     *
     * @param position the given [SnackbarPosition]
     */
    private fun updatePositionWhenHorizontalFoldingFeature(position: SnackbarPosition) {
        val foldingFeatureRect = windowLayoutInfo.extractFoldingFeatureRect()
        val screenWidth = requiredActivity.getWindowRect().width()

        val rightMarginValue = when (position) {
            START -> foldingFeatureRect.left - COORDINATOR_LAYOUT_MARGIN
            END -> COORDINATOR_LAYOUT_MARGIN
            else -> 0
        }

        val leftMarginValue = when (position) {
            START -> COORDINATOR_LAYOUT_MARGIN
            END -> foldingFeatureRect.right + COORDINATOR_LAYOUT_MARGIN
            else -> 0
        }

        val gravityValue = when (position) {
            START -> Gravity.START
            END -> Gravity.END
            BOTH -> Gravity.CENTER_HORIZONTAL
        } or Gravity.BOTTOM

        val widthValue = when (position) {
            START -> foldingFeatureRect.left
            END -> screenWidth - foldingFeatureRect.right
            BOTH -> screenWidth
        } - 2 * COORDINATOR_LAYOUT_MARGIN

        coordinatorLayout.updateLayoutParams<LayoutParams> {
            gravity = gravityValue
            width = widthValue
            leftMargin = leftMarginValue
            rightMargin = rightMarginValue
            bottomMargin = COORDINATOR_LAYOUT_MARGIN
        }
    }

    /**
     * Updates the position for the [CoordinatorLayout] child when the FoldingFeature is vertical,
     * depending on the [SnackbarPosition] param.
     *
     * @param position the given [SnackbarPosition]
     */
    private fun updatePositionWhenVerticalFoldingFeature(position: SnackbarPosition) {
        val foldingFeatureRect = windowLayoutInfo.extractFoldingFeatureRect()
        val screenHeight = requiredActivity.getWindowRect().height()

        val bottomMarginValue = when (position) {
            START -> screenHeight - foldingFeatureRect.top + COORDINATOR_LAYOUT_MARGIN
            END, BOTH -> COORDINATOR_LAYOUT_MARGIN
        }

        coordinatorLayout.updateLayoutParams<LayoutParams> {
            gravity = Gravity.BOTTOM
            width = MATCH_PARENT
            leftMargin = COORDINATOR_LAYOUT_MARGIN
            rightMargin = COORDINATOR_LAYOUT_MARGIN
            bottomMargin = bottomMarginValue
        }
    }

    /**
     * Interface definition for a callback to be invoked when the container is ready to be used.
     */
    @FunctionalInterface
    interface OnReadyListener {
        /**
         * Called when the container can be used.
         */
        fun onReady()
    }
}