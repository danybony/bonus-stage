package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.bonysoft.bonusstage.ui.common.RefreshStatus
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/**
 * Cache-first screen body: loading until the first data arrives, a full error only when nothing
 * is cached, otherwise the content with pull-to-refresh. [data] is null until it has been loaded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> RefreshableContent(
    data: T?,
    status: RefreshStatus,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when {
        data == null && status.error != null -> ErrorView(status.error, onRetry = onRefresh, modifier = modifier)
        data == null -> LoadingView(modifier)
        else -> {
            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = status.isRefreshing,
                onRefresh = onRefresh,
                state = state,
                modifier = modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = state,
                        isRefreshing = status.isRefreshing,
                        containerColor = RetroTheme.colors.window,
                        color = RetroTheme.colors.onWindow,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) { content(data) }
            }
        }
    }
}
