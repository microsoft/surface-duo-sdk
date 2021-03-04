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
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Surface
import android.view.TextureView

// constants
const val minPointsForValidStroke = 2

class InkView constructor(
    context: Context,
    attributeSet: AttributeSet
) :
    TextureView(context, attributeSet), TextureView.SurfaceTextureListener {

    private var surface: Surface? = null
    private var inputManager: InputManager
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawCanvas: Canvas
    private val currentStrokePaint = Paint()
    private val strokeList = mutableListOf<InputManager.ExtendedStroke>()
    private val overridePaint: Paint
    private val clearPaint: Paint

    // attributes
    private var enablePressure = false
    private var minStrokeWidth = 1f
    private var maxStrokeWidth = 10f

    // properties
    var color = Color.GRAY
        set(value) {
            field = value
            currentStrokePaint.color = value
        }

    var dynamicPaintHandler: DynamicPaintHandler? = null

    interface DynamicPaintHandler {
        fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint
    }

    init {
        // handle attributes
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.InkView,
            0, 0
        ).apply {
            try {
                enablePressure = getBoolean(R.styleable.InkView_enable_pressure, enablePressure)
                color = getColor(R.styleable.InkView_ink_color, color)
                minStrokeWidth = getFloat(R.styleable.InkView_min_stroke_width, minStrokeWidth)
                maxStrokeWidth = getFloat(R.styleable.InkView_max_stroke_width, maxStrokeWidth)
            } finally {
                recycle()
            }
        }
        isOpaque = false // make the texture view transparent!
        this.surfaceTextureListener = this

        // setup blend modes
        overridePaint = Paint()
        overridePaint.blendMode = BlendMode.SRC
        clearPaint = Paint()
        clearPaint.blendMode = BlendMode.CLEAR

        inputManager = createInputManager()

        initCurrentStrokePaint()
    }

    private fun createInputManager(): InputManager {
        return InputManager(
            this,
            object : InputManager.PenInputHandler {
                override fun strokeStarted(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    redrawTexture()
                }

                override fun strokeUpdated(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    redrawTexture()
                }

                override fun strokeCompleted(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    redrawTexture()
                    strokeList += stroke
                }
            }
        )
    }

    private fun initCurrentStrokePaint() {
        currentStrokePaint.color = color
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

    fun clearInk() {
        drawCanvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
        strokeList.clear()
        inputManager.currentStroke.reset()
        redrawTexture()
    }

    fun saveBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawIt(Canvas(bitmap))
        return bitmap
    }

    private fun updateStrokeWidth(pressure: Float) {
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minStrokeWidth + ((maxStrokeWidth - minStrokeWidth) * pressure),
            resources.displayMetrics
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        redrawTexture()
    }

    fun redrawTexture() {
        val canvas: Canvas = surface?.lockCanvas(null) ?: return
        try {
            drawIt(canvas)
        } finally {
            // Publish the frame.  If we overrun the consumer, frames will be dropped,
            // so on a sufficiently fast device the animation will run at faster than
            // the display refresh rate.
            //
            // If the SurfaceTexture has been destroyed, this will throw an exception.
            try {
                surface?.unlockCanvasAndPost(canvas)
            } catch (iae: IllegalArgumentException) {
                return
            }
        }
    }

    fun drawIt(canvas: Canvas) {
        val stroke = inputManager.currentStroke
        drawStroke(canvas, stroke)
    }

    private fun drawStroke(canvas: Canvas, stroke: InputManager.ExtendedStroke) {

        val points = stroke.getPoints()

        if (strokeList.isEmpty() && points.isEmpty()) {
            canvas.drawBitmap(canvasBitmap, 0f, 0f, overridePaint)
            return
        }

        if (points.size < minPointsForValidStroke) {
            return
        }

        // update the drawCanvas with the latest stroke data
        var startPoint = points[stroke.lastPointReferenced]
        for (i in stroke.lastPointReferenced + 1 until points.size) {
            val penInfo = stroke.getPenInfo(points[i])
            if (penInfo != null) {

                when {
                    penInfo.pointerType == InputManager.PointerType.PEN_ERASER -> {
                        drawCanvas.drawCircle(penInfo.x, penInfo.y, 30f, clearPaint)
                    }
                    dynamicPaintHandler != null -> {
                        dynamicPaintHandler?.let { paintHandler ->
                            val paint = paintHandler.generatePaintFromPenInfo(penInfo)
                            drawCanvas.drawLine(
                                startPoint.x,
                                startPoint.y,
                                penInfo.x,
                                penInfo.y,
                                paint
                            )
                        }
                    }
                    enablePressure -> {
                        updateStrokeWidth(penInfo.pressure)
                        drawCanvas.drawLine(startPoint.x, startPoint.y, penInfo.x, penInfo.y, currentStrokePaint)
                    }
                }

                startPoint = points[i]
            }
        }
        stroke.lastPointReferenced = points.size - 1
        // Copy image to the canvas
        canvas.drawBitmap(canvasBitmap, 0f, 0f, overridePaint)
    }

    /**
     * Invoked when a [TextureView]'s SurfaceTexture is ready for use.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The width of the surface
     * @param height The height of the surface
     */
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (surface != null) {
            this.surface = Surface(surface)
        } else {
            this.surface?.release()
            this.surface = null
        }
    }

    /**
     * Invoked when the [SurfaceTexture]'s buffers size changed.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The new width of the surface
     * @param height The new height of the surface
     */
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    /**
     * Invoked when the specified [SurfaceTexture] is about to be destroyed.
     * If returns true, no rendering should happen inside the surface texture after this method
     * is invoked. If returns false, the client needs to call [SurfaceTexture.release].
     * Most applications should return true.
     *
     * @param surface The surface about to be destroyed
     */
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        this.surface?.release()
        return true
    }

    /**
     * Invoked when the specified [SurfaceTexture] is updated through
     * [SurfaceTexture.updateTexImage].
     *
     * @param surface The surface just updated
     */
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }
}