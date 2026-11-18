package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.award_tag_beaten
import net.bonysoft.bonusstage.resources.award_tag_beaten_softcore
import net.bonysoft.bonusstage.resources.award_tag_completed
import net.bonysoft.bonusstage.resources.award_tag_mastered
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource

/** A small pixel label: award, demo, hardcore. */
@Composable
fun RetroTag(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = RetroTheme.colors.windowHighlight) {
    Text(
        text = text,
        style = RetroTheme.typography.label,
        color = textColor,
        maxLines = 1,
        modifier = modifier
            .retroFrame(fill = color, border = RetroTheme.colors.windowBorder, highlight = null)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

/** A selectable filter chip. */
@Composable
fun RetroChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RetroTheme.colors
    Text(
        text = text.uppercase(),
        style = RetroTheme.typography.label,
        color = if (selected) colors.onAccent else colors.onBackground,
        maxLines = 1,
        modifier = modifier
            .heightIn(min = 40.dp)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .retroFrame(
                fill = if (selected) colors.accent else colors.backgroundVariant,
                border = if (selected) colors.windowBorder else colors.onBackgroundMuted,
                highlight = null,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

fun Award.color(colors: net.bonysoft.bonusstage.ui.theme.RetroColors): Color = when (this) {
    Award.Mastered, Award.Completed -> colors.mastered
    Award.BeatenHardcore -> colors.hardcore
    Award.BeatenSoftcore -> colors.softcore
}

@Composable
fun AwardTag(award: Award, modifier: Modifier = Modifier) {
    val text = when (award) {
        Award.Mastered -> stringResource(Res.string.award_tag_mastered)
        Award.Completed -> stringResource(Res.string.award_tag_completed)
        Award.BeatenHardcore -> stringResource(Res.string.award_tag_beaten)
        Award.BeatenSoftcore -> stringResource(Res.string.award_tag_beaten_softcore)
    }
    val colors = RetroTheme.colors
    RetroTag(
        text = text,
        color = award.color(colors),
        textColor = if (award == Award.Mastered || award == Award.Completed) colors.onAccent else colors.windowHighlight,
        modifier = modifier,
    )
}
