# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotation default values (e.g. retrofit2.http.Field.encoded)
-keepattributes AnnotationDefault

# Keep generic signatures and annotations
-keepattributes Signature
-keepattributes *Annotation*

# Google API Client Libraries - More specific rules
-keep class com.google.api.client.googleapis.** { *; }
-keep class com.google.api.client.http.** { *; }
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.util.** { *; }
-keepnames class com.google.api.client.** { *; }

# Google API Services (Sheets and Drive) - Essential classes only
-keep class com.google.api.services.sheets.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class com.google.api.services.** {
    public <init>(...);
    public <methods>;
}

# Google Play Services - Essential classes only
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keepclassmembers class com.google.android.gms.** {
    public <init>(...);
    public <methods>;
}

# Jackson JSON Processing - Essential classes
-keep class com.fasterxml.jackson.core.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }
-keep class com.fasterxml.jackson.databind.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonCreator <init>(...);
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
    @com.fasterxml.jackson.annotation.JsonProperty <methods>;
}

# Android HTTP Transport
-keep class com.google.api.client.extensions.android.http.** { *; }
-keep class com.google.api.client.extensions.android.json.** { *; }

# Retrofit2 and OkHttp (if using for other API calls)
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep serialization classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Hilt specific rules
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep class members with @Inject annotation
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlinx Serialization
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ir.act.personalAccountant.**$$serializer { *; }
-keepclassmembers class ir.act.personalAccountant.** {
    *** Companion;
}
-keepclasseswithmembers class ir.act.personalAccountant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Apache HTTP Client (used by Google API libraries) - Optimized
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.commons.codec.binary.**

# Keep essential Apache HTTP classes used by Google APIs
-keep class org.apache.http.HttpEntity { *; }
-keep class org.apache.http.HttpResponse { *; }
-keep class org.apache.http.client.HttpClient { *; }
-keep class org.apache.http.client.methods.** { *; }
-keep class org.apache.http.entity.** { *; }
-keep class org.apache.http.impl.client.** { *; }

# Keep Google API model classes from being obfuscated
-keepclassmembers class * extends com.google.api.client.json.GenericJson {
    <fields>;
    <init>(...);
}
-keepclassmembers class * extends com.google.api.client.util.GenericData {
    <fields>;
    <init>(...);
}

# Keep all Google Sheets API model classes
-keep class com.google.api.services.sheets.v4.model.** { *; }
-keep class com.google.api.services.drive.model.** { *; }

# Keep Google HTTP Client Transport classes
-keep class com.google.api.client.http.HttpTransport { *; }
-keep class com.google.api.client.http.HttpRequestFactory { *; }
-keep class com.google.api.client.http.HttpRequestInitializer { *; }

# Keep Google OAuth2 and Credentials classes
-keep class com.google.api.client.googleapis.extensions.android.gms.auth.** { *; }
-keep class com.google.api.client.googleapis.auth.** { *; }

# Keep reflection-based classes used by Google APIs
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
    @com.google.api.client.util.Value <fields>;
}

# Keep all constructors and public methods for Google API classes
-keepclassmembers class com.google.api.services.** {
    public <init>(...);
    public <methods>;
}

# Additional Google Play Services classes
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }

# Critical: Keep all classes that might be accessed via reflection
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

# Keep Google API Client Factory classes
-keep class com.google.api.client.http.HttpRequestFactory { *; }
-keep class com.google.api.client.http.HttpTransport { *; }
-keep class com.google.api.client.json.JsonFactory { *; }

# Keep method signatures for Google API services 
-keepclassmembers class com.google.api.services.sheets.v4.Sheets$** {
    <init>(...);
    public <methods>;
}
-keepclassmembers class com.google.api.services.drive.Drive$** {
    <init>(...);
    public <methods>;
}

# Keep Google API client builder classes
-keep class com.google.api.services.sheets.v4.Sheets$Builder { *; }
-keep class com.google.api.services.drive.Drive$Builder { *; }

# Ensure all Google API transport classes are preserved
-keep class com.google.api.client.extensions.android.http.AndroidHttp { *; }
-keep class com.google.api.client.json.jackson2.JacksonFactory { *; }