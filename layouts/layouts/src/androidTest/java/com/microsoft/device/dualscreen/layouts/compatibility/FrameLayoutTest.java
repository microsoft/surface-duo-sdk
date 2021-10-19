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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.UiThreadTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.microsoft.device.dualscreen.layouts.FoldableFrameLayout;
import com.microsoft.device.dualscreen.layouts.compatibility.util.PollingCheck;
import com.microsoft.device.dualscreen.layouts.compatibility.util.WidgetTestUtils;
import com.microsoft.device.dualscreen.layouts.test.R;
import com.microsoft.device.dualscreen.layouts.compatibility.activities.FrameLayoutCtsActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class FrameLayoutTest {
    private Instrumentation mInstrumentation;
    private Activity mActivity;
    private FoldableFrameLayout mFrameLayout;

    @Rule
    public ActivityTestRule<FrameLayoutCtsActivity> mActivityRule =
            new ActivityTestRule<>(FrameLayoutCtsActivity.class);

    @Before
    public void setup() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        mActivity = mActivityRule.getActivity();
        mFrameLayout = (FoldableFrameLayout) mActivity.findViewById(R.id.framelayout);
    }

    @Test
    public void testConstructor() throws XmlPullParserException, IOException {
        AttributeSet attrs = getAttributeSet();

        new FoldableFrameLayout(mActivity);
        new FoldableFrameLayout(mActivity, attrs);
        new FoldableFrameLayout(mActivity, attrs, 0);
    }

    @Test
    public void testSetForegroundGravity() throws Throwable {
        final BitmapDrawable foreground
                = (BitmapDrawable) mActivity.getResources().getDrawable(R.drawable.size_48x48);
        WidgetTestUtils.assertScaledPixels(48, foreground.getIntrinsicHeight(), mActivity);
        WidgetTestUtils.assertScaledPixels(48, foreground.getIntrinsicWidth(), mActivity);
        assertTrue(mFrameLayout.getHeight() > foreground.getIntrinsicHeight());
        assertTrue(mFrameLayout.getWidth() > foreground.getIntrinsicWidth());
        assertNull(mFrameLayout.getForeground());

        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, mFrameLayout,
                () -> mFrameLayout.setForeground(foreground));
        assertSame(foreground, mFrameLayout.getForeground());
        // check the default gravity FILL, it completely fills its container
        assertTrue(foreground.isVisible());
        final Rect rect = foreground.getBounds();
        // foreground has been stretched
        PollingCheck.waitFor(() -> mFrameLayout.getHeight() == rect.bottom - rect.top);
        assertEquals(mFrameLayout.getWidth(), rect.right - rect.left);

        // should get a new foreground again, because former foreground has been stretched
        final BitmapDrawable newForeground =
                (BitmapDrawable) mActivity.getDrawable(R.drawable.size_48x48);
        WidgetTestUtils.assertScaledPixels(48, newForeground.getIntrinsicHeight(), mActivity);
        WidgetTestUtils.assertScaledPixels(48, newForeground.getIntrinsicWidth(), mActivity);
        assertTrue(mFrameLayout.getHeight() > newForeground.getIntrinsicHeight());
        assertTrue(mFrameLayout.getWidth() > foreground.getIntrinsicWidth());

        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, mFrameLayout, () -> {
            mFrameLayout.setForeground(newForeground);
            mFrameLayout.setForegroundGravity(Gravity.CENTER);
        });
        assertSame(newForeground, mFrameLayout.getForeground());
        assertTrue(newForeground.isVisible());
        Rect rect2 = newForeground.getBounds();
        // not changing its size
        assertEquals(foreground.getIntrinsicHeight(), rect2.bottom - rect2.top);
        assertEquals(foreground.getIntrinsicWidth(), rect2.right - rect2.left);
        assertCenterAligned(mFrameLayout, newForeground);
    }

    @Test
    public void testGatherTransparentRegion() throws Throwable {
        final LinearLayout container =
                (LinearLayout) mActivity.findViewById(R.id.framelayout_container);
        final Drawable foreground = mActivity.getResources().getDrawable(R.drawable.size_48x48);
        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, mFrameLayout, () -> {
            mFrameLayout.setForeground(foreground);
            mFrameLayout.setForegroundGravity(Gravity.CENTER);
        });
        Region region = new Region(foreground.getBounds());
        assertTrue(mFrameLayout.gatherTransparentRegion(region));

        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, mFrameLayout,
                () -> container.requestTransparentRegion(mFrameLayout));
        region = new Region(foreground.getBounds());
        assertTrue(mFrameLayout.gatherTransparentRegion(region));
    }

    @Test
    public void testAccessMeasureAllChildren() throws Throwable {
        final FoldableFrameLayout frameLayout
                = (FoldableFrameLayout) mActivity.findViewById(R.id.framelayout_measureall);
        assertFalse(frameLayout.getConsiderGoneChildrenWhenMeasuring());

        // text view and button are VISIBLE, they should be measured
        final TextView textView = (TextView) frameLayout.findViewById(R.id.framelayout_textview);
        WidgetTestUtils.assertScaledPixels(30, textView.getMeasuredHeight(), mActivity);
        WidgetTestUtils.assertScaledPixels(60, textView.getMeasuredWidth(), mActivity);
        assertEquals(textView.getMeasuredHeight(), frameLayout.getMeasuredHeight());
        assertEquals(textView.getMeasuredWidth(), frameLayout.getMeasuredWidth());

        // measureAll is false and text view is GONE, text view will NOT be measured
        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, frameLayout, () -> {
            textView.setVisibility(View.GONE);
            frameLayout.requestLayout();
        });
        assertFalse(frameLayout.getConsiderGoneChildrenWhenMeasuring());
        Button button = (Button) frameLayout.findViewById(R.id.framelayout_button);
        WidgetTestUtils.assertScaledPixels(15, button.getMeasuredHeight(), mActivity);
        WidgetTestUtils.assertScaledPixels(50, button.getMeasuredWidth(), mActivity);
        assertEquals(button.getMeasuredHeight(), frameLayout.getMeasuredHeight());
        assertEquals(button.getMeasuredWidth(), frameLayout.getMeasuredWidth());

        // measureAll is true and text view is GONE, text view will be measured
        WidgetTestUtils.runOnMainAndDrawSync(mActivityRule, frameLayout, () -> {
            frameLayout.setMeasureAllChildren(true);
            frameLayout.requestLayout();
        });
        assertTrue(frameLayout.getMeasureAllChildren());
        assertTrue(frameLayout.getConsiderGoneChildrenWhenMeasuring());
        assertEquals(textView.getMeasuredHeight(), frameLayout.getMeasuredHeight());
        assertEquals(textView.getMeasuredWidth(), frameLayout.getMeasuredWidth());
    }

    @Test
    public void testGenerateLayoutParams1() {
        MyFrameLayout myFrameLayout = new MyFrameLayout(mActivity);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        ViewGroup.LayoutParams params = myFrameLayout.generateLayoutParams(p);
        assertNotNull(params);
        assertTrue(params instanceof FoldableFrameLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, params.height);
    }

    @Test
    public void testGenerateLayoutParams2() throws XmlPullParserException, IOException {
        AttributeSet attrs = getAttributeSet();

        FoldableFrameLayout.LayoutParams params = mFrameLayout.generateLayoutParams(attrs);
        assertNotNull(params);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, params.height);
        assertEquals(Gravity.BOTTOM, params.gravity);
    }

    @Test
    public void testCheckLayoutParams() {
        MyFrameLayout myFrameLayout = new MyFrameLayout(mActivity);
        assertFalse(myFrameLayout.checkLayoutParams(null));

        int width = 120;
        int height = 80;
        ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(width, height);
        assertFalse(myFrameLayout.checkLayoutParams(params1));

        FoldableFrameLayout.LayoutParams params2 = new FoldableFrameLayout.LayoutParams(width, height);
        assertTrue(myFrameLayout.checkLayoutParams(params2));
    }

    @Test
    public void testGenerateLayoutParamsFromMarginParams() {
        MyFrameLayout myFrameLayout = new MyFrameLayout(mActivity);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(3, 5);
        lp.leftMargin = 1;
        lp.topMargin = 2;
        lp.rightMargin = 3;
        lp.bottomMargin = 4;
        FoldableFrameLayout.LayoutParams generated =
                (FoldableFrameLayout.LayoutParams) myFrameLayout.generateLayoutParams(lp);
        assertNotNull(generated);
        assertEquals(3, generated.width);
        assertEquals(5, generated.height);

        assertEquals(1, generated.leftMargin);
        assertEquals(2, generated.topMargin);
        assertEquals(3, generated.rightMargin);
        assertEquals(4, generated.bottomMargin);
    }

    @Test
    public void testGenerateDefaultLayoutParams() {
        MyFrameLayout frameLayout = new MyFrameLayout(mActivity);
        FoldableFrameLayout.LayoutParams params = frameLayout.generateDefaultLayoutParams();

        assertNotNull(params);
        assertEquals(FoldableFrameLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(FoldableFrameLayout.LayoutParams.MATCH_PARENT, params.height);
    }

    @Test
    public void testVerifyDrawable() {
        MyFrameLayout myFrameLayout = new MyFrameLayout(mActivity);

        Drawable drawable = mActivity.getResources().getDrawable(R.drawable.scenery);
        myFrameLayout.setForeground(drawable);
        assertTrue(myFrameLayout.verifyDrawable(drawable));

        drawable = mActivity.getResources().getDrawable(R.drawable.black);
        assertFalse(myFrameLayout.verifyDrawable(drawable));

        assertTrue(myFrameLayout.verifyDrawable(null));
    }

    @UiThreadTest
    @Test
    public void testForegroundTint() {
        FoldableFrameLayout inflatedView = (FoldableFrameLayout) mActivity.findViewById(R.id.foreground_tint);

        assertEquals("Foreground tint inflated correctly",
                Color.WHITE, inflatedView.getForegroundTintList().getDefaultColor());
        assertEquals("Foreground tint mode inflated correctly",
                PorterDuff.Mode.SRC_OVER, inflatedView.getForegroundTintMode());

        final Drawable foreground = spy(new ColorDrawable());
        FoldableFrameLayout view = new FoldableFrameLayout(mActivity);

        view.setForeground(foreground);
        verify(foreground, never()).setTintList(any(ColorStateList.class));

        view.setForegroundTintList(ColorStateList.valueOf(Color.RED));
        final ArgumentCaptor<ColorStateList> colorStateListCaptor =
                ArgumentCaptor.forClass(ColorStateList.class);
        verify(foreground, times(1)).setTintList(colorStateListCaptor.capture());
        int[] emptyState = new int[0];
        assertEquals(Color.RED,
                colorStateListCaptor.getValue().getColorForState(emptyState, Color.BLUE));

        reset(foreground);
        view.setForeground(null);
        view.setForeground(foreground);
        verify(foreground, times(1)).setTintList(colorStateListCaptor.capture());
        assertEquals(Color.RED,
                colorStateListCaptor.getValue().getColorForState(emptyState, Color.BLUE));
    }

    private static void assertCenterAligned(View container, Drawable drawable) {
        Rect rect = drawable.getBounds();
        int leftDelta = rect.left - container.getLeft();
        int rightDelta = container.getRight() - rect.right;
        int topDelta = rect.top - container.getTop();
        int bottomDelta = container.getBottom() - rect.bottom;

        assertTrue(Math.abs(leftDelta - rightDelta) <= 1);
        assertTrue(Math.abs(topDelta - bottomDelta) <= 1);
    }

    private AttributeSet getAttributeSet() throws XmlPullParserException, IOException {
        XmlPullParser parser = mActivity.getResources().getLayout(R.layout.framelayout_layout);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        return Xml.asAttributeSet(parser);
    }

    private static class MyFrameLayout extends FoldableFrameLayout {
        public MyFrameLayout(Context context) {
            super(context);
        }

        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return super.checkLayoutParams(p);
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
        }

        @Override
        protected FoldableFrameLayout.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who);
        }

        @Override
        protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return super.generateLayoutParams(p);
        }
    }
}
