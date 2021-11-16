# Surface Duo SDK

This repo contains the source code for the Microsoft Surface Duo SDK.

Please read the [code of conduct](CODE_OF_CONDUCT.md) and [contribution guidelines](CONTRIBUTING.md).

## Getting Started

When importing the code into Android Studio, use the **surface-duo-sdk** folder as the base directory of the project. This lets you access the sdk source code and run the sdk samples from within the same project.

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

2. Add dependencies to the module-level **build.gradle** file (current version may be different from what's shown here). There are multiple libraries available - for some of them you should reference **ScreenManager** plus any additional libraries you plan to use:

    **ScreenManager**

    If you want to choose the version that uses Display Mask API, then you should add the following lines to your gradle file.

    ```gradle
    implementation "com.microsoft.device.dualscreen:screenmanager-displaymask:1.0.0-beta4"
    ```

    or if you want to choose the version that uses Window Manager API, then you should add the following lines.

    ```gradle
    implementation "com.microsoft.device.dualscreen:screenmanager-windowmanager:1.0.0-beta4"
    ```

    **Bottom navigation**

    ```gradle
    implementation "com.microsoft.device.dualscreen:bottomnavigation:1.0.0-beta3"
    ```

    **Fragments handler**

    ```gradle
    implementation "com.microsoft.device.dualscreen:fragmentshandler:1.0.0-beta3"
    implementation "com.microsoft.device.dualscreen:screenmanager-windowmanager:1.0.0-beta4"
    // Or, if you want to use the screen manager with display mask version
    // implementation "com.microsoft.device.dualscreen:screenmanager-displaymask:1.0.0-beta4"
    ```

    **Layouts**

    ```gradle
    implementation "com.microsoft.device.dualscreen:layouts:1.0.0-beta6"
    ```

    **Tabs**

    ```gradle
    implementation "com.microsoft.device.dualscreen:tabs:1.0.0-beta2"
    implementation "com.microsoft.device.dualscreen:screenmanager-windowmanager:1.0.0-beta4"
    // Or, if you want to use the screen manager with display mask version
    // implementation "com.microsoft.device.dualscreen:screenmanager-displaymask:1.0.0-beta4"
    ```

    **RecyclerView**

    ```gradle
    implementation "com.microsoft.device.dualscreen:recyclerview:1.0.0-beta4"
    ```

    **Ink**

    ```gradle
    implementation "com.microsoft.device:ink:1.0.0-alpha4"
    ```

### Useful links

- Documentation for these libraries at [dual-screen library](https://docs.microsoft.com/dual-screen/android/api-reference/dualscreen-library/)
- Surface Duo developer documentation at [docs.microsoft.com/dual-screen](https://docs.microsoft.com/dual-screen).
- [Surface Duo developer blog](https://devblogs.microsoft.com/surface-duo)

### Samples that utilize this SDK

- [Kotlin samples](https://github.com/microsoft/surface-duo-sdk-samples-kotlin)
- [Java samples](https://github.com/microsoft/surface-duo-sdk-samples)

### Other Surface Duo developer samples

- [Android app samples](https://github.com/microsoft/surface-duo-app-samples)
- [Xamarin samples](https://github.com/microsoft/surface-duo-sdk-xamarin-samples)
- [Unity samples](https://github.com/microsoft/surface-duo-sdk-unity-samples)
- [React Native samples](https://github.com/microsoft/react-native-dualscreen)
- [Flutter samples](https://github.com/microsoft/surface-duo-sdk-samples-flutter)
