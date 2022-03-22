/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.navigation.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.FoldableNavController
import androidx.navigation.fragment.FoldableNavHostFragment
import androidx.navigation.ui.FoldableAppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithFoldableNavController
import com.microsoft.device.dualscreen.navigation.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: FoldableAppBarConfiguration

    private val navController: FoldableNavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as FoldableNavHostFragment
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)
        appBarConfiguration = FoldableAppBarConfiguration(navController.graph)
        setupActionBarWithFoldableNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) ||
            super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (!navController.navigateUp()) {
            finish()
        }
    }
}
