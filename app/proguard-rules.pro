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

# Preserve annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.hilt.** { *; }
-keep @interface dagger.hilt.** { *; }
-keep @interface com.google.dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* *;
    @com.google.dagger.hilt.* *;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep @interface kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.* *;
}

# Room
-keep class androidx.room.** { *; }
-keep @interface androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* *;
}

# Retrofit / OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keepattributes Signature

# Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature

# Coil
-keep class coil.** { *; }
-keep class com.google.accompanist.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# Unity Ads / LevelPlay
-keep class com.unity3d.** { *; }
-keep class com.unity3d.services.** { *; }

# Unity Mediation Adapters
-keep class com.unity3d.mediation.** { *; }
-dontwarn com.unity3d.mediation.**

# Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Prevent R8 from removing unused resources
-keepclassmembers class * {
    public static <fields>;
}

# Keep BuildConfig
-keep class com.example.myapplication.BuildConfig { *; }

# Keep R classes
-keep class com.example.myapplication.R { *; }
-keep class com.example.myapplication.R$* { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep @SuppressWarnings
-keepattributes *Annotation*

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}