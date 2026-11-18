package net.bonysoft.bonusstage.data.rom

import androidx.compose.runtime.Composable

/** ROM scans need rhash through cinterop on iOS; not built yet (see the iOS release, #18). */
@Composable
actual fun rememberRomFolderPicker(onPicked: (folder: String) -> Unit): (() -> Unit)? = null
