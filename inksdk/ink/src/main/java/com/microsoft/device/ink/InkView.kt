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
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RequiresApi
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class InkView constructor(
    context: Context,
    attributeSet: AttributeSet
) :
    TextureView(context, attributeSet) {

    private var inputManager: InputManager
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawCanvas: Canvas
    private val currentStrokePaint = Paint()
    private val strokeList = mutableListOf<InputManager.ExtendedStroke>()

    private lateinit var mRenderer : Renderer

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
        isOpaque = false // mae the texture view transparent!
        //handle attributes
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

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun strokeCompleted(
                penInfo: InputManager.PenInfo,
                stroke: InputManager.ExtendedStroke
            ) {
                drawStroke(drawCanvas, stroke)
                strokeList += stroke
            }
        })

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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun clearInk() {
        drawCanvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
        strokeList.clear()
        inputManager.currentStroke.reset()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawIt(Canvas(bitmap))
        return bitmap
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

     @RequiresApi(Build.VERSION_CODES.Q)
     fun drawIt(canvas: Canvas) {
        val stroke = inputManager.currentStroke
        drawStroke(canvas, stroke)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun drawStroke(canvas: Canvas, stroke: InputManager.ExtendedStroke) {

        val points = stroke.getPoints()

        if(strokeList.isEmpty() &&  points.isEmpty()){
            canvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
            return
        }


        if (points.size < 2) {
            return
        }
        var startPoint = points[stroke.lastPointReferenced]

        for (i in stroke.lastPointReferenced+1 until points.size) {
            val penInfo = stroke.getPenInfo(points[i])
            if (penInfo != null && penInfo.pointerType != InputManager.PointerType.PEN_ERASER) {
                var paint = currentStrokePaint
                if (_dynamicPaintHandler != null) {
                    paint = _dynamicPaintHandler!!.generatePaintFromPenInfo(penInfo)
                } else if (enablePressure) {
                    updateStrokeWidth(penInfo.pressure)
                }

                drawCanvas.drawLine(startPoint.x, startPoint.y, penInfo.x, penInfo.y, paint)
                startPoint = points[i]
            }
        }
        stroke.lastPointReferenced = points.size-1
        canvas.drawBitmap(canvasBitmap, 0f, 0f, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mRenderer = Renderer()
        mRenderer.drawCallback =  object : Renderer.DrawCallback {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun draw(canvas: Canvas) {
                drawIt(canvas)
            }
        }

        mRenderer.start()
        this.surfaceTextureListener = mRenderer
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mRenderer.halt()
    }

    /**
     * Handles Canvas rendering and SurfaceTexture callbacks.
     *
     *
     * We don't create a Looper, so the SurfaceTexture-by-way-of-TextureView callbacks
     * happen on the UI thread.
     */
    private class Renderer : Thread("TextureViewCanvas Renderer"),
        SurfaceTextureListener {
        private val mLock = ReentrantLock() // guards mSurfaceTexture, mDone
        private val condition  = mLock.newCondition()
        private var mSurfaceTexture: SurfaceTexture? = null
        private var mDone = false
        private var mWidth // from SurfaceTexture
                = 0
        private var mHeight = 0


        private var _drawCallback: DrawCallback? = null
        var drawCallback = _drawCallback
            set(value) {
                _drawCallback = value
                field = value
            }
        interface DrawCallback {
            fun draw(canvas: Canvas)
        }


        override fun run() {
            while (true) {
                var surfaceTexture: SurfaceTexture? = null

                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                mLock.withLock {
                    while (!mDone && mSurfaceTexture.also { surfaceTexture = it } == null) {
                        try {
                            condition.await()
                        } catch (ie: InterruptedException) {
                            throw RuntimeException(ie) // not expected
                        }
                    }
                }

                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation()
            }
        }

        /**
         * Draws updates as fast as the system will allow.
         *
         *
         * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
         * In previous (and future) releases, with the async queue, many of the frames we
         * render may be dropped.
         *
         *
         * The correct thing to do here is use Choreographer to schedule frame updates off
         * of vsync, but that's not nearly as much fun.
         */
        private fun doAnimation() {

            // Create a Surface for the SurfaceTexture.
            var surface: Surface?
            mLock.withLock {
                when (val surfaceTexture = mSurfaceTexture) {
                    null -> {
                        return
                    }
                    else -> surface = Surface(surfaceTexture)
                }
            }
            while (true) {
                val canvas: Canvas = surface?.lockCanvas(null) ?: break
                try {
                    drawCallback?.draw(canvas)
                } finally {
                    // Publish the frame.  If we overrun the consumer, frames will be dropped,
                    // so on a sufficiently fast device the animation will run at faster than
                    // the display refresh rate.
                    //
                    // If the SurfaceTexture has been destroyed, this will throw an exception.
                    try {
                        surface?.unlockCanvasAndPost(canvas)
                    } catch (iae: IllegalArgumentException) {
                        break
                    }
                }

            }
            surface?.release()
        }

        /**
         * Tells the thread to stop running.
         */
        fun halt() {
            mLock.withLock {
                mDone = true
                condition.signal()
            }
        }

        // will be called on UI thread
        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
            mWidth = width
            mHeight = height
            mLock.withLock {
                mSurfaceTexture = st
                condition.signal()
            }
        }

        // will be called on UI thread
        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
            mWidth = width
            mHeight = height
        }

        // will be called on UI thread
        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
            mLock.withLock { mSurfaceTexture = null }
            return true
        }

        // will be called on UI thread
        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {
            //Log.d(TAG, "onSurfaceTextureUpdated");
            
        }
    }
}