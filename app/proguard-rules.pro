# Firebase / Firestore model classes need no-arg constructor + fields preserved
-keepclassmembers class com.perru.lifeline.domain.model.** {
  <init>();
  <fields>;
}
-keep class com.cloudinary.** { *; }
