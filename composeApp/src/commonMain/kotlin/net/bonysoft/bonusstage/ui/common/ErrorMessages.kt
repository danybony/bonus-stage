package net.bonysoft.bonusstage.ui.common

import androidx.compose.runtime.Composable
import net.bonysoft.bonusstage.raapi.RaApiException
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.error_network
import net.bonysoft.bonusstage.resources.error_not_found
import net.bonysoft.bonusstage.resources.error_rate_limited
import net.bonysoft.bonusstage.resources.error_server
import net.bonysoft.bonusstage.resources.error_unauthorized
import net.bonysoft.bonusstage.resources.error_unexpected
import org.jetbrains.compose.resources.stringResource

/** A user-facing explanation of a failed API call. */
@Composable
fun errorMessage(error: Throwable): String = when (error) {
    is RaApiException.Unauthorized -> stringResource(Res.string.error_unauthorized)
    is RaApiException.NotFound -> stringResource(Res.string.error_not_found)
    is RaApiException.RateLimited -> stringResource(Res.string.error_rate_limited)
    is RaApiException.Network -> stringResource(Res.string.error_network)
    is RaApiException.Http -> stringResource(Res.string.error_server, error.status)
    else -> stringResource(Res.string.error_unexpected)
}
