package net.bonysoft.bonusstage.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.raapi.RaMedia
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroButtonStyle
import net.bonysoft.bonusstage.ui.components.RetroDialogContent
import net.bonysoft.bonusstage.ui.components.RetroMenuItem
import net.bonysoft.bonusstage.ui.components.RetroProgressBar
import net.bonysoft.bonusstage.ui.components.RetroTextField
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/** Every base component on one screen, to review the theme (ADR 0004). */
@Composable
fun ThemeGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("BONUS STAGE", style = RetroTheme.typography.display, color = RetroTheme.colors.accent)
        Text(
            "Theme gallery: windows, menus, gauges and badges.",
            style = RetroTheme.typography.body,
            color = RetroTheme.colors.onBackgroundMuted,
        )
        MenuSample()
        ProgressSample()
        BadgeSample()
        ButtonSample()
        TextFieldSample()
        RetroDialogContent(
            title = "SIGN OUT?",
            text = "Your cached progress stays on this device.",
            confirmText = "Yes",
            dismissText = "No",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Composable
private fun MenuSample() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    RetroWindow(title = "MENU", modifier = Modifier.fillMaxWidth()) {
        listOf("Profile", "Library", "Wishlist", "Settings").forEachIndexed { index, label ->
            RetroMenuItem(text = label, selected = selected == index, onClick = { selected = index })
        }
    }
}

@Composable
private fun ProgressSample() {
    RetroWindow(title = "PROGRESS", modifier = Modifier.fillMaxWidth()) {
        Text("Sonic the Hedgehog", style = RetroTheme.typography.menu, color = RetroTheme.colors.onWindow)
        Spacer(Modifier.height(8.dp))
        RetroProgressBar(progress = 12f / 23f)
        Spacer(Modifier.height(4.dp))
        Text("12 / 23  ·  52%", style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted)
        Spacer(Modifier.height(12.dp))
        RetroProgressBar(progress = 1f, color = RetroTheme.colors.mastered)
        Spacer(Modifier.height(12.dp))
        RetroProgressBar(progress = 0.3f, color = RetroTheme.colors.hardcore)
    }
}

@Composable
private fun BadgeSample() {
    RetroWindow(title = "BADGES", modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RetroBadge(RaMedia.badge("250336"), contentDescription = "Unlocked badge")
            RetroBadge(RaMedia.badge("325551"), contentDescription = "Unlocked badge")
            RetroBadge(RaMedia.badge("108302", locked = true), contentDescription = "Locked badge", locked = true)
            RetroBadge(RaMedia.image("/Images/067895.png"), contentDescription = "Game icon")
        }
    }
}

@Composable
private fun ButtonSample() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RetroButton(text = "Start", onClick = {})
        RetroButton(text = "Back", onClick = {}, style = RetroButtonStyle.Secondary)
        RetroButton(text = "Off", onClick = {}, enabled = false)
    }
}

@Composable
private fun TextFieldSample() {
    var name by rememberSaveable { mutableStateOf("MaxMilyin") }
    RetroWindow(title = "SIGN IN", modifier = Modifier.fillMaxWidth()) {
        RetroTextField(value = name, onValueChange = { name = it }, label = "Username")
        Spacer(Modifier.height(12.dp))
        RetroTextField(
            value = "",
            onValueChange = {},
            label = "Web API key",
            isError = true,
            supportingText = "The key was rejected by RetroAchievements.",
        )
    }
}

@Preview
@Composable
private fun ThemeGalleryPreview() {
    RetroTheme { ThemeGallery() }
}

@Preview
@Composable
private fun MenuPreview() {
    RetroTheme { Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) { MenuSample() } }
}

@Preview
@Composable
private fun ProgressPreview() {
    RetroTheme { Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) { ProgressSample() } }
}

@Preview
@Composable
private fun BadgePreview() {
    RetroTheme { Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) { BadgeSample() } }
}

@Preview
@Composable
private fun ButtonPreview() {
    RetroTheme { Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) { ButtonSample() } }
}

@Preview
@Composable
private fun TextFieldPreview() {
    RetroTheme { Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) { TextFieldSample() } }
}

@Preview
@Composable
private fun DialogPreview() {
    RetroTheme {
        Box(Modifier.background(RetroTheme.colors.background).padding(16.dp)) {
            RetroDialogContent("SIGN OUT?", "Your cached progress stays on this device.", "Yes", {}, {}, dismissText = "No")
        }
    }
}
