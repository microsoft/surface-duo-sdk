/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.inksample

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.isVisible
import com.microsoft.device.ink.InkView
import com.microsoft.device.ink.InkView.DynamicPaintHandler
import com.microsoft.device.ink.InputManager

class MainActivity : AppCompatActivity() {

    private lateinit var inkView: InkView
    private lateinit var webView: WebView
    private lateinit var fancySwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inkView = findViewById(R.id.inkView)
        webView = findViewById<WebView>(R.id.webView)
        fancySwitch = findViewById<Switch>(R.id.fancySwitch)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://en.wikipedia.org/wiki/Special:Random")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
    }

    fun clickClear(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.clearInk()
    }

    fun setRed(@Suppress("UNUSED_PARAMETER")view: View) {
        resetPaintHandler()
        inkView.color = Color.RED
    }

    fun setGreen(@Suppress("UNUSED_PARAMETER")view: View) {
        resetPaintHandler()
        inkView.color = Color.GREEN
    }

    fun setBlue(@Suppress("UNUSED_PARAMETER")view: View) {
        resetPaintHandler()
        inkView.color = Color.BLUE
    }

    fun setBlack(@Suppress("UNUSED_PARAMETER")view: View) {
        resetPaintHandler()
        inkView.color = Color.BLACK
    }

    fun setYellow(@Suppress("UNUSED_PARAMETER")view: View) {
        resetPaintHandler()
        inkView.color = Color.YELLOW
    }

    fun setRainbow(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.dynamicPaintHandler = RainbowPaintHandler()
        fancySwitch.isChecked = false
    }
    /**
     * Required because some colors are set on the InkView, but
     * rainbow ink is controlled via a paint handler (which is also
     * how the pressure handler works, but it modifies color)
     */
    fun resetPaintHandler() {
        if (fancySwitch.isChecked) {
            inkView.dynamicPaintHandler = FancyPaintHandler()
        } else {
            inkView.dynamicPaintHandler = null
        }
    }

    fun copyImage(view: View) {
        val image = view as ImageView
        image.setImageBitmap(inkView.saveBitmap())
    }

    fun fancySwitchChanged(view: View) {
        var switch = view as Switch
        if (switch.isChecked) {
            inkView.dynamicPaintHandler = FancyPaintHandler()
            // inkView.dynamicPaintHandler = HighlighterPaintHandler()
        } else {
            inkView.dynamicPaintHandler = null
        }
    }

    /**
     * Renders the ink with transparency linked to the pressure on the pen.
     */
    inner class FancyPaintHandler : DynamicPaintHandler {
        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            var paint = Paint()
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
                penInfo.pressure * 10 + 5,
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
            var paint = Paint()
            val alpha = 80

            paint.color = Color.argb(
                alpha.toInt(),
                Color.YELLOW.red,
                Color.YELLOW.green,
                Color.YELLOW.blue
            )
            paint.isAntiAlias = true
            // Set stroke width based on display density.
            paint.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                penInfo.pressure * 10 + 5,
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
        var frequency = .3
        var steps = 20
        var i = 0
        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            var paint = Paint()

            if (i > steps) i = 0 else i++
            paint.color = Color.argb(
                255,
                (Math.sin(frequency * i + 0) * 127 + 128).toInt(),
                (Math.sin(frequency * i + 2) * 127 + 128).toInt(),
                (Math.sin(frequency * i + 4) * 127 + 128).toInt()
            )
            paint.isAntiAlias = true
            // Set stroke width based on display density.
            paint.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                penInfo.pressure * 10 + 5,
                resources.displayMetrics
            )
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND

            return paint
        }
    }

    fun webSwitchChanged(view: View) {
        var switch = view as Switch
        this.webView.isVisible = switch.isChecked
    }
}