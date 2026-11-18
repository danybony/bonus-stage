package net.bonysoft.bonusstage.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.error_title
import net.bonysoft.bonusstage.resources.loading
import net.bonysoft.bonusstage.resources.retry
import net.bonysoft.bonusstage.resources.stale_notice
import net.bonysoft.bonusstage.ui.common.errorMessage
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource

/** "LOADING" with a blinking block cursor, like a cartridge booting. */
@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    val blink = rememberInfiniteTransition(label = "cursor")
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 500, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorAlpha",
    )
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.loading).uppercase(), style = RetroTheme.typography.title, color = RetroTheme.colors.onBackground)
            Text(
                "█",
                style = RetroTheme.typography.title,
                color = RetroTheme.colors.accent,
                modifier = Modifier.graphicsLayer { this.alpha = alpha },
            )
        }
    }
}

/** Full-screen error when there is nothing cached to show. */
@Composable
fun ErrorView(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        RetroWindow(title = stringResource(Res.string.error_title), modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
            Text(errorMessage(error), style = RetroTheme.typography.body, color = RetroTheme.colors.onWindow)
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                RetroButton(text = stringResource(Res.string.retry), onClick = onRetry)
            }
        }
    }
}

/** A strip above cached content when the last refresh failed. */
@Composable
fun StaleNotice(error: Throwable, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.stale_notice, errorMessage(error)),
        style = RetroTheme.typography.bodySmall,
        color = RetroTheme.colors.onAccent,
        modifier = modifier
            .fillMaxWidth()
            .retroFrame(fill = RetroTheme.colors.accent, border = RetroTheme.colors.windowBorder, highlight = null)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
