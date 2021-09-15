/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.ink

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Surface
import android.view.TextureView

// constants
const val minPointsForValidStroke = 2
const val defaultHoverStrokeWidth = 5f

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
    private val overridePaint: Paint
    private val clearPaint: Paint
    private val hoverPaint = Paint()
    private val hoverEraserPaint = Paint()
    private val hoverHighlightPaint = Paint()

    // attributes
    private var enablePressure = false
    private var enableInking = false
    private var renderGPU = true
    private var minStrokeWidth = 1f
    private var maxStrokeWidth = 10f

    private val strokeList = mutableListOf<InputManager.ExtendedStroke>()
    private val inkingList = mutableListOf<DynamicPaintHandler?>()

    // properties
    var color = Color.GRAY
        set(value) {
            field = value
            currentStrokePaint.color = value
            hoverPaint.color = value
            hoverHighlightPaint.color = value
        }

    var strokeWidth: Float
        get() {
            return minStrokeWidth
        }
        set(value) {
            minStrokeWidth = value
            // cache eraser hover radius
            val radius = (maxStrokeWidth - minStrokeWidth) / 2
            hoverEraserPaint.setPathEffect(DashPathEffect(floatArrayOf(radius, radius, radius, radius), 0f))
        }

    var strokeWidthMax: Float
        get() {
            return maxStrokeWidth
        }
        set(value) {
            maxStrokeWidth = value
            // cache eraser hover radius
            val radius = (maxStrokeWidth - minStrokeWidth) / 2
            hoverEraserPaint.setPathEffect(DashPathEffect(floatArrayOf(radius, radius, radius, radius), 0f))
        }

    var pressureEnabled: Boolean
        get() {
            return enablePressure
        }
        set(value) {
            enablePressure = value
        }

    var inkingEnabled: Boolean
        get() {
            return enableInking
        }
        set(value) {
            enableInking = value
        }

    var dynamicPaintHandler: DynamicPaintHandler? = null

    interface DynamicPaintHandler {
        fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint
        fun selectInkingMode(): InputManager.InkingType
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
                renderGPU = getBoolean(R.styleable.InkView_render_gpu, renderGPU)
            } finally {
                recycle()
            }
        }
        isOpaque = false // make the texture view transparent!
        this.surfaceTextureListener = this

        // setup blend modes
        overridePaint = Paint()
        overridePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        inputManager = createInputManager()

        initCurrentStrokePaint()
        initHoverPaint()
    }

    private fun createInputManager(): InputManager {
        return InputManager(
            this,
            object : InputManager.PenInputHandler {
                override fun strokeStarted(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    if (enableInking) {
                        stroke.color = color
                        stroke.width = strokeWidth
                        stroke.widthMax = strokeWidthMax
                        redrawTexture()
                    }
                }

                override fun strokeUpdated(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    if (enableInking) {
                        redrawTexture()
                    }
                }

                override fun strokeCompleted(
                    penInfo: InputManager.PenInfo,
                    stroke: InputManager.ExtendedStroke
                ) {
                    if (enableInking) {
                        redrawTexture()
                        strokeList += stroke
                        inkingList += dynamicPaintHandler
                    }
                }
            },
            object : InputManager.PenHoverHandler {
                override fun hoverStarted(penInfo: InputManager.PenInfo) {
                    if (enableInking) {
                        drawHover(
                            penInfo.x,
                            penInfo.y,
                            (minStrokeWidth + maxStrokeWidth) / 2,
                            penInfo.pointerType
                        )
                    }
                }

                override fun hoverMoved(penInfo: InputManager.PenInfo) {
                    if (enableInking) {
                        drawHover(
                            penInfo.x,
                            penInfo.y,
                            (minStrokeWidth + maxStrokeWidth) / 2,
                            penInfo.pointerType
                        )
                    }
                }

                override fun hoverEnded(penInfo: InputManager.PenInfo) {
                    if (enableInking) {
                        redrawTexture()
                    }
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

    private fun initHoverPaint() {
        hoverPaint.color = currentStrokePaint.color
        hoverPaint.isAntiAlias = true
        // Set stroke width based on display density.
        hoverPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            defaultHoverStrokeWidth,
            resources.displayMetrics
        )

        hoverPaint.style = Paint.Style.STROKE
        hoverPaint.strokeJoin = Paint.Join.ROUND
        hoverPaint.strokeCap = Paint.Cap.ROUND

        // Eraser hover indicator
        hoverEraserPaint.color = Color.LTGRAY
        hoverEraserPaint.isAntiAlias = true
        hoverEraserPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            defaultHoverStrokeWidth,
            resources.displayMetrics
        )
        hoverEraserPaint.style = Paint.Style.STROKE
        hoverEraserPaint.strokeJoin = Paint.Join.ROUND
        hoverEraserPaint.strokeCap = Paint.Cap.ROUND

        // Highlight hover indicator
        hoverHighlightPaint.color = currentStrokePaint.color
        hoverHighlightPaint.isAntiAlias = true
        hoverHighlightPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            defaultHoverStrokeWidth,
            resources.displayMetrics
        )
        hoverHighlightPaint.style = Paint.Style.STROKE
        hoverHighlightPaint.strokeJoin = Paint.Join.BEVEL
        hoverHighlightPaint.strokeCap = Paint.Cap.BUTT
    }

    fun clearInk() {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        strokeList.clear()
        inputManager.currentStroke.reset()
        redrawTexture()
    }

    fun saveBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val saveCanvas = Canvas(bitmap)
        drawStroke()
        saveCanvas.drawBitmap(canvasBitmap, 0f, 0f, overridePaint)
        return bitmap
    }

    fun getDrawing(): List<InputManager.ExtendedStroke> {
        return strokeList
    }

    fun loadDrawing(strokes: List<InputManager.ExtendedStroke>, inks: List<DynamicPaintHandler?>) {
        if (strokes.size != inks.size)
            return

        strokeList.clear()
        inkingList.clear()

        strokeList.addAll(strokes.toCollection(mutableListOf()))
        inkingList.addAll(inks.toCollection(mutableListOf()))
    }

    private fun drawStrokeList() {
        if (strokeList.size != inkingList.size)
            return

        val defaultStroke = createDefaultStroke()
        val defaultPaintHandler = dynamicPaintHandler

        for (i in strokeList.indices) {
            val stroke = strokeList[i]
            stroke.lastPointReferenced = 0

            inputManager.currentStroke = stroke
            loadStrokeValues(stroke, inkingList[i])
            redrawTexture()
        }

        loadStrokeValues(defaultStroke, defaultPaintHandler)
    }

    private fun createDefaultStroke(): InputManager.ExtendedStroke {
        val defaultStroke = InputManager.ExtendedStroke()
        defaultStroke.color = color
        defaultStroke.width = strokeWidth
        defaultStroke.widthMax = strokeWidthMax
        return defaultStroke
    }

    private fun loadStrokeValues(stroke: InputManager.ExtendedStroke, paintHandler: DynamicPaintHandler?) {
        color = stroke.color
        strokeWidth = stroke.width
        strokeWidthMax = stroke.widthMax
        dynamicPaintHandler = paintHandler
    }

    private fun updateStrokeWidth(pressure: Float, paint: Paint) {
        paint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minStrokeWidth + ((maxStrokeWidth - minStrokeWidth) * pressure),
            resources.displayMetrics
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
    }

    fun drawHover(cx: Float, cy: Float, radius: Float, pointerType: InputManager.PointerType = InputManager.PointerType.UNKNOWN) {
        val canvas: Canvas = if (renderGPU) surface?.lockHardwareCanvas() ?: return else surface?.lockCanvas(null) ?: return

        try {
            // Copy image to the canvas
            canvas.drawBitmap(canvasBitmap, 0f, 0f, overridePaint)

            // Draw hover
            if (dynamicPaintHandler?.selectInkingMode() == InputManager.InkingType.HIGHLIGHTING) {
                // use linear shape for highlighter
                canvas.drawLine(cx, cy - radius, cx, cy + radius, hoverHighlightPaint)
            } else {
                // use circular shape for pen/eraser
                val hover = when (pointerType) {
                    InputManager.PointerType.PEN_ERASER -> hoverEraserPaint
                    else -> hoverPaint
                }
                canvas.drawCircle(cx, cy, radius, hover)
            }
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

    fun redrawTexture() {
        drawStroke()
        val canvas: Canvas = if (renderGPU) surface?.lockHardwareCanvas() ?: return else surface?.lockCanvas(null) ?: return

        try {
            // Copy image to the canvas
            canvas.drawBitmap(canvasBitmap, 0f, 0f, overridePaint)
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

    private fun drawStroke() {
        val stroke = inputManager.currentStroke
        val points = stroke.getPoints()

        if (strokeList.isEmpty() && points.isEmpty()) {
            return
        }

        if (points.size < minPointsForValidStroke) {
            return
        }

        // update the drawCanvas with the latest stroke data
        var startPoint = points[stroke.lastPointReferenced]
        for (i in stroke.lastPointReferenced + 1 until points.size) {
            val penInfo = stroke.getPenInfo(i)
            if (penInfo != null) {
                when {
                    penInfo.pointerType == InputManager.PointerType.PEN_ERASER -> {
                        updateStrokeWidth(penInfo.pressure, clearPaint)
                        drawCanvas.drawCircle(
                            penInfo.x,
                            penInfo.y,
                            (minStrokeWidth + maxStrokeWidth) / 2,
                            clearPaint
                        )
                        stroke.inkingMode = InputManager.InkingType.ERASING
                    }
                    dynamicPaintHandler != null -> {
                        dynamicPaintHandler?.let { paintHandler ->
                            val paint = paintHandler.generatePaintFromPenInfo(penInfo)
                            hoverPaint.color = paint.color
                            hoverHighlightPaint.color = paint.color
                            drawCanvas.drawLine(
                                startPoint.x,
                                startPoint.y,
                                penInfo.x,
                                penInfo.y,
                                paint
                            )
                            stroke.inkingMode = paintHandler.selectInkingMode()
                        }
                    }
                    else -> {
                        val paint = when (stroke.inkingMode) {
                            InputManager.InkingType.ERASING -> clearPaint
                            else -> currentStrokePaint
                        }
                        if (enablePressure)
                            updateStrokeWidth(penInfo.pressure, paint)

                        drawCanvas.drawLine(
                            startPoint.x, startPoint.y, penInfo.x, penInfo.y,
                            paint
                        )
                    }
                }

                startPoint = points[i]
            }
        }
        stroke.lastPointReferenced = points.size - 1
    }

    /**
     * Erase last stroke and redraw all other strokes
     */
    fun undo() {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (strokeList.isNotEmpty() && inkingList.isNotEmpty()) {
            strokeList.removeLast()
            inkingList.removeLast()
        }
        drawStrokeList()
        if (strokeList.isEmpty()) {
            redrawTexture()
        }
    }

    /**
     * Invoked when a [TextureView]'s SurfaceTexture is ready for use.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The width of the surface
     * @param height The height of the surface
     */
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        this.surface = Surface(surface)
        redrawTexture()
        drawStrokeList()
    }

    /**
     * Invoked when the [SurfaceTexture]'s buffers size changed.
     *
     * @param surface The surface returned by
     * [android.view.TextureView.getSurfaceTexture]
     * @param width The new width of the surface
     * @param height The new height of the surface
     */
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    /**
     * Invoked when the specified [SurfaceTexture] is about to be destroyed.
     * If returns true, no rendering should happen inside the surface texture after this method
     * is invoked. If returns false, the client needs to call [SurfaceTexture.release].
     * Most applications should return true.
     *
     * @param surface The surface about to be destroyed
     */
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        this.surface?.release()
        return true
    }

    /**
     * Invoked when the specified [SurfaceTexture] is updated through
     * [SurfaceTexture.updateTexImage].
     *
     * @param surface The surface just updated
     */
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}