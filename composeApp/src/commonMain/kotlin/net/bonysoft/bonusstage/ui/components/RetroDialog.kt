package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/** A confirmation dialog drawn as a game window. */
@Composable
fun RetroDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        RetroDialogContent(title, text, confirmText, onConfirm, onDismiss, modifier, dismissText)
    }
}

@Composable
internal fun RetroDialogContent(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
) {
    RetroWindow(modifier = modifier, title = title) {
        Text(text = text, style = RetroTheme.typography.body, color = RetroTheme.colors.onWindow)
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            if (dismissText != null) {
                RetroButton(text = dismissText, onClick = onDismiss, style = RetroButtonStyle.Secondary)
            }
            RetroButton(text = confirmText, onClick = onConfirm)
        }
    }
}
