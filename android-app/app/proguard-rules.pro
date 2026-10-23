# Currently inactive (isMinifyEnabled = false for the pilot — see
# app/build.gradle.kts). Left ready for when minification gets turned on.

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.magicmirror.vton.**$$serializer { *; }
-keepclassmembers class com.magicmirror.vton.** {
    *** Companion;
}
-keepclasseswithmembers class com.magicmirror.vton.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature, Exceptions, *Annotation*
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# AndroidUSBCamera (libausbc) — keep everything; avoid stripping JNI/reflection-based internals
-keep class com.jiangdg.ausbc.** { *; }
-dontwarn com.jiangdg.ausbc.**
