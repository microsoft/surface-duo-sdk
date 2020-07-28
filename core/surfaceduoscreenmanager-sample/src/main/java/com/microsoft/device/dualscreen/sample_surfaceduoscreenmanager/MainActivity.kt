/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.sample_surfaceduoscreenmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.core.manager.ScreenModeListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as SampleApp).surfaceDuoScreenManager.addScreenModeListener(
            this,
            object : ScreenModeListener {
                override fun onSwitchToSingleScreen() {
                    Toast.makeText(
                        this@MainActivity,
                        "Single Screen Mode",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onSwitchToDualScreen() {
                    Toast.makeText(
                        this@MainActivity,
                        "Dual Screen Mode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}
