package com.microsoft.device.dualscreen.testing.filters

import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import com.microsoft.device.dualscreen.testing.rules.DualScreenTestRule
import com.microsoft.device.dualscreen.testing.runner.FoldableJUnit4ClassRunner

/**
 * A convenience way to mock the [FoldingFeature] with default values provided for testing purpose.
 * With the default values the WindowManager will be mocked with a [FoldingFeature.State.HALF_OPENED] feature
 * that splits the screen along the [FoldingFeature.Orientation.HORIZONTAL] axis.
 * The bounds of the feature are calculated based on orientation and size.
 *
 * If the feature is VERTICAL then the feature is centered horizontally.
 * The top-left x-coordinate is center - (size / 2) and the top-right x-coordinate is center + (size / 2).
 *
 * If the feature is HORIZONTAL then the feature is centered vertically.
 * The top-left y-coordinate is center - (size / 2) and the bottom-left y-coordinate is center - (size / 2).
 *
 * The folding features always cover the window in one dimension and that determines the other coordinates.
 *
 * Must be used together with [DualScreenTestRule] and [FoldableJUnit4ClassRunner]
 *
 * @param windowBounds array of coordinates that represents some display area that will contain the [FoldingFeature]. ex: [left, top, right, bottom]
 * @param center the center of the fold complementary to the orientation.
 * For a HORIZONTAL fold, this is the y-axis and for a VERTICAL fold this is the x-axis.
 * @param size the smaller dimension of the fold. The larger dimension always covers the entire window.
 * @param state State of the fold. The default value is [FoldingFeatureState.HALF_OPENED]
 * @param orientation Orientation of the fold. The default value is [FoldingFeatureOrientation.HORIZONTAL]
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MockFoldingFeature(
    val windowBounds: IntArray,
    val center: Int = -1,
    val size: Int = 0,
    val state: FoldingFeatureState = FoldingFeatureState.HALF_OPENED,
    val orientation: FoldingFeatureOrientation = FoldingFeatureOrientation.HORIZONTAL
) {
    /**
     * Represents the State of the [FoldingFeature].
     */
    enum class FoldingFeatureState {
        /**
         * The foldable device is completely open, the screen space that is presented to the user is flat.
         * See the [Posture](https://developer.android.com/guide/topics/large-screens/learn-about-foldables#postures)
         * section in the official documentation for visual samples and references.
         */
        FLAT,

        /**
         * The foldable device's hinge is in an intermediate position between opened and closed state,
         * there is a non-flat angle between parts of the flexible screen or between physical screen panels.
         * See the [Posture](https://developer.android.com/guide/topics/large-screens/learn-about-foldables#postures)
         * section in the official documentation for visual samples and references.
         */
        HALF_OPENED
    }

    /**
     * Represents the axis for which the [FoldingFeature] runs parallel to.
     */
    enum class FoldingFeatureOrientation {
        /**
         * The width of the [FoldingFeature] is greater than the height.
         */
        HORIZONTAL,

        /**
         * The height of the [FoldingFeature] is greater than or equal to the width.
         */
        VERTICAL
    }
}

/**
 * Translates the coordinates from [IntArray] to [Rect]
 */
internal val MockFoldingFeature.windowBoundsRect: Rect
    get() = Rect(
        windowBounds[0],
        windowBounds[1],
        windowBounds[2],
        windowBounds[3]
    )

/**
 * Translates from [MockFoldingFeature.FoldingFeatureState] to [FoldingFeature.State]
 */
internal val MockFoldingFeature.foldingFeatureState: FoldingFeature.State
    get() = when (state) {
        MockFoldingFeature.FoldingFeatureState.FLAT -> FoldingFeature.State.FLAT
        else -> FoldingFeature.State.HALF_OPENED
    }

/**
 * Translates from [MockFoldingFeature.FoldingFeatureOrientation] to [FoldingFeature.Orientation]
 */
internal val MockFoldingFeature.foldingFeatureOrientation: FoldingFeature.Orientation
    get() = when (orientation) {
        MockFoldingFeature.FoldingFeatureOrientation.HORIZONTAL -> FoldingFeature.Orientation.HORIZONTAL
        else -> FoldingFeature.Orientation.VERTICAL
    }
