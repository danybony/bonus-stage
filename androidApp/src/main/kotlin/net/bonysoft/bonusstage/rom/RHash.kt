package net.bonysoft.bonusstage.rom

/** rcheevos rhash through JNI (src/main/cpp). */
internal object RHash {
    init {
        System.loadLibrary("bonusstage_rhash")
    }

    /** Hashes the file open on [fd]; [name] gives rhash the extension. Returns "consoleId:hash" entries. */
    @JvmStatic
    external fun nativeHash(fd: Int, name: String): Array<String>
}
