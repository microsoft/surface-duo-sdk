package com.microsoft.device.dualscreen.screenmanager.tests.utils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.screenmanager.tests.R

class TransparentFirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_first)
        findViewById<Button>(R.id.start_button).setOnClickListener {
            startActivity(Intent(this, TransparentSecondActivity::class.java))
        }
    }
}