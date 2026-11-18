@file:OptIn(ExperimentalForeignApi::class)

package net.bonysoft.bonusstage.data.rom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject

/**
 * The Files folder picker. The folder is kept as a security-scoped bookmark (see IosRomLibrary),
 * so it can be scanned again after a restart, like a persisted SAF tree on Android.
 */
@Composable
actual fun rememberRomFolderPicker(onPicked: (folder: String) -> Unit): (() -> Unit)? {
    val controller = LocalUIViewController.current
    val currentOnPicked = rememberUpdatedState(onPicked)
    // The picker only keeps a weak reference to its delegate.
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
                IosRomLibrary.bookmark(url)?.let(currentOnPicked.value)
            }
        }
    }
    return remember(controller) {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeFolder), asCopy = false)
            picker.delegate = delegate
            controller.presentViewController(picker, animated = true, completion = null)
        }
    }
}
