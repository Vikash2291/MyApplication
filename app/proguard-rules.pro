# Keep line numbers for readable Crashlytics stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# kotlinx.serialization
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.myapplication.**$$serializer { *; }

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Unity Ads
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-dontwarn com.unity3d.**

# Google Mobile Ads / UMP ship their own consumer rules.
