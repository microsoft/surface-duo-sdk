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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.UiThreadTest;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.microsoft.device.dualscreen.layouts.SurfaceDuoLayout;
import com.microsoft.device.dualscreen.layouts.compatibility.activities.LinearLayoutCtsActivity;
import com.microsoft.device.dualscreen.layouts.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test {@link SurfaceDuoLayout}.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class LinearLayoutTest {
    private Instrumentation mInstrumentation;
    private Activity mActivity;

    @Rule
    public ActivityTestRule<LinearLayoutCtsActivity> mActivityRule =
            new ActivityTestRule<>(LinearLayoutCtsActivity.class);

    @Before
    public void setup() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        mActivity = mActivityRule.getActivity();
    }

    @Test
    public void testConstructor() {
        new SurfaceDuoLayout(mActivity);

        new SurfaceDuoLayout(mActivity, (AttributeSet) null);

        XmlPullParser parser = mActivity.getResources().getXml(R.layout.linearlayout_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new SurfaceDuoLayout(mActivity, attrs);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullContext() {
        new SurfaceDuoLayout(null, (AttributeSet) null);
    }

    @UiThreadTest
    @Test
    public void testAccessBaselineAligned() {
        SurfaceDuoLayout parent = (SurfaceDuoLayout) mActivity.findViewById(R.id.linear_empty);
        parent.setBaselineAligned(true);
        assertTrue(parent.isBaselineAligned());

        parent.setBaselineAligned(false);
        assertFalse(parent.isBaselineAligned());

        // android:baselineAligned="false" in LinearLayout weightsum
        parent = (SurfaceDuoLayout) mActivity.findViewById(R.id.linear_weightsum);
        assertFalse(parent.isBaselineAligned());

        // default mBaselineAligned is true.
        parent = (SurfaceDuoLayout) mActivity.findViewById(R.id.linear_horizontal);
        assertTrue(parent.isBaselineAligned());
    }

    @UiThreadTest
    @Test
    public void testGetBaseline() {
        SurfaceDuoLayout parent = (SurfaceDuoLayout) mActivity.findViewById(R.id.linear_empty);

        ListView lv1 = new ListView(mActivity);
        parent.addView(lv1);
        assertEquals(-1, parent.getBaseline());

        ListView lv2 = new ListView(mActivity);
        parent.addView(lv2);
        parent.setBaselineAlignedChildIndex(2);
        try {
            parent.getBaseline();
            fail("LinearLayout.getBaseline() should throw exception here.");
        } catch (RuntimeException e) {
        }

        ListView lv3 = new MockListView(mActivity);
        parent.addView(lv3);
        parent.setBaselineAlignedChildIndex(3);
        assertEquals(lv3.getBaseline(), parent.getBaseline());
    }

    @UiThreadTest
    @Test
    public void testWeightDistribution() {
        SurfaceDuoLayout parent = (SurfaceDuoLayout) mActivity.findViewById(R.id.linear_empty);
        parent.removeAllViews();

        for (int i = 0; i < 3; i++) {
            parent.addView(new View(mActivity), new SurfaceDuoLayout.LayoutParams(0, 0, 1));
        }

        int size = 100;
        int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        for (int i = 0; i < 3; i++) {
            View child = parent.getChildAt(i);
            SurfaceDuoLayout.LayoutParams lp = (SurfaceDuoLayout.LayoutParams) child.getLayoutParams();
            lp.height = 0;
            lp.width = SurfaceDuoLayout.LayoutParams.MATCH_PARENT;
            child.setLayoutParams(lp);
        }
        parent.setOrientation(SurfaceDuoLayout.VERTICAL);
        parent.measure(spec, spec);
        parent.layout(0, 0, size, size);
        assertEquals(100, parent.getWidth());
        assertEquals(100, parent.getChildAt(0).getWidth());
        assertEquals(100, parent.getChildAt(1).getWidth());
        assertEquals(100, parent.getChildAt(2).getWidth());
        assertEquals(100, parent.getHeight());
        assertEquals(33, parent.getChildAt(0).getHeight());
        assertEquals(33, parent.getChildAt(1).getHeight());
        assertEquals(34, parent.getChildAt(2).getHeight());

        for (int i = 0; i < 3; i++) {
            View child = parent.getChildAt(i);
            SurfaceDuoLayout.LayoutParams lp = (SurfaceDuoLayout.LayoutParams) child.getLayoutParams();
            lp.height = SurfaceDuoLayout.LayoutParams.MATCH_PARENT;
            lp.width = 0;
            child.setLayoutParams(lp);
        }
        parent.setOrientation(SurfaceDuoLayout.HORIZONTAL);
        parent.measure(spec, spec);
        parent.layout(0, 0, size, size);
        assertEquals(100, parent.getWidth());
        assertEquals(33, parent.getChildAt(0).getWidth());
        assertEquals(33, parent.getChildAt(1).getWidth());
        assertEquals(34, parent.getChildAt(2).getWidth());
        assertEquals(100, parent.getHeight());
        assertEquals(100, parent.getChildAt(0).getHeight());
        assertEquals(100, parent.getChildAt(1).getHeight());
        assertEquals(100, parent.getChildAt(2).getHeight());
    }

    @UiThreadTest
    @Test
    public void testGenerateLayoutParams() {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(320, 240);
        MockLinearLayout parent = (MockLinearLayout) mActivity.findViewById(R.id.linear_custom);
        SurfaceDuoLayout.LayoutParams layoutParams1 = parent.generateLayoutParams(lp);
        assertEquals(320, layoutParams1.width);
        assertEquals(240, layoutParams1.height);
    }

    @UiThreadTest
    @Test
    public void testCheckLayoutParams() {
        MockLinearLayout parent = (MockLinearLayout) mActivity.findViewById(R.id.linear_custom);

        ViewGroup.LayoutParams params = new AbsoluteLayout.LayoutParams(240, 320, 0, 0);
        assertFalse(parent.checkLayoutParams(params));

        params = new SurfaceDuoLayout.LayoutParams(240, 320);
        assertTrue(parent.checkLayoutParams(params));
    }

    @UiThreadTest
    @Test
    public void testGenerateDefaultLayoutParams() {
        MockLinearLayout parent = (MockLinearLayout) mActivity.findViewById(R.id.linear_custom);

        parent.setOrientation(SurfaceDuoLayout.HORIZONTAL);
        ViewGroup.LayoutParams param = parent.generateDefaultLayoutParams();
        assertNotNull(param);
        assertTrue(param instanceof SurfaceDuoLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.height);

        parent.setOrientation(SurfaceDuoLayout.VERTICAL);
        param = parent.generateDefaultLayoutParams();
        assertNotNull(param);
        assertTrue(param instanceof SurfaceDuoLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, param.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.height);

        parent.setOrientation(-1);
        assertNull(parent.generateDefaultLayoutParams());
    }

    @UiThreadTest
    @Test
    public void testGenerateLayoutParamsFromMarginParams() {
        MockLinearLayout parent = (MockLinearLayout) mActivity.findViewById(R.id.linear_custom);

        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(3, 5);
        lp.leftMargin = 1;
        lp.topMargin = 2;
        lp.rightMargin = 3;
        lp.bottomMargin = 4;
        SurfaceDuoLayout.LayoutParams generated = parent.generateLayoutParams(lp);
        assertNotNull(generated);
        assertEquals(3, generated.width);
        assertEquals(5, generated.height);

        assertEquals(1, generated.leftMargin);
        assertEquals(2, generated.topMargin);
        assertEquals(3, generated.rightMargin);
        assertEquals(4, generated.bottomMargin);
    }

    private class MockListView extends ListView {
        private final static int DEFAULT_CHILD_BASE_LINE = 1;

        public MockListView(Context context) {
            super(context);
        }

        public int getBaseline() {
            return DEFAULT_CHILD_BASE_LINE;
        }
    }

    /**
     * Add MockLinearLayout to help for testing protected methods in LinearLayout.
     * Because we can not access protected methods in LinearLayout directly, we have to
     * extends from it and override protected methods so that we can access them in
     * our test codes.
     */
    public static class MockLinearLayout extends SurfaceDuoLayout {
        public MockLinearLayout(Context c) {
            super(c);
        }

        public MockLinearLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return super.checkLayoutParams(p);
        }

        @Override
        protected SurfaceDuoLayout.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }

        @Override
        protected SurfaceDuoLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return super.generateLayoutParams(p);
        }
    }
}
