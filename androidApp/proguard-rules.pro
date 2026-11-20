# R8 rules for the release build. Libraries ship their own rules (Ktor, kotlinx.serialization,
# Koin, SQLDelight, Coil, WorkManager); these cover what only this app knows about.

# rcheevos rhash: the JNI function is looked up by class and method name.
-keepclasseswithmembernames class net.bonysoft.bonusstage.rom.RHash {
    native <methods>;
}

# Ktor's client logs through SLF4J when present; it is not on Android.
-dontwarn org.slf4j.**
