# Surface Duo SDK

This repo contains the source code for the Surface Duo SDK.

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

2. Add the SDK dependency to the module-level **build.gradle** file (current version may be different from what's shown here):

    ```gradle
    implementation "com.microsoft.device:dualscreen-layout:1.0.0-alpha01"
    ```

## Useful links

* Documentation at [docs.microsoft.com/dual-screen](https://docs.microsoft.com/dual-screen).
* [Blog](https://devblogs.microsoft.com/surface-duo)

### Samples that utilize this SDK

* [Java samples](https://github.com/microsoft/surface-duo-sdk-samples)
* [Kotlin samples](https://github.com/microsoft/surface-duo-sdk-samples-kotlin)

### Other Surface Duo developer samples

* [Xamarin samples](https://github.com/microsoft/surface-duo-sdk-xamarin-samples)
* [Unity samples](https://github.com/microsoft/surface-duo-sdk-unity-samples)
* [React Native samples](https://github.com/microsoft/react-native-dualscreen)
