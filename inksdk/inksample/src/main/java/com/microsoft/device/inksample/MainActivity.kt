/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.inksample

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.isVisible
import com.microsoft.device.ink.InkView
import com.microsoft.device.ink.InkView.DynamicPaintHandler
import com.microsoft.device.ink.InputManager
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var inkView: InkView
    private lateinit var webView: WebView
    private lateinit var fancySwitch: SwitchCompat
    private lateinit var seekBar: SeekBar
    private lateinit var circleView: ImageView

    private var savedInk: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inkView = findViewById(R.id.inkView)
        webView = findViewById(R.id.webView)
        fancySwitch = findViewById(R.id.fancySwitch)
        seekBar = findViewById(R.id.seekBar)
        circleView = findViewById(R.id.circleView)

        setupClickListeners()

        webView.webViewClient = WebViewClient()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.loadUrl("https://en.wikipedia.org/wiki/Special:Random")

        seekBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    setStrokeWidth()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            },
        )

        setStrokeWidth()
    }

    fun setStrokeWidth() {
        val layoutParams: ViewGroup.LayoutParams = circleView.layoutParams
        val factor = resources.displayMetrics.density
        layoutParams.width = (seekBar.progress * factor).toInt()
        layoutParams.height = (seekBar.progress * factor).toInt()
        circleView.layoutParams = layoutParams

        if (inkView.pressureEnabled) {
            inkView.strokeWidth = seekBar.progress / STROKE_MAX_MIN_RATIO
            inkView.strokeWidthMax = seekBar.progress.toFloat()
        } else {
            inkView.strokeWidth = seekBar.progress.toFloat()
            inkView.strokeWidthMax = seekBar.progress.toFloat()
        }
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnClear)?.setOnClickListener(::clickClear)
        findViewById<Button>(R.id.btnSave)?.setOnClickListener(::saveInk)
        findViewById<Button>(R.id.btnLoad)?.setOnClickListener(::loadInk)
        findViewById<ImageView>(R.id.redColorPick)?.setOnClickListener(::setRed)
        findViewById<ImageView>(R.id.greenColorPick)?.setOnClickListener(::setGreen)
        findViewById<ImageView>(R.id.blueColorPick)?.setOnClickListener(::setBlue)
        findViewById<ImageView>(R.id.yellowColorPick)?.setOnClickListener(::setYellow)
        findViewById<ImageView>(R.id.blackColorPick)?.setOnClickListener(::setBlack)
        findViewById<ImageView>(R.id.rainbowColorPick)?.setOnClickListener(::setRainbow)
        fancySwitch.setOnClickListener(::fancySwitchChanged)
        findViewById<SwitchCompat>(R.id.webSwitch)?.setOnClickListener(::webSwitchChanged)
        findViewById<ImageView>(R.id.imageCopy)?.setOnClickListener(::copyImage)
    }

    private fun clickClear(@Suppress("UNUSED_PARAMETER") view: View) {
        inkView.clearInk()
    }

    private fun setRed(@Suppress("UNUSED_PARAMETER") view: View) {
        resetPaintHandler()
        inkView.color = Color.RED
    }

    private fun setGreen(@Suppress("UNUSED_PARAMETER") view: View) {
        resetPaintHandler()
        inkView.color = Color.GREEN
    }

    private fun setBlue(@Suppress("UNUSED_PARAMETER") view: View) {
        resetPaintHandler()
        inkView.color = Color.BLUE
    }

    private fun setBlack(@Suppress("UNUSED_PARAMETER") view: View) {
        resetPaintHandler()
        inkView.color = Color.BLACK
    }

    private fun setYellow(@Suppress("UNUSED_PARAMETER") view: View) {
        resetPaintHandler()
        inkView.color = Color.YELLOW
    }

    private fun setRainbow(@Suppress("UNUSED_PARAMETER") view: View) {
        inkView.dynamicPaintHandler = RainbowPaintHandler()
        fancySwitch.isChecked = false
    }

    /**
     * Required because some colors are set on the InkView, but
     * rainbow ink is controlled via a paint handler (which is also
     * how the pressure handler works, but it modifies color)
     */
    private fun resetPaintHandler() {
        if (fancySwitch.isChecked) {
            inkView.dynamicPaintHandler = FancyPaintHandler()
        } else {
            inkView.dynamicPaintHandler = null
        }
    }

    private fun copyImage(view: View) {
        val image = view as ImageView
        image.setImageBitmap(inkView.saveBitmap())
    }

    private fun saveInk(view: View) {
        savedInk = inkView.saveInk()
    }

    private fun loadInk(view: View) {
        if (savedInk.isNotBlank()) {
            inkView.loadInk(savedInk)
        }
    }

    private fun fancySwitchChanged(view: View) {
        val switch = view as SwitchCompat
        if (switch.isChecked) {
            inkView.dynamicPaintHandler = FancyPaintHandler()
        } else {
            inkView.dynamicPaintHandler = null
        }
    }

    /**
     * Renders the ink with transparency linked to the pressure on the pen.
     */
    inner class FancyPaintHandler : DynamicPaintHandler {
        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            val paint = Paint()
            val alpha = penInfo.pressure * 255

            paint.color = Color.argb(
                alpha.toInt(),
                inkView.color.red,
                inkView.color.green,
                inkView.color.blue
            )
            paint.isAntiAlias = true
            // Set stroke width based on display density.
            paint.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                penInfo.pressure * (inkView.strokeWidthMax - inkView.strokeWidth) + inkView.strokeWidth,
                resources.displayMetrics
            )
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND

            return paint
        }
    }

    /**
     * Renders the ink as though from a highlighter: permanently transparent
     * and yellow-colored.
     */
    inner class HighlighterPaintHandler : DynamicPaintHandler {
        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            val paint = Paint()
            val alpha = 80

            paint.color = Color.argb(
                alpha,
                Color.YELLOW.red,
                Color.YELLOW.green,
                Color.YELLOW.blue
            )
            paint.isAntiAlias = true
            // Set stroke width based on display density.
            paint.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                penInfo.pressure * (inkView.strokeWidthMax - inkView.strokeWidth) + inkView.strokeWidth,
                resources.displayMetrics
            )
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.BEVEL
            paint.strokeCap = Paint.Cap.BUTT

            return paint
        }
    }

    /**
     * Renders the ink as a continually changing color line
     * with a rainbow effect (similar to Microsoft Whiteboard)
     */
    inner class RainbowPaintHandler : DynamicPaintHandler {
        // frequency = 2*Math.PI/steps
        private val frequency = .0375
        private val steps = 160
        private var i = 0
        private val amplitude = 127
        private val volume = 128
        private val rPhase = 0
        private val gPhase = 2
        private val bPhase = 4

        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            val paint = Paint()

            if (i > steps) i = 0 else i++
            paint.color = Color.argb(
                255,
                (sin(frequency * i + rPhase) * amplitude + volume).toInt(),
                (sin(frequency * i + gPhase) * amplitude + volume).toInt(),
                (sin(frequency * i + bPhase) * amplitude + volume).toInt()
            )
            paint.isAntiAlias = true
            // Set stroke width based on display density.
            paint.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                penInfo.pressure * (inkView.strokeWidthMax - inkView.strokeWidth) + inkView.strokeWidth,
                resources.displayMetrics
            )
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND

            return paint
        }
    }

    private fun webSwitchChanged(view: View) {
        val switch = view as SwitchCompat
        this.webView.isVisible = switch.isChecked
    }

    companion object {
        const val STROKE_MAX_MIN_RATIO = 10f
    }
}