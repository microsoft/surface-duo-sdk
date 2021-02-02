/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class InkView constructor(
    context: Context,
    attributeSet: AttributeSet
) :
    View(context, attributeSet) {

    private var inputManager: InputManager
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawCanvas: Canvas
    private val currentStrokePaint: Paint
    private val strokeList = mutableListOf<InputManager.ExtendedStroke>()

    // attributes
    private var enablePressure = false
    private var inkColor = Color.GRAY
    private var minStrokeWidth = 1f
    private var maxStrokeWidth = 10f

    private var _dynamicPaintHandler: DynamicPaintHandler? = null

    var dynamicPaintHandler = _dynamicPaintHandler
        set(value) {
            _dynamicPaintHandler = value
            field = value
        }

    interface DynamicPaintHandler {
        fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint
    }

    init {
        //handle attributes
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.InkView,
            0, 0
        ).apply {

            try {
                enablePressure = getBoolean(R.styleable.InkView_enable_pressure, enablePressure)
                inkColor = getColor(R.styleable.InkView_ink_color, inkColor)
                minStrokeWidth = getFloat(R.styleable.InkView_min_stroke_width, minStrokeWidth)
                maxStrokeWidth = getFloat(R.styleable.InkView_max_stroke_width, maxStrokeWidth)
            } finally {
                recycle()
            }
        }

        inputManager = InputManager(this, object : InputManager.PenInputHandler {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun strokeStarted(
                penInfo: InputManager.PenInfo,
                stroke: InputManager.ExtendedStroke
            ) {
                // Log.i(TAG, "strokeStarted " + stroke.getPoints().size)
                if (penInfo.pointerType == InputManager.PointerType.PEN_ERASER) {
                    clearInk()
                }
            }

            override fun strokeUpdated(
                penInfo: InputManager.PenInfo,
                stroke: InputManager.ExtendedStroke
            ) {
                invalidate()
            }

            override fun strokeCompleted(
                penInfo: InputManager.PenInfo,
                stroke: InputManager.ExtendedStroke
            ) {
                drawStroke(drawCanvas, stroke)
                strokeList += stroke
            }
        })

        currentStrokePaint = Paint()
        currentStrokePaint.color = inkColor
        currentStrokePaint.isAntiAlias = true
        // Set stroke width based on display density.
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minStrokeWidth,
            resources.displayMetrics
        )

        currentStrokePaint.style = Paint.Style.STROKE
        currentStrokePaint.strokeJoin = Paint.Join.ROUND
        currentStrokePaint.strokeCap = Paint.Cap.ROUND
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun clearInk() {
        drawCanvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
        strokeList.clear()
        inputManager.currentStroke.reset()
        invalidate()
    }

    fun saveBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setColor(color: Color) {
        inkColor = color.toArgb()
        currentStrokePaint.color = inkColor
    }

    private fun updateStrokeWidth(preeasure: Float) {
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minStrokeWidth + ((maxStrokeWidth - minStrokeWidth) * preeasure),
            resources.displayMetrics
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val stroke = inputManager.currentStroke
        drawStroke(canvas, stroke)
    }

    private fun drawStroke(canvas: Canvas, stroke: InputManager.ExtendedStroke) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, null)

        val points = stroke.getPoints()
        if (points.size < 2) {
            return
        }
        var startPoint = points[0]

        for (i in 1 until points.size) {
            val penInfo = stroke.getPenInfo(points[i])
            if (penInfo != null && penInfo.pointerType != InputManager.PointerType.PEN_ERASER) {
                var paint = currentStrokePaint
                if (_dynamicPaintHandler != null) {
                    paint = _dynamicPaintHandler!!.generatePaintFromPenInfo(penInfo)
                } else if (enablePressure) {
                    updateStrokeWidth(penInfo.pressure)
                }

                canvas.drawLine(startPoint.x, startPoint.y, penInfo.x, penInfo.y, paint)
                startPoint = points[i]
            }
        }
    }
}