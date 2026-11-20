package net.bonysoft.bonusstage.rom

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bonysoft.bonusstage.data.rom.RomFile
import net.bonysoft.bonusstage.data.rom.RomHash
import net.bonysoft.bonusstage.data.rom.RomExtensions
import net.bonysoft.bonusstage.data.rom.RomLibrary
import java.io.File
import java.util.zip.ZipInputStream

/** ROM folders picked through the Storage Access Framework, hashed with the native rhash. */
class SafRomLibrary(private val context: Context) : RomLibrary {

    override suspend fun listFiles(folder: String): List<RomFile> = withContext(Dispatchers.IO) {
        val tree = Uri.parse(folder)
        val files = mutableListOf<RomFile>()
        val pending = ArrayDeque(listOf(DocumentsContract.getTreeDocumentId(tree)))
        while (pending.isNotEmpty() && files.size < MAX_FILES) {
            val parent = pending.removeFirst()
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parent)
            context.contentResolver.query(children, COLUMNS, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val name = cursor.getString(1) ?: continue
                    if (cursor.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) {
                        pending += id
                    } else {
                        files += RomFile(DocumentsContract.buildDocumentUriUsingTree(tree, id).toString(), name, cursor.getLong(3))
                    }
                }
            }
        }
        files
    }

    override suspend fun hash(file: RomFile): List<RomHash> = withContext(Dispatchers.IO) {
        try {
            // rhash hashes any .zip by its name, as an arcade set. A zip holding one console ROM is
            // hashed by that ROM's content instead, as emulators do, and skips the arcade catalog.
            val zipped = if (file.name.endsWith(".zip", ignoreCase = true)) hashZippedRom(file) else emptyList()
            zipped.ifEmpty {
                context.contentResolver.openFileDescriptor(Uri.parse(file.id), "r")?.use { descriptor ->
                    hashDescriptor(descriptor, file.name)
                }.orEmpty()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not hash ${file.name}", e)
            emptyList()
        }
    }

    /**
     * Hashes the ROM inside a zip that holds exactly one (next to readme or text files), extracted
     * to a temporary file. Zips with several ROMs, like disc images split in tracks, are skipped.
     */
    private fun hashZippedRom(file: RomFile): List<RomHash> {
        val extracted = File.createTempFile("zipped-rom", null, context.cacheDir)
        try {
            var romName: String? = null
            val input = context.contentResolver.openInputStream(Uri.parse(file.id)) ?: return emptyList()
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name.substringAfterLast('/')
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (entry.isDirectory || extension !in RomExtensions || extension in ARCHIVES) continue
                    if (romName != null || entry.size > MAX_ZIPPED_ROM_BYTES) return emptyList()
                    romName = name
                    // The size in the entry header can be missing, so the copy stops at the limit too.
                    extracted.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var copied = 0L
                        while (true) {
                            val read = zip.read(buffer)
                            if (read < 0) break
                            copied += read
                            if (copied > MAX_ZIPPED_ROM_BYTES) return emptyList()
                            output.write(buffer, 0, read)
                        }
                    }
                }
            }
            val name = romName ?: return emptyList()
            return ParcelFileDescriptor.open(extracted, ParcelFileDescriptor.MODE_READ_ONLY).use { hashDescriptor(it, name) }
        } finally {
            extracted.delete()
        }
    }

    private fun hashDescriptor(descriptor: ParcelFileDescriptor, name: String): List<RomHash> =
        RHash.nativeHash(descriptor.fd, name).mapNotNull { entry ->
            val console = entry.substringBefore(':').toIntOrNull() ?: return@mapNotNull null
            RomHash(console, entry.substringAfter(':').lowercase())
        }

    private companion object {
        const val TAG = "SafRomLibrary"

        /** A safety net against huge trees; a ROM set rarely gets near it. */
        const val MAX_FILES = 20_000

        /** Archives inside archives are not opened. */
        val ARCHIVES = setOf("zip", "7z")

        /** Larger than any cartridge ROM; bigger zipped images (discs) are not extracted. */
        const val MAX_ZIPPED_ROM_BYTES = 512L * 1024 * 1024

        val COLUMNS = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
        )
    }
}
