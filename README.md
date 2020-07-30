# Surface Duo SDK

This repo contains the source code for the Microsoft Surface Duo SDK.

Please read the [code of conduct](CODE_OF_CONDUCT.md) and [contribution guidelines](CONTRIBUTING.md).

## Add to your project

1. Add the maven repository to the top-level **build.gradle** file:

    ```gradle
    allprojects {
       repositories {
           maven {
               url "https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1"
           }
       }
    }
    ```

2. Add dependencies to the module-level **build.gradle** file (current version may be different from what's shown here). There are multiple libraries available - you should always reference **Core** plus any additional libraries you plan to use:

    **Core**

    ```gradle
    implementation "com.microsoft.device.dualscreen:core:1.0.0-alpha01"
    ```

    **Bottom navigation**

    ```gradle
    implementation "com.microsoft.device.dualscreen:bottomnavigation:1.0.0-alpha03"
    ```

    **Fragments handler**

    ```gradle
    implementation "com.microsoft.device.dualscreen:fragmentshandler:1.0.0-alpha02"
    ```

    **Layouts**

    ```gradle
    implementation "com.microsoft.device.dualscreen:layouts:1.0.0-alpha02"
    ```

    **Tabs**

    ```gradle
    implementation "com.microsoft.device.dualscreen:tabs:1.0.0-alpha04"
    ```

## Useful links

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
