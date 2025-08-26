# Keep all data model classes used by Firebase, Room, and Kotlinx Serialization.
-keep class com.ozonic.offnbuy.data.local.model.** { *; }
-keep class com.ozonic.offnbuy.data.remote.dto.** { *; }
-keep class com.ozonic.offnbuy.domain.model.** { *; }

# For Kotlinx Serialization: Keep classes annotated with @Serializable and their members
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
    <methods>;
}
-keep class kotlinx.serialization.** { *; }

# For Room Database: Keep DAOs, Entities, and the Database class
-keep class * implements androidx.room.Dao { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *

# For Kotlin Coroutines, which your ViewModels and Repositories use extensively
-keepclassmembers class kotlinx.coroutines.flow.** {
    *;
}
-keepclassmembers class **$*COROUTINE$* {
    *;
}
-keepclassmembers class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    *;
}

# General rule for Kotlin data classes to preserve their 'copy' and other synthetic methods
-keepclassmembers class ** extends java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}