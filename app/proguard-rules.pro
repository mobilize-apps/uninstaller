# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/majedev/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep enums
-keepclassmembers enum * { *; }
-keepattributes Signature
-keepattributes *Annotation*


# Fabric.io
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**


# Butterknife rules
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


# Remove debug, verbose and info logs (Android & Timber)
-assumenosideeffects class android.util.Log {
    public static *** i(...);
    public static *** d(...);
    public static *** v(...);
}

-assumenosideeffects class timber.log.Timber* {
    public static *** i(...);
    public static *** d(...);
    public static *** v(...);
}