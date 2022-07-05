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
    androidTestImplementation "com.microsoft.device.dualscreen.testing:testing-kotlin:1.0.0-alpha4"
    ```

3. Optional. If you need to use other testing dependencies such as [Espresso](https://developer.android.com/training/testing/espresso) or [UiAutomator](https://developer.android.com/training/testing/other-components/ui-automator), you will have to add them to your **build.gradle** file as well. This library uses these dependencies but are not exposed.

# API reference

## UiDevice Extensions

These functions can be used in dual-screen UI tests to simulate swipe gestures that affect app display. The swipes are simulated using UiDevice, and the coordinates are calculated based on the display width/height of the testing device. They can be used only on dual-screen devices, not foldable devices or large screen devices.

**API functions:**

```kotlin
fun UiDevice.spanFromStart()
```

Span app from the top/left pane

```kotlin
fun UiDevice.spanFromEnd()
```

Span app from the bottom/right pane

```kotlin
fun UiDevice.unspanToStart()
```

Unspan app to the top/left pane

```kotlin
fun UiDevice.unspanToEnd()
```

Unspan app to bottom/right pane

```kotlin
fun UiDevice.switchToStart()
```

Switch app from bottom/right pane to top/left pane

```kotlin
fun UiDevice.switchToEnd() 
```

Switch app from top/left pane to bottom/right pane

```kotlin
fun UiDevice.closeStart() 
```

Close app from top/left pane

```kotlin
fun UiDevice.closeEnd()
```

Close app from bottom/right pane

```kotlin
fun UiDevice.resetOrientation()
```

Simulates orienting the device into its natural orientation, re-enables the sensors and un-freezes the device rotation.

## FoldingFeatureHelper

Contains several utility functions that helps to create mocked [FoldingFeatures](https://developer.android.com/reference/androidx/window/layout/FoldingFeature). Tests can run on any single screen device/emulator and simulate a specific FoldingFeature to make easier to test foldable UIs without the need to use foldable devices/emulators.

**API functions:**

```kotlin
fun createWindowLayoutInfoPublisherRule(): TestRule {
    return WindowLayoutInfoPublisherRule()
}
```
Returns WindowLayoutInfoPublisherRule which allows you to push through different WindowLayoutInfo values on demand from Window.testing to test.

```kotlin
fun <A : ComponentActivity> TestRule.simulateVerticalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Creates a vertical folding feature with the defined size, center position and state.
By default, not passing parameters and using the default ones, the folding feature will be centered, have 0px width and the position of the device will be HALF_OPENED (book mode).

```kotlin
fun <A : ComponentActivity> TestRule.simulateHorizontalFoldingFeature(
    activityRule: ActivityScenarioRule<A>,
    center: Int = -1,
    size: Int = 0,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Creates an horizontal folding feature with the defined size, center position and state. By default, not passing parameters and using the default ones, the folding feature will be centered, have 0px width and the position of the device will be HALF_OPENED (table-top/desktop mode).

```kotlin
fun <A : ComponentActivity> TestRule.simulateSurfaceDuo1(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT,
    orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL)
```
Simulates Surface Duo 1 folding feature (84px FoldingFeature width). By default, the position of the of the device will be FLAT (open in 180 degrees) and its orientation is VERTICAL.

```kotlin
fun <A : ComponentActivity> TestRule.simulateSurfaceDuo2(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.FLAT,
    orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL) 
```
Simulates Surface Duo 2 folding feature (66px FoldingFeature width). By default, the position of the of the device will be FLAT (open in 180 degrees) and its orientation is VERTICAL.

```kotlin
fun <A : ComponentActivity> TestRule.simulateFoldDevice(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Simulates Fold device with a vertical folding feature (0px FoldingFeature width). By default, the position of the of the device will be HALF_OPENED (book mode).

```kotlin
fun <A : ComponentActivity> TestRule.simulateFlipDevice(
    activityRule: ActivityScenarioRule<A>,
    state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED)
```
Simulates Flip device with an horizontal folding feature (0px FoldingFeature width). By default, the position of the of the device will be HALF_OPENED (table-top/desktop mode).

## DeviceModel

The DeviceModel class and related helper functions can be used in dual-screen UI tests to help calculate coordinates for simulated swipe gestures. Device properties are determined using UiDevice.

**API functions:**

```kotlin
enum class DeviceModel
```
Enum class that can be used to represent various device models and extract coordinates that can be used for simulating gestures in UI tests.
It can take the following values:
- SurfaceDuo
- SurfaceDuo2
- HorizontalFoldIn
- FoldInOuterDisplay
- FoldOut
- Other

For Microsoft Surface Duo devices, the coordinates are all from the dual portrait point of view, and dimensions were taken from [here](https://docs.microsoft.com/dual-screen/android/surface-duo-dimensions).
 
```kotlin
fun UiDevice.isSurfaceDuo(): Boolean
```
Checks whether a device is a Surface Duo model.

```kotlin
fun UiDevice.getFoldSize(): Int
```
Returns a pixel value of the hinge/fold size of a foldable or dual-screen device.

```kotlin
fun UiDevice.getDeviceModel(): DeviceModel
```
Returns the model of a device based on display width and height (in pixels).

## Annotations

**Important:** Please see [FoldableTestRule](#foldabletestrule) and [FoldableJUnit4ClassRunner](#foldablejunit4classrunner) sections before start using the following annotations.

### SingleScreenTest/DualScreenTest

Add this annotation for the test method or test class if you want to run the test on single-screen/dual-screen mode.
Also, using the `orientation` parameter, you can run the test on the specified device orientation.

The `orientation` parameter can have the following values:
- `UiAutomation.ROTATION_FREEZE_0`.
- `UiAutomation.ROTATION_FREEZE_90`.
- `UiAutomation.ROTATION_FREEZE_180`.
- `UiAutomation.ROTATION_FREEZE_270`.

Here you can see how you can use the `@SingleScreenTest` annotation as a test-class level:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
@SingleScreenTest(orientation = UiAutomation.ROTATION_FREEZE_180)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    @Test
    fun sampleTestMethod() {
    }
}
```

Here you can see how you can use the `@DualScreenTest` at a test-function level:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    
    @Test
    @DualScreenTest(orientation = UiAutomation.ROTATION_FREEZE_90)
    fun sampleTestMethod() {
    }
}
```

### MockFoldingFeature

Use this annotation for the test method or test class if you want to mock the folding feature with the desired data.

- `windowBounds` parameter is an array of coordinates that represents some display area that will contain the [FoldingFeature](https://developer.android.com/reference/androidx/window/layout/FoldingFeature), 
for example [left, top, right, bottom]. `windowBounds` **width and height must be greater than** `0`.

    **By default**, if you don't define your own `windowBounds` values, **it will use the whole display area**, more specifically `[0, 0, uiDevice.displayWidth, uiDevice.displayHeight]`.

- The `center` parameter is the center of the fold complementary to the orientation.
For a `HORIZONTAL` fold, this is the y-axis and for a `VERTICAL` fold this is the x-axis. **Default value** is `-1`.

    If this parameter is not provided, then will be calculated based on `windowBounds` parameter as windowBounds.centerY() or windowBounds.centerX(), depending on the given `orientation`.

- The `size` parameter is the smaller dimension of the fold. The larger dimension always covers the entire window. **Default value** is `0`.

- Parameter `state` represents the state of the fold.
Allowed values are:
  - `FoldingFeatureState.HALF_OPENED`.
  - `FoldingFeatureState.FLAT`.
  
  The **default value** is `FoldingFeatureState.HALF_OPENED`.
  
  See the [Posture](https://developer.android.com/guide/topics/large-screens/learn-about-foldables#postures) section in the official documentation for visual samples and references.

- Parameter `orientation` is the orientation of the fold.
Allowed values are:
  - `FoldingFeatureOrientation.HORIZONTAL`.
  - `FoldingFeatureOrientation.VERTICAL`.
  
  The **default value** is `FoldingFeatureOrientation.HORIZONTAL`.

Here is an example of how we can use the `@MockingFoldingFeature` as a test-class level setting also a specific `windowBounds`:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
@MockFoldingFeature(
    windowBounds = [0, 0, 1768, 2208], 
    center = 0, 
    size = 2, 
    state = FoldingFeatureState.FLAT, 
    orientation = FoldingFeatureOrientation.HORIZONTAL
)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    @Test
    fun sampleTestMethod() {
    }
}
```

Below you can see how we can use the `@MockingFoldingFeature` at a test-function level without defining a specific `windowBounds`:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    
    @Test
    @MockFoldingFeature(
        center = 0, 
        size = 2, 
        orientation = FoldingFeatureOrientation.HORIZONTAL
    )
    fun sampleTestMethod() {
    }
}
```

### TargetDevices

Use this annotations for the test method or test class if you want to run the test only on the specified devices or 
if you want to ignore some devices.

- The `devices` parameter is an array of wanted devices and the `ignoreDevices` is an array with the devices to be ignored.
  
  Example of usage: 
  - `@TargetDevices`(devices = [DeviceModel.SurfaceDuo, DeviceModel.SurfaceDuo2]).
  - `@TargetDevices`(ignoreDevices = [DeviceModel.SurfaceDuo]).
  
  **You cannot use both parameters at the same time.**

Allowed values for device model:

- `DeviceModel.SurfaceDuo` - representation for `SurfaceDuo1` device or emulator

- `DeviceModel.SurfaceDuo2` - representation for `SurfaceDuo2` device and emulator

- `DeviceModel.HorizontalFoldIn` - representation for `6.7" horizontal Fold-In` devices and emulators

- `DeviceModel.FoldInOuterDisplay` - representation for `7.6" Fold-In` with outer display devices and emulators

- `DeviceModel.FoldOut` - representation for `8" FoldOut` devices and emulators

- `DeviceModel.Other` - representation for other foldable devices and emulators

Below you can see how you can use the `@TargetDevices` annotation at a test-class level setting up target `devices`:
```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
@TargetDevices(devices = [DeviceModel.SurfaceDuo, DeviceModel.SurfaceDuo2])
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    @Test
    fun sampleTestMethod() {
    }
}
```

Below you can see how you can use the `@TargetDevices` annotation at a test-function level setting up `ignoreDevices`:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    
    @Test
    @TargetDevices(ignoreDevices = [DeviceModel.SurfaceDuo])
    fun sampleTestMethod() {
    }
}
```

### DeviceOrientation

Use this annotation for the test method or test class if you want to run the test on the specified device orientation.

- The `orientation` parameter represents the device orientation and can have the following values:
  - `UiAutomation.ROTATION_FREEZE_0`.
  - `UiAutomation.ROTATION_FREEZE_90`.
  - `UiAutomation.ROTATION_FREEZE_180`.
  - `UiAutomation.ROTATION_FREEZE_270`.

Below you can see how you can use `@DeviceOrientation` annotation at a test-class level:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
@DeviceOrientation(orientation = UiAutomation.ROTATION_FREEZE_180)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    @Test
    fun sampleTestMethod() {
    }
}
```

Below you can see how you can use `@DeviceOrientation` annotation at a test-function level:

```kotlin
@RunWith(FoldableJUnit4ClassRunner::class)
class TestSample {
    private val activityScenarioRule = activityScenarioRule<MainActivity>()
    private val foldableTestRule = FoldableTestRule()
    @get:Rule
    val testRule: TestRule = foldableRuleChain(activityScenarioRule, foldableTestRule)
    
    @Test
    @TargetDevices(orientation = UiAutomation.ROTATION_FREEZE_90)
    fun sampleTestMethod() {
    }
}
```

## FoldableTestRule

`FoldableTestRule` is a custom `TestRule` that must be used together with `@SingleScreenTest`, `@DualScreenTest`, `@MockFoldingFeature` and `@DeviceOrientation` annotations.

**Without using this test rule, the annotations will not work.**
This `TestRule` is using reflection to retrieve these annotations and parameters in order to run the tests on the wanted posture and device orientation.

## FoldableJUnit4ClassRunner

`FoldableJUnit4ClassRunner` is a custom `AndroidJUnit4ClassRunner` that must be used together with `@SingleScreenTest`, `@DualScreenTest`, `@MockFoldingFeature`, `@DeviceOrientation` and `@TargetDevices` annotations.

This runner will validate the parameters for the annotations or will ignore tests when the current device is not on the wanted devices or in the ignored devices list.  
**Without using this** `Runner`, **the annotations will not be validated and the** `@TargetDevices` **annotation will not have any effect.**

## WindowLayoutInfoConsumer

 Provides a [Consumer](https://developer.android.com/reference/java/util/function/Consumer)<[WindowLayoutInfo](https://developer.android.com/reference/kotlin/androidx/window/layout/WindowLayoutInfo)> that an Activity can use to reset previous WindowLayoutInfo data and to wait for new WindowLayoutInfo changes.

 **API functions:**

```kotlin
fun register(activity: AppCompatActivity)
```
Register a listener to consume WindowLayoutInfo values.

```kotlin
override fun accept(windowLayoutInfo: WindowLayoutInfo)
```
Accepts a WindowLayoutInfo with the new information that will handle.

```kotlin
fun resetWindowInfoLayout()
```
Resets the last window layout info to `null`.

```kotlin
fun resetWindowInfoLayoutCounter()
```
Resets WindowLayoutInfo counter when waiting for window layout changes to happen, before calling *waitForWindowInfoLayoutChanges*.

```kotlin
fun waitForWindowInfoLayoutChanges(): Boolean
```
Blocks and waits for the next window layout info changes to happen.
Returns `true` if the window layout info changed before the timeout count reached zero, and `false` if the waiting time elapsed before the changes happened.

```kotlin
fun reset()
```
Resets current used instance to its original state.

## CurrentActivityDelegate

[Activity](https://developer.android.com/reference/android/app/Activity) delegate used to setup and support a test scenario.
It provides useful functionality used whenever you want to ensure that the Activity is running before use any view action or assertion on its UI components.

**API functions:**

```kotlin
fun <T : Activity> setup(activityScenarioRule: ActivityScenarioRule<T>)
```
Setup the delegate using a given ActivityScenarioRule.

```kotlin
fun <T : Activity> clear(activityScenarioRule: ActivityScenarioRule<T>)
```
Removes a given ActivityScenarioRule from the delegate.

```kotlin
fun resetActivity()
```
Resets the last started Activity to *null*.

```kotlin
fun resetActivityCounter()
```
Resets the activity counter when waiting for the Activity to start before calling it.

```kotlin
fun waitForActivity(): Boolean
```
Blocks and waits for the next activity to be started.
Returns `true` if the activity was started before the timeout count reached zero, or `false` if the waiting time elapsed before the changes happened already.

## ForceClick

```kotlin
class ForceClick : ViewAction
```
This is a [ViewAction](https://developer.android.com/reference/androidx/test/espresso/ViewAction) that clicks on a given [View](https://developer.android.com/reference/android/view/View) without checking its coordinates in the screen when the device has been rotated.

## ViewMatcher

Contains utilities that are used for view assertions in UI tests.

**API functions:**

```kotlin
fun isViewOnScreen(
    position: DisplayPosition,
    orientation: Int,
    firstDisplay: Int,
    totalDisplay: Int,
    foldingFeature: Int = 0
): Matcher<View>
```
Returns a [Matcher](http://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/Matcher.html) that checks if a View is shown in the display area given its position and screen dimensions.

```kotlin
fun areCoordinatesOnTargetScreen(
    targetScreenPosition: DisplayPosition,
    start: Int,
    end: Int,
    firstDisplay: Int,
    totalDisplay: Int,
    foldingFeature: Int = 0
): Boolean
```
Checks wether a specific screen-position is the screen defined by the function parameters.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the Microsoft Open Source Code of Conduct. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

## License

Copyright (c) Microsoft Corporation.

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.