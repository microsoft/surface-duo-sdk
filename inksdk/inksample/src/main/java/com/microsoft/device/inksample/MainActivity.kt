/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.inksample

import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.microsoft.device.ink.InkView
import com.microsoft.device.ink.InkView.DynamicPaintHandler
import com.microsoft.device.ink.InputManager

class MainActivity : AppCompatActivity() {

    private lateinit var inkView: InkView
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inkView = findViewById(R.id.inkView)
        webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://en.wikipedia.org/wiki/Special:Random")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun clickClear(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.clearInk()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setRed(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.color = Color.RED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setGreen(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.color = Color.GREEN
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setBlue(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.color = Color.BLUE
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setBlack(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.color = Color.BLACK
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setYellow(@Suppress("UNUSED_PARAMETER")view: View) {
        inkView.color = Color.YELLOW
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun copyImage(view: View) {
        val image = view as ImageView
        image.setImageBitmap(inkView.saveBitmap())
    }

    fun fancySwitchChanged(view: View) {
        var switch = view as Switch
        if (switch.isChecked ){
            inkView.dynamicPaintHandler = FancyPaintHandler()
        } else {
            inkView.dynamicPaintHandler = null
        }
    }

    inner class FancyPaintHandler : DynamicPaintHandler {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun generatePaintFromPenInfo(penInfo: InputManager.PenInfo): Paint {
            var paint = Paint()
            val a = penInfo.pressure * 255


            paint.color = Color.argb(
                a.toInt(),
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
            paint.strokeCap = Paint.Cap.ROUND;

            return paint;
        }
    }

    fun webSwitchChanged(view: View) {
        var switch = view as Switch
        if (switch.isChecked ){
            this.webView.visibility = View.VISIBLE


        } else {
            this.webView.visibility = View.INVISIBLE
        }
    }

}