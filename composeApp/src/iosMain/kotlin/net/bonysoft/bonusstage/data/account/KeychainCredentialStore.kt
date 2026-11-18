package net.bonysoft.bonusstage.data.account

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Keeps the account in the iOS Keychain as a generic password, readable after the first unlock
 * (so background refresh works) and never synced or restored to another device.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainCredentialStore : CredentialStore {

    // Retained for the life of the app: this store is a singleton.
    private val service = CFBridgingRetain(SERVICE)
    private val account = CFBridgingRetain(ACCOUNT)

    override suspend fun load(): String? = memScoped {
        val result = alloc<CFTypeRefVar>()
        val status = withQuery(kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne) {
            SecItemCopyMatching(it, result.ptr)
        }
        if (status != errSecSuccess) return@memScoped null
        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override suspend fun save(value: String) {
        clear()
        @Suppress("CAST_NEVER_SUCCEEDS")
        val data = CFBridgingRetain((value as NSString).dataUsingEncoding(NSUTF8StringEncoding))
        withQuery(kSecValueData to data, kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly) {
            SecItemAdd(it, null)
        }
        data?.let { CFRelease(it) }
    }

    override suspend fun clear() {
        withQuery { SecItemDelete(it) }
    }

    /** Runs [block] with a query for the account item plus [extra] attributes, then releases it. */
    private inline fun <T> withQuery(vararg extra: Pair<CFTypeRef?, CFTypeRef?>, block: (CFMutableDictionaryRef?) -> T): T {
        val entries = arrayOf(kSecClass to kSecClassGenericPassword, kSecAttrService to service, kSecAttrAccount to account, *extra)
        val query = CFDictionaryCreateMutable(null, entries.size.convert(), kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
        entries.forEach { (key, value) -> CFDictionaryAddValue(query, key, value) }
        try {
            return block(query)
        } finally {
            CFRelease(query)
        }
    }

    private companion object {
        const val SERVICE = "net.bonysoft.bonusstage"
        const val ACCOUNT = "retroachievements-account"
    }
}
