# Proguard rules for BabyyGurll Admin
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.babyygurll.admin.data.model.** { *; }

