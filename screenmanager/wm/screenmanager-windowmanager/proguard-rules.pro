# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# WindowManager alpha01 has currently a bug that removes more classes than needed.
# The fix will be added in next alpha02.
# In the mean time we can apply this rule to keep wm classes
# More info:
# Issue: https://issuetracker.google.com/issues/157286362
# Fix already merged for alpha02: https://android-review.googlesource.com/c/platform/frameworks/support/+/1324178/
-keep class androidx.window.** { *; }