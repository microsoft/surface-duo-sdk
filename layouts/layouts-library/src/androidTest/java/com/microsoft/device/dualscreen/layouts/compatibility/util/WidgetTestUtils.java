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

package com.microsoft.device.dualscreen.layouts.compatibility.util;

import static android.view.ViewTreeObserver.OnDrawListener;
import static android.view.ViewTreeObserver.OnGlobalLayoutListener;

import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.rule.ActivityTestRule;

import junit.framework.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The useful methods for widget test.
 */
public class WidgetTestUtils {
    /**
     * Assert that two bitmaps have identical content (same dimensions, same configuration,
     * same pixel content).
     *
     * @param b1 the first bitmap which needs to compare.
     * @param b2 the second bitmap which needs to compare.
     */
    public static void assertEquals(Bitmap b1, Bitmap b2) {
        if (b1 == b2) {
            return;
        }

        if (b1 == null || b2 == null) {
            Assert.fail("the bitmaps are not equal");
        }

        // b1 and b2 are all not null.
        if (b1.getWidth() != b2.getWidth() || b1.getHeight() != b2.getHeight()
            || b1.getConfig() != b2.getConfig()) {
            Assert.fail("the bitmaps are not equal");
        }

        int w = b1.getWidth();
        int h = b1.getHeight();
        int s = w * h;
        int[] pixels1 = new int[s];
        int[] pixels2 = new int[s];

        b1.getPixels(pixels1, 0, w, 0, 0, w, h);
        b2.getPixels(pixels2, 0, w, 0, 0, w, h);

        for (int i = 0; i < s; i++) {
            if (pixels1[i] != pixels2[i]) {
                Assert.fail("the bitmaps are not equal");
            }
        }
    }

    /**
     * Find beginning of the special element.
     * @param parser XmlPullParser will be parsed.
     * @param firstElementName the target element name.
     *
     * @throws XmlPullParserException if XML Pull Parser related faults occur.
     * @throws IOException if I/O-related error occur when parsing.
     */
    public static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        Assert.assertNotNull(parser);
        Assert.assertNotNull(firstElementName);

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            ;
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName()
                    + ", expected " + firstElementName);
        }
    }

    /**
     * Compare the expected pixels with actual, scaling for the target context density
     *
     * @throws AssertionFailedError
     */
    public static void assertScaledPixels(int expected, int actual, Context context) {
        Assert.assertEquals(expected * context.getResources().getDisplayMetrics().density,
                actual, 3);
    }

    /** Converts dips into pixels using the {@link Context}'s density. */
    public static int convertDipToPixels(Context context, int dip) {
      float density = context.getResources().getDisplayMetrics().density;
      return Math.round(density * dip);
    }

    /**
     * Retrieve a bitmap that can be used for comparison on any density
     * @param resources
     * @return the {@link Bitmap} or <code>null</code>
     */
    public static Bitmap getUnscaledBitmap(Resources resources, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * Retrieve a dithered bitmap that can be used for comparison on any density
     * @param resources
     * @param config the preferred config for the returning bitmap
     * @return the {@link Bitmap} or <code>null</code>
     */
    public static Bitmap getUnscaledAndDitheredBitmap(Resources resources,
            int resId, Bitmap.Config config) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = false;
        options.inPreferredConfig = config;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * Argument matcher for equality check of a CharSequence.
     *
     * @param expected expected CharSequence
     *
     * @return
     */
    public static CharSequence sameCharSequence(final CharSequence expected) {
        return argThat(new BaseMatcher<CharSequence>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof CharSequence) {
                    return TextUtils.equals(expected, (CharSequence) o);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("doesn't match " + expected);
            }
        });
    }

    /**
     * Argument matcher for equality check of an Editable.
     *
     * @param expected expected Editable
     *
     * @return
     */
    public static Editable sameEditable(final Editable expected) {
        return argThat(new BaseMatcher<Editable>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof Editable) {
                    return TextUtils.equals(expected, (Editable) o);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("doesn't match " + expected);
            }
        });
    }

    /**
     * Runs the specified {@link Runnable} on the main thread and ensures that the specified
     * {@link View}'s tree is drawn before returning.
     *
     * @param activityTestRule the activity test rule used to run the test
     * @param view the view whose tree should be drawn before returning
     * @param runner the runnable to run on the main thread, or {@code null} to
     *               simply force invalidation and a draw pass
     */
    public static void runOnMainAndDrawSync(@NonNull final ActivityTestRule activityTestRule,
            @NonNull final View view, @Nullable final Runnable runner) {
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            activityTestRule.runOnUiThread(() -> {
                final OnDrawListener listener = new OnDrawListener() {
                    @Override
                    public void onDraw() {
                        // posting so that the sync happens after the draw that's about to happen
                        view.post(() -> {
                            view.getViewTreeObserver().removeOnDrawListener(this);
                            latch.countDown();
                        });
                    }
                };

                view.getViewTreeObserver().addOnDrawListener(listener);

                if (runner != null) {
                    runner.run();
                }
                view.invalidate();
            });

            Assert.assertTrue("Expected draw pass occurred within 5 seconds",
                    latch.await(5, TimeUnit.SECONDS));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Runs the specified Runnable on the main thread and ensures that the activity's view tree is
     * laid out before returning.
     *
     * @param activityTestRule the activity test rule used to run the test
     * @param runner the runnable to run on the main thread. {@code null} is
     * allowed, and simply means that there no runnable is required.
     * @param forceLayout true if there should be an explicit call to requestLayout(),
     * false otherwise
     */
    public static void runOnMainAndLayoutSync(@NonNull final ActivityTestRule activityTestRule,
            @Nullable final Runnable runner, boolean forceLayout)
            throws Throwable {
        runOnMainAndLayoutSync(activityTestRule,
                activityTestRule.getActivity().getWindow().getDecorView(), runner, forceLayout);
    }

    /**
     * Runs the specified Runnable on the main thread and ensures that the specified view is
     * laid out before returning.
     *
     * @param activityTestRule the activity test rule used to run the test
     * @param view The view
     * @param runner the runnable to run on the main thread. {@code null} is
     * allowed, and simply means that there no runnable is required.
     * @param forceLayout true if there should be an explicit call to requestLayout(),
     * false otherwise
     */
    public static void runOnMainAndLayoutSync(@NonNull final ActivityTestRule activityTestRule,
            @NonNull final View view, @Nullable final Runnable runner, boolean forceLayout)
            throws Throwable {
        final View rootView = view.getRootView();

        final CountDownLatch latch = new CountDownLatch(1);

        activityTestRule.runOnUiThread(() -> {
            final OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // countdown immediately since the layout we were waiting on has happened
                    latch.countDown();
                }
            };

            rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

            if (runner != null) {
                runner.run();
            }

            if (forceLayout) {
                rootView.requestLayout();
            }
        });

        try {
            Assert.assertTrue("Expected layout pass within 5 seconds",
                    latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
