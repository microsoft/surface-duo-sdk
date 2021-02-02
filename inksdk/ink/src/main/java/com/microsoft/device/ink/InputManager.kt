/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.digitalink.Ink

@RequiresApi(Build.VERSION_CODES.M)
class InputManager(view: View, penInputHandler: PenInputHandler) {

    private val inputHandler: PenInputHandler
    val currentStroke = ExtendedStroke()

    init {
        setupInputEvents(view)
        inputHandler = penInputHandler
        currentStroke.reset()
    }

    interface PenInputHandler {
        fun strokeStarted(penInfo: PenInfo, stroke: ExtendedStroke)
        fun strokeUpdated(penInfo: PenInfo, stroke: ExtendedStroke)
        fun strokeCompleted(penInfo: PenInfo, stroke: ExtendedStroke)
    }

    enum class PointerType {
        MOUSE,
        FINGER,
        PEN_TIP,
        PEN_ERASER,
        UNKNOWN
    }

    data class PenInfo(
        val pointerType: PointerType,
        val x: Float,
        val y: Float,
        val pressure: Float,
        val orientation: Float,
        val tilt: Float,
        val primaryButtonState: Boolean,
        val secondaryButtonState: Boolean
    ) {
        companion object {
            @RequiresApi(Build.VERSION_CODES.M)
            fun createFromEvent(event: MotionEvent): PenInfo {
                val pointerType: PointerType = when (event.getToolType(0)) {
                    MotionEvent.TOOL_TYPE_FINGER -> PointerType.FINGER
                    MotionEvent.TOOL_TYPE_MOUSE -> PointerType.MOUSE
                    MotionEvent.TOOL_TYPE_STYLUS -> PointerType.PEN_TIP
                    MotionEvent.TOOL_TYPE_ERASER -> PointerType.PEN_ERASER
                    else -> PointerType.UNKNOWN
                }

                return PenInfo(
                    pointerType = pointerType,
                    x = event.x,
                    y = event.y,
                    pressure = event.pressure,
                    orientation = event.orientation,
                    tilt = event.getAxisValue(MotionEvent.AXIS_TILT),
                    primaryButtonState = ((event.buttonState and MotionEvent.BUTTON_PRIMARY) > 0)
                            or ((event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) > 0),
                    secondaryButtonState = ((event.buttonState and MotionEvent.BUTTON_SECONDARY) > 0)
                            or ((event.buttonState and MotionEvent.BUTTON_STYLUS_SECONDARY) > 0)
                )
            }
        }
    }

    class ExtendedStroke {
        private var builder = Ink.Stroke.builder()
        private var penInfos = HashMap<Int, PenInfo>()

        fun addPoint(penInfo: PenInfo) {
            val point = Ink.Point.create(penInfo.x, penInfo.y)
            builder.addPoint(point)
            penInfos[point.hashCode()] = penInfo
        }

        fun getPoints(): List<Ink.Point> {
            return builder.build().points
        }

        fun getPenInfo(point: Ink.Point): PenInfo? {
            return penInfos[point.hashCode()]
        }

        fun reset() {
            builder = Ink.Stroke.builder()
            penInfos.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun setupInputEvents(view: View) {

        view.setOnTouchListener { _: View, event: MotionEvent ->

            var consumed = true
            val penInfo = PenInfo.createFromEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentStroke.reset()
                    currentStroke.addPoint(penInfo)
                    inputHandler.strokeStarted(penInfo, currentStroke)
                }
                MotionEvent.ACTION_MOVE -> {
                    currentStroke.addPoint(penInfo)
                    inputHandler.strokeUpdated(penInfo, currentStroke)
                }
                MotionEvent.ACTION_UP -> {
                    currentStroke.addPoint(penInfo)
                    inputHandler.strokeCompleted(penInfo, currentStroke)
                }
                else -> consumed = false
            }

            consumed

        }
    }

    companion object {
        private const val TAG = "Ink.InputManager"
    }
}