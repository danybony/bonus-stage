package net.bonysoft.bonusstage.data.rom

import androidx.compose.runtime.Composable

/** A file in the user's ROM folder. [id] is whatever the platform needs to open it again. */
data class RomFile(val id: String, val name: String, val size: Long)

/** One hash rhash computed for a file, for one candidate console. */
data class RomHash(val consoleId: Int, val hash: String)

/**
 * Reads the user's ROM folder and hashes its files with rcheevos rhash. Only provided where a
 * native rhash is available (Android for now): resolve it with `getOrNull`.
 */
interface RomLibrary {
    suspend fun listFiles(folder: String): List<RomFile>

    /** Empty when rhash cannot hash the file (unknown format, unreadable, multi-file disc). */
    suspend fun hash(file: RomFile): List<RomHash>
}

/** Lets the user pick a folder; returns null where picking ROM folders is not supported. */
@Composable
expect fun rememberRomFolderPicker(onPicked: (folder: String) -> Unit): (() -> Unit)?

/** Extensions worth hashing: everything else in a ROM folder (saves, covers, notes) is skipped. */
val RomExtensions = setOf(
    "nes", "fds", "unf", "unif", "sfc", "smc", "swc", "fig", "bs", "gb", "gbc", "gba", "nds", "dsi",
    "n64", "z64", "v64", "ndd", "md", "gen", "smd", "bin", "sms", "gg", "sg", "32x", "pce", "sgx",
    "a26", "a78", "lnx", "lyx", "j64", "jag", "ngp", "ngc", "ws", "wsc", "pc2", "vb", "min", "col",
    "int", "vec", "chf", "o2", "tic", "zip", "iso", "chd", "cue", "pbp", "cso", "3ds", "cia",
    "rom", "mx1", "mx2", "dsk", "d64", "tap", "cas", "uze", "wasm", "arduboy", "hex",
)

fun RomFile.isCandidate(): Boolean = name.substringAfterLast('.', "").lowercase() in RomExtensions
