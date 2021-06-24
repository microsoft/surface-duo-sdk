package com.microsoft.device.dualscreen.test.utils

import androidx.test.espresso.ViewAction

/**
 * Returns an action that clicks the view without to check the coordinates on the screen.
 * Seems that ViewActions.click() finds coordinates of the view on the screen, and then performs the tap on the coordinates.
 * Seems that changing the screen rotations affects these coordinates and ViewActions.click() throws exceptions.
 */
fun forceClick(): ViewAction = ForceClick()