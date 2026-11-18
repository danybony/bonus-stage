package net.bonysoft.bonusstage.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.recap_last_month
import net.bonysoft.bonusstage.resources.recap_this_month
import net.bonysoft.bonusstage.resources.recap_title
import net.bonysoft.bonusstage.resources.share_text_recap
import net.bonysoft.bonusstage.ui.components.ErrorView
import net.bonysoft.bonusstage.ui.components.LoadingView
import net.bonysoft.bonusstage.ui.components.RetroChip
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecapRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: RecapViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RetroScaffold(title = stringResource(Res.string.recap_title), onBack = onBack, modifier = modifier) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RetroChip(stringResource(Res.string.recap_this_month), selected = !state.lastMonth, onClick = { viewModel.select(lastMonth = false) })
                RetroChip(stringResource(Res.string.recap_last_month), selected = state.lastMonth, onClick = { viewModel.select(lastMonth = true) })
            }
            val recap = state.recap
            val error = state.error
            when {
                recap != null -> ShareableCard(
                    fileName = "bonus-stage-recap-${recap.month}.png",
                    shareText = stringResource(Res.string.share_text_recap, recap.monthLabel().lowercase().replaceFirstChar { it.uppercase() }),
                    imageCount = recap.imageUrls().size,
                    modifier = Modifier.padding(top = 16.dp),
                ) { onLoaded -> MonthlyRecapCard(recap, state.username, onLoaded) }
                error != null -> ErrorView(error, onRetry = { viewModel.select(state.lastMonth) })
                else -> LoadingView(Modifier.padding(top = 120.dp).align(Alignment.CenterHorizontally))
            }
        }
    }
}
