# Proguard rules for BabyyGurll Customer Boutique
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.babyygurll.store.data.model.** { *; }

