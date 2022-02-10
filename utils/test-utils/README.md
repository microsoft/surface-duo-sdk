# Testing-kotlin

In this module you can find the different utilities that we have created in order to help us to implement UI tests on our libraries.

These utilities were originally created as a way to reuse code on our different UI tests without the need to copy-paste useful function on each library module.
Since we think that there is value on these utilities to be shared with the developer community we have created a testing component that can be added as a external dependency to any project that want to implement UI tests targeting dual-screen and foldable devices.

## Add to your project

1. Make sure you have **mavenCentral()** repository in your top level **build.gradle** file:

    ```gradle
    allprojects {
        repositories {
            google()
            mavenCentral()
         }
    }
    ```

2. Add dependencies to the module-level **build.gradle** file (current version may be different from what's shown here):

    ```gradle
    implementation "com.microsoft.device.dualscreen.testing:testing-kotlin:1.0.0-alpha1"
    ```

## API reference

The library is divided into a few classes that cover just one specific purpose:

### CurrentActivityDelegate

[Activity](https://developer.android.com/reference/android/app/Activity) delgate used to setup and support a test scenario.
It provides useful functionality used whenever you want to ensure that the Activity is running before use any view action or assertion on its UI components.

API functions:

`fun <T : Activity> setup(activityScenarioRule: ActivityScenarioRule<T>)`

Setup the delegate using a given ActivityScenarioRule/

`fun <T : Activity> clear(activityScenarioRule: ActivityScenarioRule<T>)`

Removes a given ActivityScenarioRule from the delegate

`fun resetActivity()`

Resets the last started Activity to `null`

`fun resetActivityCounter()`

Resets the activity counter when waiting for the Activity to start before calling it

`fun waitForActivity(): Boolean`

Blocks and waits for the next activity to be started.
Returns `true` if the activity was started before the timeout count reached zero, or `false` if the waiting time elapsed before the changes happened already.

### DeviceRotation

Provides utilities based on [UiAutomator](https://developer.android.com/training/testing/other-components/ui-automator) where we can change the configuration of the device for testing purposes.

`fun unfreezeRotation()`

Re-enables the sensors and un-freezes the device rotation allowing its contents to rotate with the device physical rotation. During a test execution, it is best to keep the device frozen in a specific orientation until the test case execution has completed.

`fun setOrientationLeft()`

Simulates changing the orientation of the device to the left and also freezes rotation by disabling the sensors.

`fun setOrientationNatural()`

Simulates orienting the device into its natural orientation and also freezes rotation by disabling the sensors.

`fun setOrientationRight()`

Simulates orienting the device to the right and also freezes rotation by disabling the sensors.

`fun resetOrientation()`

Simulates orienting the device into its natural orientation, re-enables the sensors and un-freezes the device rotation.

### FoldingFeatureUtils

Contains several utility functions that helps to create mocked [FoldingFeatures](https://developer.android.com/reference/androidx/window/layout/FoldingFeature). Tests can run on any single screen device/emulator and simulate a specific FoldingFeature to make easier to test foldable UIs without the need to use foldable devices/emulators.

```
fun <A : ComponentActivity> TestRule.createVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Creates a vertical folding feature with the defined size, center position and state.
By default, not passing parameters and using the default ones, the folding feature will be centered, have 0px width and the position of the device will be HALF_OPENED (book mode).

```
fun <A : ComponentActivity> TestRule.createHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Creates an horizontal folding feature with the defined size, center position and state. By default, not passing parameters and using the default ones, the folding feature will be centered, have 0px width and the position of the device will be HALF_OPENED (table-top/desktop mode).

```
fun <A : ComponentActivity> TestRule.createSurfaceDuo1FoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT)
```
Simulates Surface Duo 1 folding feature (84px FoldingFeature width). By default, the position of the of the device will be FLAT (open in 180 degrees).

```
fun <A : ComponentActivity> TestRule.createSurfaceDuo2FoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT)
```
Simulates Surface Duo 2 folding feature (66px FoldingFeature width). By default, the position of the of the device will be FLAT (open in 180 degrees).

```
fun <A : ComponentActivity> TestRule.createFoldWithVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Simulates well known available foldable device's vertical folding feature (0px FoldingFeature width). By default, the position of the of the device will be HALF_OPENED (book mode).

```
fun <A : ComponentActivity> TestRule.createFoldWithHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Simulates well known available foldable device's horizontal folding feature (0px FoldingFeature width). By default, the position of the of the device will be HALF_OPENED (table-top/desktop mode).

### ForceClick

`class ForceClick : ViewAction`

This is a [ViewAction](https://developer.android.com/reference/androidx/test/espresso/ViewAction) that clicks on a given [View](https://developer.android.com/reference/android/view/View) without checking its coordinates in the screen when the device has been rotated.

### SurfaceDuoUtils

Contains useful specs information and utilities for tests that target **Surface Duo** devices.

`object SurfaceDuo1`

Inside this object you can find useful information about **Surface Duo 1** specs and utilities to span and unspan apps on this device and emulator.

`fun switchFromSingleToDualScreen()`

Span an app across displays. The function will handle correctly the different rotation modes of the device.
This function works on **Surface Duo 2**.

`fun switchFromDualToSingleScreen()`

Unspan an app from being spanned across the whole display area to a single screen. The function will handle correctly the different rotation modes of the device.
This function works on **Surface Duo 1**.

`object SurfaceDuo2`

Inside this object you can find useful information about **Surface Duo 2** specs and utilities to span and unspan apps on this device and emulator.

`fun switchFromSingleToDualScreen()`

Span an app across displays. The function will handle correctly the different rotation modes of the device.
This function works on **Surface Duo 2**.

`fun switchFromDualToSingleScreen()`

Unspan an app from being spanned across the whole display area to a single screen. The function will handle correctly the different rotation modes of the device.
This function works on **Surface Duo 2**.

### ViewMatcher

Contains utilities that are used for view assertions in UI tests.

```
fun isViewOnScreen(
    position: DisplayPosition,
    orientation: Int,
    firstDisplayWidth: Int,
    totalDisplayWidth: Int,
    foldingFeatureWidth: Int = 0
): Matcher<View>
```

Returns a Matcher that checks if a View is shown in the display area given its position and screen dimensions.

```
fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    xStart: Int,
    xEnd: Int,
    firstDisplayWidth: Int,
    totalDisplayWidth: Int,
    foldingFeatureWidth: Int = 0
): Boolean
```

Checks wether a specific screen-position is the screen defined by the function parameters.

### Useful links

- Documentation for these libraries at [dual-screen library](https://docs.microsoft.com/dual-screen/android/api-reference/dualscreen-library/)
- Surface Duo developer documentation at [docs.microsoft.com/dual-screen](https://docs.microsoft.com/dual-screen).
- [Surface Duo developer blog](https://devblogs.microsoft.com/surface-duo)

### WindowLayoutInfoConsumer

 Provides a [Consumer](https://developer.android.com/reference/java/util/function/Consumer)<[WindowLayoutInfo](https://developer.android.com/reference/kotlin/androidx/window/layout/WindowLayoutInfo)> that an Activity can use to reset previous WindowLayoutInfo data and to wait for new WindowLayoutInfo changes.

`fun register(activity: AppCompatActivity)`

Register a listener to consume WindowLayoutInfo values.

`override fun accept(windowLayoutInfo: WindowLayoutInfo)`

Accepts a WindowLayoutInfo with the new information that will handle.

`fun resetWindowInfoLayout()`

Resets the last window layout info to `null`.

`fun resetWindowInfoLayoutCounter()`

Resets WindowLayoutInfo counter when waiting for window layout changes to happen, before calling *waitForWindowInfoLayoutChanges*.

`fun waitForWindowInfoLayoutChanges(): Boolean`

Blocks and waits for the next window layout info changes to happen.
Returns `true` if the window layout info changed before the timeout count reached zero, and `false` if the waiting time elapsed before the changes happened.

`fun reset()`

Resets current used instance to its original state.

## Contributing

Please read the [code of conduct](CODE_OF_CONDUCT.md) and [contribution guidelines](CONTRIBUTING.md).

## Code of Conduct

This project has adopted the Microsoft Open Source Code of Conduct. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

## License

Copyright (c) Microsoft Corporation.

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.