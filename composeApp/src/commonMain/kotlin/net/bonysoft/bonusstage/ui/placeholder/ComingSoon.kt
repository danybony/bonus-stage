package net.bonysoft.bonusstage.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/** Stand-in for screens that land in later pull requests. */
@Composable
fun ComingSoon(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    RetroScaffold(title = title, modifier = modifier, onBack = onBack) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("COMING SOON", style = RetroTheme.typography.title, color = RetroTheme.colors.onBackgroundMuted)
        }
    }
}
