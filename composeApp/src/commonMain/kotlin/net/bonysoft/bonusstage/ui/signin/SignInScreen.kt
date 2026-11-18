package net.bonysoft.bonusstage.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.app_name
import net.bonysoft.bonusstage.resources.error_missing_fields
import net.bonysoft.bonusstage.resources.sign_in_api_key
import net.bonysoft.bonusstage.resources.sign_in_button
import net.bonysoft.bonusstage.resources.sign_in_checking
import net.bonysoft.bonusstage.resources.sign_in_demo
import net.bonysoft.bonusstage.resources.sign_in_demo_help
import net.bonysoft.bonusstage.resources.sign_in_hide_key
import net.bonysoft.bonusstage.resources.sign_in_key_help
import net.bonysoft.bonusstage.resources.sign_in_open_settings
import net.bonysoft.bonusstage.resources.sign_in_show_key
import net.bonysoft.bonusstage.resources.sign_in_tagline
import net.bonysoft.bonusstage.resources.sign_in_title
import net.bonysoft.bonusstage.resources.sign_in_unofficial
import net.bonysoft.bonusstage.resources.sign_in_username
import net.bonysoft.bonusstage.ui.common.errorMessage
import net.bonysoft.bonusstage.ui.components.PixelIcon
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroButtonStyle
import net.bonysoft.bonusstage.ui.components.RetroMenuItem
import net.bonysoft.bonusstage.ui.components.RetroTextField
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val RA_SETTINGS_URL = "https://retroachievements.org/settings"

@Composable
fun SignInRoute(modifier: Modifier = Modifier, viewModel: SignInViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignInScreen(
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onApiKeyChange = viewModel::onApiKeyChange,
        onSignIn = viewModel::signIn,
        onDemo = viewModel::signInDemo,
        modifier = modifier,
    )
}

@Composable
fun SignInScreen(
    state: SignInUiState,
    onUsernameChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    val uriHandler = LocalUriHandler.current
    var showKey by rememberSaveable { mutableStateOf(false) }
    val error = when (val e = state.error) {
        null -> null
        SignInError.MissingFields -> stringResource(Res.string.error_missing_fields)
        is SignInError.Api -> errorMessage(e.cause)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(Res.string.app_name).uppercase(), style = RetroTheme.typography.display, color = colors.accent)
            Text(stringResource(Res.string.sign_in_tagline), style = RetroTheme.typography.body, color = colors.onBackgroundMuted)
        }

        RetroWindow(title = stringResource(Res.string.sign_in_title), modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RetroTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = stringResource(Res.string.sign_in_username),
                    enabled = !state.isChecking,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                )
                RetroTextField(
                    value = state.apiKey,
                    onValueChange = onApiKeyChange,
                    label = stringResource(Res.string.sign_in_api_key),
                    enabled = !state.isChecking,
                    isError = error != null,
                    supportingText = error,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Go,
                    ),
                    keyboardActions = KeyboardActions(onGo = { onSignIn() }),
                    trailing = {
                        val label = stringResource(if (showKey) Res.string.sign_in_hide_key else Res.string.sign_in_show_key)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(role = Role.Button, onClick = { showKey = !showKey })
                                .semantics { contentDescription = label },
                            contentAlignment = Alignment.Center,
                        ) {
                            PixelIcon(
                                icon = if (showKey) PixelIcons.EyeOff else PixelIcons.Eye,
                                color = RetroTheme.colors.onWindow,
                            )
                        }
                    },
                )
                Text(stringResource(Res.string.sign_in_key_help), style = RetroTheme.typography.bodySmall, color = colors.onWindowMuted)
                RetroMenuItem(
                    text = stringResource(Res.string.sign_in_open_settings),
                    onClick = { uriHandler.openUri(RA_SETTINGS_URL) },
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    RetroButton(
                        text = stringResource(if (state.isChecking) Res.string.sign_in_checking else Res.string.sign_in_button),
                        onClick = onSignIn,
                        enabled = state.canSubmit,
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.sign_in_demo_help), style = RetroTheme.typography.body, color = colors.onBackgroundMuted)
            RetroButton(text = stringResource(Res.string.sign_in_demo), onClick = onDemo, style = RetroButtonStyle.Secondary)
        }

        Text(stringResource(Res.string.sign_in_unofficial), style = RetroTheme.typography.bodySmall, color = colors.onBackgroundMuted)
    }
}

@Preview
@Composable
private fun SignInScreenPreview() {
    RetroTheme {
        SignInScreen(SignInUiState(username = "BonusDemo"), {}, {}, {}, {})
    }
}
