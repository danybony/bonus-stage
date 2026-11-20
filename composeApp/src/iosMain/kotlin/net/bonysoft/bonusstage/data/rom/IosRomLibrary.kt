@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package net.bonysoft.bonusstage.data.rom

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.bonysoft.bonusstage.rhash.bs_rhash
import platform.Foundation.NSData
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create

/**
 * ROM folders picked in Files, hashed with rcheevos rhash (cinterop, src/nativeInterop/cinterop).
 * iOS gives real paths inside the folder, so rhash reads files itself, including .cue tracks.
 */
class IosRomLibrary : RomLibrary {

    /** The folder whose security scope is open; it stays open for the hashing that follows a listing. */
    private var scoped: NSURL? = null

    override suspend fun listFiles(folder: String): List<RomFile> = withContext(Dispatchers.IO) {
        val root = resolve(folder) ?: return@withContext emptyList()
        scoped?.stopAccessingSecurityScopedResource()
        root.startAccessingSecurityScopedResource()
        scoped = root
        val manager = NSFileManager.defaultManager
        val enumerator = manager.enumeratorAtURL(
            root,
            includingPropertiesForKeys = null,
            options = NSDirectoryEnumerationSkipsHiddenFiles,
            errorHandler = null,
        ) ?: return@withContext emptyList()
        val files = mutableListOf<RomFile>()
        while (files.size < MAX_FILES) {
            val url = enumerator.nextObject() as? NSURL ?: break
            val path = url.path ?: continue
            val attributes = manager.attributesOfItemAtPath(path, error = null) ?: continue
            if (attributes[NSFileType] == NSFileTypeDirectory) continue
            val size = (attributes[NSFileSize] as? NSNumber)?.longLongValue ?: 0L
            files += RomFile(id = path, name = url.lastPathComponent ?: path.substringAfterLast('/'), size = size)
        }
        files
    }

    override suspend fun hash(file: RomFile): List<RomHash> = withContext(Dispatchers.IO) {
        memScoped {
            val consoles = allocArray<IntVar>(MAX_HASHES)
            val hashes = allocArray<ByteVar>(MAX_HASHES * HASH_LENGTH)
            val count = bs_rhash(file.id, consoles, hashes, MAX_HASHES)
            (0 until count).map { i ->
                RomHash(consoles[i], (hashes + i * HASH_LENGTH)!!.readBytes(HASH_LENGTH).decodeToString().lowercase())
            }
        }
    }

    companion object {
        private const val PREFIX = "bookmark:"
        private const val MAX_HASHES = 12
        private const val HASH_LENGTH = 32

        /** A safety net against huge trees; a ROM set rarely gets near it. */
        private const val MAX_FILES = 20_000

        /** Turns a picked folder into a string that can be stored and resolved after a restart. */
        fun bookmark(url: NSURL): String? {
            val accessing = url.startAccessingSecurityScopedResource()
            try {
                val data = url.bookmarkDataWithOptions(0u, includingResourceValuesForKeys = null, relativeToURL = null, error = null)
                return data?.let { PREFIX + it.base64EncodedStringWithOptions(0u) }
            } finally {
                if (accessing) url.stopAccessingSecurityScopedResource()
            }
        }

        private fun resolve(folder: String): NSURL? {
            if (!folder.startsWith(PREFIX)) return null
            val data = NSData.create(base64EncodedString = folder.removePrefix(PREFIX), options = 0u) ?: return null
            return NSURL.URLByResolvingBookmarkData(data, options = 0u, relativeToURL = null, bookmarkDataIsStale = null, error = null)
        }
    }
}
