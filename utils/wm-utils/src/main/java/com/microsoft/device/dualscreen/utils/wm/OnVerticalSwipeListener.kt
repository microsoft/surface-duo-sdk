/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.utils.wm

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

/**
 * Helper class that detects a swipe left or right on the custom views
 */
open class OnVerticalSwipeListener(context: Context?) : OnTouchListener {
    companion object {
        private const val SWIPE_MIN_DISTANCE = 130

        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private var gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    private var flingDone = false
    private var touchY = 0f

    open fun onSwipeBottom() {}

    open fun onSwipeTop() {}

    open fun onTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            flingDone = false
        } else if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            touchY = ev.y
        } else {
            if (flingDone) {
                return true
            }
            val dX: Float = abs(ev.y - touchY)
            if (dX > SWIPE_MIN_DISTANCE) {
                flingDone = true
                return true
            }
        }
        return false
    }

    inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffY) > abs(diffX) &&
                    abs(diffY) > SWIPE_THRESHOLD &&
                    abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }
}