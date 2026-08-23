# Add project specific ProGuard rules here.

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.subulalhuda.**$$serializer { *; }
-keepclassmembers class com.subulalhuda.** {
    *** Companion;
}
-keepclasseswithmembers class com.subulalhuda.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# YouTube Player
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
