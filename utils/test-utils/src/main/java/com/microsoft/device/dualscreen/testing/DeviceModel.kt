/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing

import android.util.Log
import android.view.Surface
import androidx.test.uiautomator.UiDevice

/**
 * DEVICE MODEL
 * -----------------------------------------------------------------------------------------------
 * The DeviceModel class and related helper functions can be used in dualscreen UI tests to help
 * calculate coordinates for simulated swipe gestures. Device properties are determined using
 * UiDevice.
 */

/**
 * Enum class that can be used to represent various device models and extract coordinates that can be used for
 * simulating gestures in UI tests
 *
 * For Microsoft Surface Duo devices, the coordinates are all from the dual portrait point of view, and dimensions
 * were taken from here: https://docs.microsoft.com/dual-screen/android/surface-duo-dimensions
 *
 * @param paneWidth_: width of device panes in pixels in dual portrait mode (assumed to have panes of equal size)
 * @param paneHeight_: height of device panes in pixels in dual portrait mode (assumed to have panes of equal size)
 * @param foldWidth: width of device foldingFeature in pixels in dual portrait mode
 * @param leftX: x-coordinate of the center of the left pane in dual portrait mode
 * @param rightX: x-coordinate of the center of the right pane in dual portrait mode
 * @param middleX: x-coordinate of the center of the device in dual portrait mode
 * @param middleY: y-coordinate of the center of the device in dual portrait mode
 * @param bottomY: y-coordinate of the bottom of the device in dual portrait mode
 * @param spanSteps: number of move steps to take when executing a span gesture, where one step takes ~ 5ms
 * @param unspanSteps: number of move steps to take when executing a unspan gesture, where one step takes ~ 5ms
 * @param switchSteps: number of move steps to take when executing a switch gesture, where one step takes ~ 5ms
 * @param closeSteps: number of move steps to take when executing a close gesture, where one step takes ~ 5ms
 */
enum class DeviceModel(
    internal var paneWidth_: Int,
    internal var paneHeight_: Int,
    val foldWidth: Int,
    val leftX: Int = paneWidth_ / 2,
    val rightX: Int = leftX + paneWidth_ + foldWidth,
    val middleX: Int = paneWidth_ + foldWidth / 2,
    val middleY: Int = paneHeight_ / 2,
    val bottomY: Int,
    val spanSteps: Int = 400,
    val unspanSteps: Int = 200,
    val switchSteps: Int = 100,
    val closeSteps: Int = 50,
    internal var totalDisplay_: Int = paneWidth_ * 2 + foldWidth
) {
    /**
     * Representation for SurfaceDuo1 device and emulator
     */
    SurfaceDuo(paneWidth_ = 1350, paneHeight_ = 1800, foldWidth = 84, bottomY = 1780),

    /**
     * Representation for SurfaceDuo2 device and emulator
     */
    SurfaceDuo2(paneWidth_ = 1344, paneHeight_ = 1892, foldWidth = 66, bottomY = 1870),

    /**
     * Representation for 6.7" horizontal Fold-In devices and emulators
     */
    HorizontalFoldIn(paneWidth_ = 1080, paneHeight_ = 1318, foldWidth = 0, bottomY = 0),

    /**
     * Representation for 7.6" Fold-In with outer display devices and emulators
     */
    FoldInOuterDisplay(paneWidth_ = 884, paneHeight_ = 2208, foldWidth = 0, bottomY = 0),

    /**
     * Representation for 8" FoldOut devices and emulators
     */
    FoldOut(paneWidth_ = 1100, paneHeight_ = 2480, foldWidth = 0, bottomY = 0),

    /**
     * Representation for Other foldable devices and emulators.
     */
    Other(paneWidth_ = 0, paneHeight_ = 0, foldWidth = 0, bottomY = 0);

    val paneWidth: Int
        get() = paneWidth_

    val paneHeight: Int
        get() = paneHeight_

    val totalDisplay: Int
        get() = totalDisplay_

    override fun toString(): String {
        return "$name [leftX: $leftX rightX: $rightX middleX: $middleX middleY: $middleY bottomY: $bottomY]"
    }
}

/**
 * Checks whether a device is a Surface Duo model
 */
fun UiDevice.isSurfaceDuo(): Boolean {
    val model = getDeviceModel()
    return model == DeviceModel.SurfaceDuo || model == DeviceModel.SurfaceDuo2
}

/**
 * Returns a pixel value of the hinge/fold size of a foldable or dual-screen device
 */
fun UiDevice.getFoldSize(): Int {
    return getDeviceModel().foldWidth
}

/**
 * Returns the model of a device based on display width and height (in pixels)
 */
fun UiDevice.getDeviceModel(): DeviceModel {
    Log.d(
        "DeviceModel",
        "w: $displayWidth h: $displayHeight rotation: $displayRotation"
    )

    return when (displayRotation) {
        Surface.ROTATION_0, Surface.ROTATION_180 -> getModelFromPaneWidth(displayWidth)
        Surface.ROTATION_90, Surface.ROTATION_270 -> getModelFromPaneWidth(displayHeight)
        else -> throw Error("Unknown rotation state $displayRotation")
    }
}

/**
 * Helper method to compare the pane width of a device to the pane widths of the defined device
 * models
 *
 * @param paneWidth: pane width in pixels
 */
private fun UiDevice.getModelFromPaneWidth(paneWidth: Int): DeviceModel {
    for (model in DeviceModel.values()) {
        // pane width could be the width of a single pane, or the width of two panes + the width
        // of the hinge
        if (paneWidth == model.paneWidth || paneWidth == model.paneWidth * 2 + model.foldWidth)
            return model
    }
    Log.d(
        "DeviceModel",
        "Unknown dualscreen device dimensions $displayWidth $displayHeight"
    )
    return DeviceModel.Other.apply {
        this.paneWidth_ = displayWidth / 2
        this.paneHeight_ = displayHeight
        this.totalDisplay_ = paneWidth_ * 2 + foldWidth
    }
}
