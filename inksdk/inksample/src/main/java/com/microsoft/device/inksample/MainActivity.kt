/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.inksample

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.ink.InkView

class MainActivity : AppCompatActivity() {

    private lateinit var inkView: InkView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inkView = findViewById(R.id.inkView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun clickClear() {
        inkView.clearInk()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setRed() {
        inkView.setColor(Color.valueOf(Color.RED))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setGreen() {
        inkView.setColor(Color.valueOf(Color.GREEN))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlue() {
        inkView.setColor(Color.valueOf(Color.BLUE))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setBlack() {
        inkView.setColor(Color.valueOf(Color.BLACK))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun copyImage(view: View) {
        val image = view as ImageView
        image.setImageBitmap(inkView.saveBitmap())
    }
}