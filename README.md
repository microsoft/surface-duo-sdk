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
    implementation "com.microsoft.device.dualscreen:bottomnavigation:1.0.0-beta5"
    ```

    **Fragments handler**

    ```gradle
    implementation "com.microsoft.device.dualscreen:fragmentshandler:1.0.0-beta5"
    ```

    **Layouts**

    ```gradle
    implementation "com.microsoft.device.dualscreen:layouts:1.0.0-beta9"
    ```

    **Tabs**

    ```gradle
    implementation "com.microsoft.device.dualscreen:tabs:1.0.0-beta5"
    ```

    **RecyclerView**

    ```gradle
    implementation "com.microsoft.device.dualscreen:recyclerview:1.0.0-beta6"
    ```

   **Ink**

    ```gradle
    implementation "com.microsoft.device:ink:1.0.0-alpha5"
    ```
   
   **Foldable Navigation Component**
    ```gradle
    def nav_version = "1.0.0-alpha3"

    // Java language implementation
    implementation "com.microsoft.device.dualscreen:navigation-fragment:$nav_version"
    implementation "com.microsoft.device.dualscreen:navigation-ui:$nav_version"

    // Kotlin
    implementation "com.microsoft.device.dualscreen:navigation-fragment-ktx:$nav_version"
    implementation "com.microsoft.device.dualscreen:navigation-ui-ktx:$nav_version"
    ```

    **SnackbarContainer**

    ```gradle
    implementation "com.microsoft.device.dualscreen:snackbar:1.0.0-alpha2"
    ```

   **Testing**

    ```gradle
    implementation "com.microsoft.device.dualscreen.testing:testing-kotlin:1.0.0-alpha4"
    ```

### Useful links

- Documentation for these libraries at [dual-screen library](https://docs.microsoft.com/dual-screen/android/api-reference/dualscreen-library/)
- Surface Duo developer documentation at [docs.microsoft.com/dual-screen](https://docs.microsoft.com/dual-screen).
- [Surface Duo developer blog](https://devblogs.microsoft.com/surface-duo)

### Samples that utilize this SDK

- [Kotlin samples](https://github.com/microsoft/surface-duo-sdk-samples-kotlin)
- [Java samples](https://github.com/microsoft/surface-duo-sdk-samples)
- [Dual Screen Experience Example](https://github.com/microsoft/surface-duo-dual-screen-experience-example)

### Other Surface Duo developer samples

- [Window Manager samples](https://github.com/microsoft/surface-duo-window-manager-samples)
- [Jetpack Compose samples](https://github.com/microsoft/surface-duo-compose-samples)
- [Android app samples](https://github.com/microsoft/surface-duo-app-samples)
- [Xamarin samples](https://github.com/microsoft/surface-duo-sdk-xamarin-samples)
- [Unity samples](https://github.com/microsoft/surface-duo-sdk-unity-samples)
- [React Native samples](https://github.com/microsoft/react-native-dualscreen)
- [Flutter samples](https://github.com/microsoft/surface-duo-sdk-samples-flutter)
