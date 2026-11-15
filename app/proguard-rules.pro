# Kotlinx Serialization Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable class *;
}

# Supabase & Ktor Rules
-keep class io.github.jan.tennert.supabase.** { *; }
-keep class io.ktor.** { *; }
