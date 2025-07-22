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
#keep BuildConfig constants (if you use BuildConfig.BUILD_TYPE, etc.)
-keep class **.BuildConfig { *; }

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions

-keep class com.caastv.tvapp.model.** { *; }
-keep class com.caastv.tvapp.utils.network.** { *; }
-keepdirectories com.example.tvapp.model.**

-keep class androidx.leanback.** { *; }
-keep interface androidx.leanback.** { *; }
-keepclassmembers class * extends androidx.leanback.widget.Presenter {
    public <init>(...);
}
-keepclassmembers class * extends androidx.leanback.widget.Row {
    public <init>(...);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class ** {
    @java.lang.Deprecated public *;
    @java.lang.Override public *;
}

# Keep ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}


# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Jetpack Compose runtime classes
-keep class androidx.compose.runtime.** { *; }


# Keep Retrofit and OkHttp classes
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Keep Retrofit interfaces
-keep interface com.caastv.tvapp.utils.network.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep GSON/Jackson if used
-keep class com.google.gson.** { *; }
-keep class org.codehaus.** { *; }
-keep class com.fasterxml.** { *; }


# Keep Hilt/Dagger generated code
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManagerHolder { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# Hilt specific rules
-keep class dagger.hilt.internal.aggregatedroot.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManagerHolder { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# Keep test classes
-keep class * extends junit.framework.TestCase { *; }
-keep class org.junit.** { *; }
-keep class androidx.test.** { *; }

# Gson models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room schema
-keepclassmembers class androidx.room.** { *; }

# Keep Error Prone annotations
-keep class com.google.errorprone.annotations.** { *; }
-dontwarn com.google.errorprone.annotations.**

