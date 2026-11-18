package net.bonysoft.bonusstage.rom

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bonysoft.bonusstage.data.rom.RomFile
import net.bonysoft.bonusstage.data.rom.RomHash
import net.bonysoft.bonusstage.data.rom.RomLibrary

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
            context.contentResolver.openFileDescriptor(Uri.parse(file.id), "r")?.use { descriptor ->
                RHash.nativeHash(descriptor.fd, file.name).mapNotNull { entry ->
                    val console = entry.substringBefore(':').toIntOrNull() ?: return@mapNotNull null
                    RomHash(console, entry.substringAfter(':').lowercase())
                }
            }.orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Could not hash ${file.name}", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "SafRomLibrary"

        /** A safety net against huge trees; a ROM set rarely gets near it. */
        const val MAX_FILES = 20_000

        val COLUMNS = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
        )
    }
}
