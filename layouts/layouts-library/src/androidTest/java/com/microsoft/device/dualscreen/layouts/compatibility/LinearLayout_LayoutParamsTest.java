/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.device.dualscreen.layouts.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.microsoft.device.dualscreen.layouts.SurfaceDuoLayout;
import com.microsoft.device.dualscreen.layouts.compatibility.util.XmlUtils;
import com.microsoft.device.dualscreen.layouts.test.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class LinearLayout_LayoutParamsTest {
    @Test
    public void testConstructor() throws XmlPullParserException, IOException {
        final Context context = InstrumentationRegistry.getTargetContext();
        XmlResourceParser p = context.getResources().getLayout(R.layout.linearlayout_layout2);

        XmlUtils.beginDocument(p, "com.microsoft.device.dualscreen.layouts.SurfaceDuoLayout");
        SurfaceDuoLayout.LayoutParams linearLayoutParams =
                new SurfaceDuoLayout.LayoutParams(context, p);
        assertEquals(LayoutParams.MATCH_PARENT, linearLayoutParams.width);
        assertEquals(LayoutParams.WRAP_CONTENT, linearLayoutParams.height);
        assertEquals(0.0f, linearLayoutParams.weight, 0.0f);
        assertEquals(-1, linearLayoutParams.gravity);

        linearLayoutParams = new SurfaceDuoLayout.LayoutParams(320, 240);
        assertEquals(320, linearLayoutParams.width);
        assertEquals(240, linearLayoutParams.height);
        assertEquals(0.0f, linearLayoutParams.weight, 0.0f);
        assertEquals(-1, linearLayoutParams.gravity);

        linearLayoutParams = new SurfaceDuoLayout.LayoutParams(360, 320, 0.4f);
        assertEquals(360, linearLayoutParams.width);
        assertEquals(320, linearLayoutParams.height);
        assertEquals(0.4f, linearLayoutParams.weight, 0.0f);
        assertEquals(-1, linearLayoutParams.gravity);

        LayoutParams layoutParams = new LayoutParams(200, 480);
        linearLayoutParams = new SurfaceDuoLayout.LayoutParams(layoutParams);
        assertEquals(200, linearLayoutParams.width);
        assertEquals(480, linearLayoutParams.height);
        assertEquals(0.0f, linearLayoutParams.weight, 0.0f);
        assertEquals(-1, linearLayoutParams.gravity);

        MarginLayoutParams marginLayoutParams = new MarginLayoutParams(320, 200);
        linearLayoutParams = new SurfaceDuoLayout.LayoutParams(marginLayoutParams);
        assertEquals(320, linearLayoutParams.width);
        assertEquals(200, linearLayoutParams.height);
        assertEquals(0.0f, linearLayoutParams.weight, 0.0f);
        assertEquals(-1, linearLayoutParams.gravity);

        SurfaceDuoLayout.LayoutParams linearLayoutParams2 = new SurfaceDuoLayout.LayoutParams(360, 720);
        linearLayoutParams2.weight = 0.9f;
        linearLayoutParams2.gravity = Gravity.RIGHT;
        linearLayoutParams = new SurfaceDuoLayout.LayoutParams(linearLayoutParams2);
        assertEquals(360, linearLayoutParams.width);
        assertEquals(720, linearLayoutParams.height);
        assertEquals(0.9f, linearLayoutParams.weight, 0.0f);
        assertEquals(Gravity.RIGHT, linearLayoutParams.gravity);
    }

    @Test
    public void testDebug() {
        SurfaceDuoLayout.LayoutParams layoutParams = new SurfaceDuoLayout.LayoutParams(320, 240);
        assertNotNull(layoutParams.debug("test: "));
    }
}
