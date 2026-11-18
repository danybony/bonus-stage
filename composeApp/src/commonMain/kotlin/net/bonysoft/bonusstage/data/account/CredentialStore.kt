package net.bonysoft.bonusstage.data.account

/**
 * Encrypted storage for the account: Android Keystore and iOS Keychain. Implementations must
 * return null rather than throw when stored data cannot be read (for example a reset keystore).
 */
interface CredentialStore {
    suspend fun load(): String?
    suspend fun save(value: String)
    suspend fun clear()
}

/** Something that holds per-user data and must forget it on sign-out (caches, scheduled work). */
fun interface UserDataCleaner {
    suspend fun clear()
}
