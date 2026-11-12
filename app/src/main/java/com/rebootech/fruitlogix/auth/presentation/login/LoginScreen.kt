package com.rebootech.fruitlogix.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.BuildConfig
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.presentation.components.AuthBrand
import com.rebootech.fruitlogix.auth.presentation.components.AuthCheckRow
import com.rebootech.fruitlogix.auth.presentation.components.AuthErrorBanner
import com.rebootech.fruitlogix.auth.presentation.components.AuthLanguageSwitch
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val info = state.infoMessage?.let { stringResource(it) }

    LaunchedEffect(state.infoMessage) {
        if (info != null) {
            snackbarHostState.showSnackbar(info)
            viewModel.onInfoShown()
        }
    }

    LoginContent(
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onKeepSignedInChange = viewModel::onKeepSignedInChange,
        onSignIn = viewModel::signIn,
        onForgotPassword = viewModel::onForgotPassword,
        onCreateAccount = onNavigateToSignUp,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onKeepSignedInChange: (Boolean) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showLanguageSwitch: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md, vertical = spacing.sm)
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                if (showLanguageSwitch) AuthLanguageSwitch()
            }

            Spacer(modifier = Modifier.height(spacing.lg))
            AuthBrand()
            Spacer(modifier = Modifier.height(spacing.lg))

            Text(
                text = stringResource(R.string.login_welcome),
                style = FruitLogixTheme.typography.headlineMedium,
                color = scheme.onSurface
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                style = FruitLogixTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.md))

            LabeledTextField(
                label = stringResource(R.string.login_username),
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = stringResource(R.string.login_username_placeholder),
                leadingIconRes = R.drawable.ic_person,
                error = state.usernameError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Ascii,
                autofill = ContentType.Username
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            LabeledTextField(
                label = stringResource(R.string.login_password),
                value = state.password,
                onValueChange = onPasswordChange,
                leadingIconRes = R.drawable.ic_fleet_lock,
                error = state.passwordError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    onSignIn()
                },
                autofill = ContentType.Password,
                isPassword = true,
                passwordVisible = state.isPasswordVisible,
                onTogglePasswordVisible = onTogglePasswordVisibility
            )

            Spacer(modifier = Modifier.height(spacing.xs))
            AuthCheckRow(
                label = stringResource(R.string.login_keep_signed_in),
                checked = state.keepSignedIn,
                onCheckedChange = onKeepSignedInChange,
                enabled = !state.isLoading
            )

            state.generalError?.let {
                Spacer(modifier = Modifier.height(spacing.xs))
                AuthErrorBanner(message = stringResource(it))
            }

            Spacer(modifier = Modifier.height(spacing.sm))
            PrimaryButton(
                text = stringResource(R.string.login_submit),
                onClick = {
                    focusManager.clearFocus()
                    onSignIn()
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = if (state.isLoading) {
                    {
                        CircularProgressIndicator(
                            color = scheme.onPrimaryContainer,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    }
                } else null
            )
            TextActionButton(
                text = stringResource(R.string.login_forgot),
                onClick = onForgotPassword,
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(spacing.md))
            HorizontalDivider(color = scheme.outlineVariant)
            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = stringResource(R.string.login_new_here),
                style = FruitLogixTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            SecondaryButton(
                text = stringResource(R.string.login_create_account),
                onClick = onCreateAccount,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = stringResource(R.string.iam_version, BuildConfig.VERSION_NAME),
                style = FruitLogixTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LoginPreview() {
    FruitLogixTheme {
        LoginContent(
            state = LoginUiState(username = "rosa.quispe", generalError = R.string.login_error_credentials),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onKeepSignedInChange = {},
            onSignIn = {},
            onForgotPassword = {},
            onCreateAccount = {},
            showLanguageSwitch = false
        )
    }
}
