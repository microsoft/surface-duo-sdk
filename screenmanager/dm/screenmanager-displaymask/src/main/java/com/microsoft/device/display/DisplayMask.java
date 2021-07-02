package com.microsoft.device.display;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import java.util.List;

/**
 * Represents the area of the display that is not functional for displaying content.
 * This is only a stub class and the actual implementation is provided by the platform.
 *
 * <p>{@code DisplayMask} is immutable.
 */
public final class DisplayMask {
    /**
     * Creates a DisplayMask instance.
     *
     * @param safeInsets    the insets from each edge which avoid the display mask as returned by
     *                      {@link #getSafeInsetTop()} etc.
     * @param boundingRects the bounding rects of the display masks as returned by
     *                      {@link #getBoundingRects()} ()}.
     */
    DisplayMask(Rect safeInsets, List<Rect> boundingRects) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the bounding region of the mask.
     *
     * <p>
     * <strong>Note:</strong> There may be more than one mask, in which case the returned
     * {@code Region} will be non-contiguous and its bounding rect will be meaningless without
     * intersecting it first.
     * <p>
     * Example:
     * <pre>
     *     // Getting the bounding rectangle of the top display mask
     *     Region bounds = displayMask.getBounds();
     *     bounds.op(0, 0, Integer.MAX_VALUE, displayMask.getSafeInsetTop(), Region.Op.INTERSECT);
     *     Rect topDisplayMask = bounds.getBoundingRect();
     * </pre>
     *
     * @return the bounding region of the mask. Coordinates are relative
     * to the top-left corner of the content view and in pixel units.     *
     */
    public Region getBounds() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns a list of {@code Rect}s, each of which is the bounding rectangle for a non-functional
     * area on the display.
     *
     * @return a list of bounding {@code Rect}s, one for each display mask area.
     */
    public List<Rect> getBoundingRects() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns a list of {@code Rect}s with respect to the rotation, each of which is the bounding rectangle for a non-functional
     * area on the display.
     *
     * @param rotation possible values are: Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270
     * @return a list of bounding {@code Rect}s, one for each display mask area.
     */
    public List<Rect> getBoundingRectsForRotation(int rotation) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Creates the display mask according ton config_mainBuiltInDisplayMaskRectApproximation, which is the closest
     * rectangle-base approximation of the mask.
     */
    public static DisplayMask fromResourcesRectApproximation(Context context) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Creates the display mask according to config_mainBuiltInDisplayMaskRect.
     */
    public static DisplayMask fromResourcesRect(Context context) {
        throw new RuntimeException("Stub!");
    }
}
