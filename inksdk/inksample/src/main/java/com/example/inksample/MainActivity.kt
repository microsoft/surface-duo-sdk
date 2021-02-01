/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.example.inksample

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.ink.InkView

class MainActivity : AppCompatActivity() {

    var inkView: InkView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inkView = findViewById(R.id.inkView)
    }

    fun clickClear(view: View) {
        inkView!!.clearInk()
    }

    fun setRed(view: View) {
        inkView!!.setColor(Color.valueOf(Color.RED))
    }

    fun setGreen(view: View) {
        inkView!!.setColor(Color.valueOf(Color.GREEN))
    }

    fun setBlue(view: View) {
        inkView!!.setColor(Color.valueOf(Color.BLUE))
    }

    fun setBlack(view: View) {
        inkView!!.setColor(Color.valueOf(Color.BLACK))
    }

    fun copyImage(view: View) {
        val image = view as ImageView
        image.setImageBitmap(inkView!!.saveBitmap())
    }
}