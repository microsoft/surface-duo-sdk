/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.surfaceduo.displaysample;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.device.dualscreen.layout.SurfaceDuoLayout;

public class JavaMainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.empty_activity);
//        findViewById<FrameLayout>(R.id.parent).addView(
//            SurfaceDuoLayout(this, SurfaceDuoLayout.Config().apply {
//                singleScreenLayoutId = R.layout.single_screen
//                dualScreenStartLayoutId = R.layout.dual_screen_start
//                dualScreenEndLayoutId = R.layout.dual_screen_end
//                dualLandscapeSingleLayoutId = R.layout.single_screen
//            })
//        )

//        SurfaceDuoLayout.Config config = new SurfaceDuoLayout.Config();
//        config.singleScreenLayoutId = R.layout.single_screen;
//        config.dualScreenStartLayoutId = R.layout.dual_screen_start;
//        config.dualScreenEndLayoutId = R.layout.dual_screen_end;
//        config.dualLandscapeSingleLayoutId = R.layout.single_screen;
//
//        ((FrameLayout) findViewById(R.id.parent)).addView(new SurfaceDuoLayout(this, config));
//
//        ((SurfaceDuoLayout) findViewById(R.id.surface_duo_layout))
//                .updateConfigCreator()
//                .singleScreenLayoutId(R.layout.single_screen)
//                .dualScreenStartLayoutId(R.layout.dual_screen_start)
//                .dualScreenEndLayoutId(R.layout.dual_screen_end)
//                .reInflate();

        // Replace configuration
//        findViewById<SurfaceDuoLayout>(R.id.surface_duo_layout)
//            .newConfigCreator()
//            .dualScreenStartLayoutId(R.layout.dual_screen_start)
//            .dualScreenEndLayoutId(R.layout.dual_screen_end)
//            .reInflate()

        // Update configuration
//        findViewById<SurfaceDuoLayout>(R.id.surface_duo_layout)
//            .updateConfigCreator()
//            .dualScreenStartLayoutId(R.layout.dual_screen_start)
//            .dualScreenEndLayoutId(R.layout.dual_screen_end)
//            .reInflate()

    }
}
