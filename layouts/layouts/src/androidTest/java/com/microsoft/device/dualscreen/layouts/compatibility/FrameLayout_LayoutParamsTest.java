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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.microsoft.device.dualscreen.layouts.FoldableFrameLayout;
import com.microsoft.device.dualscreen.layouts.test.R;
import com.microsoft.device.dualscreen.layouts.compatibility.util.WidgetTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class FrameLayout_LayoutParamsTest {
    private Context mContext;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    private AttributeSet getAttributeSet() throws XmlPullParserException, IOException {
        XmlPullParser parser = mContext.getResources().getLayout(R.layout.framelayout_layout);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        return Xml.asAttributeSet(parser);
    }

    @Test
    public void testConstructor() throws XmlPullParserException, IOException {
        AttributeSet attrs = getAttributeSet();

        new FoldableFrameLayout.LayoutParams(mContext, attrs);
        new FoldableFrameLayout.LayoutParams(FoldableFrameLayout.LayoutParams.MATCH_PARENT,
                FoldableFrameLayout.LayoutParams.MATCH_PARENT);
        new FoldableFrameLayout.LayoutParams(FoldableFrameLayout.LayoutParams.WRAP_CONTENT,
                FoldableFrameLayout.LayoutParams.WRAP_CONTENT, 0);
        new FoldableFrameLayout.LayoutParams(new ViewGroup.LayoutParams(mContext, attrs));
        new FoldableFrameLayout.LayoutParams(new FoldableFrameLayout.LayoutParams(mContext, attrs));
        new FoldableFrameLayout.LayoutParams(new MarginLayoutParams(mContext, attrs));

        new FoldableFrameLayout.LayoutParams(-1, -1);
        new FoldableFrameLayout.LayoutParams(-1, -1, -1);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullContext() {
        new FoldableFrameLayout.LayoutParams(null, null);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullViewGroupParams() {
        new FoldableFrameLayout.LayoutParams((ViewGroup.LayoutParams) null);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullViewGroupMarginParams() {
        new FoldableFrameLayout.LayoutParams((ViewGroup.MarginLayoutParams) null);
    }

    @Test
    public void testCopyConstructor() {
        FoldableFrameLayout.LayoutParams copy;

        final FoldableFrameLayout.LayoutParams fllp = new FoldableFrameLayout.LayoutParams(
                FoldableFrameLayout.LayoutParams.MATCH_PARENT,
                FoldableFrameLayout.LayoutParams.MATCH_PARENT
        );
        fllp.gravity = Gravity.BOTTOM;
        fllp.leftMargin = 5;
        fllp.topMargin = 10;
        fllp.rightMargin = 15;
        fllp.bottomMargin = 20;

        copy = new FoldableFrameLayout.LayoutParams(fllp);
        assertEquals("Width", fllp.width, copy.width);
        assertEquals("Height", fllp.height, copy.height);
        assertEquals("Gravity", fllp.gravity, copy.gravity);
        assertEquals("Left margin", fllp.leftMargin, copy.leftMargin);
        assertEquals("Top margin", fllp.topMargin, copy.topMargin);
        assertEquals("Right margin", fllp.rightMargin, copy.rightMargin);
        assertEquals("Bottom margin", fllp.bottomMargin, copy.bottomMargin);

        final MarginLayoutParams mlp = new MarginLayoutParams(
                FoldableFrameLayout.LayoutParams.MATCH_PARENT,
                FoldableFrameLayout.LayoutParams.MATCH_PARENT
        );
        mlp.leftMargin = 5;
        mlp.topMargin = 10;
        mlp.rightMargin = 15;
        mlp.bottomMargin = 20;

        copy = new FoldableFrameLayout.LayoutParams(mlp);
        assertEquals("Width", mlp.width, copy.width);
        assertEquals("Height", mlp.height, copy.height);
        assertEquals("Left margin", fllp.leftMargin, copy.leftMargin);
        assertEquals("Top margin", fllp.topMargin, copy.topMargin);
        assertEquals("Right margin", fllp.rightMargin, copy.rightMargin);
        assertEquals("Bottom margin", fllp.bottomMargin, copy.bottomMargin);

        final ViewGroup.LayoutParams vglp = new ViewGroup.LayoutParams(
                FoldableFrameLayout.LayoutParams.MATCH_PARENT,
                FoldableFrameLayout.LayoutParams.MATCH_PARENT
        );

        copy = new FoldableFrameLayout.LayoutParams(vglp);
        assertEquals("Width", vglp.width, copy.width);
        assertEquals("Height", vglp.height, copy.height);
    }
}
