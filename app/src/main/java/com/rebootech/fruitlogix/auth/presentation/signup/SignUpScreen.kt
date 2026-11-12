package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.presentation.components.AuthCheckRow
import com.rebootech.fruitlogix.auth.presentation.components.AuthErrorBanner
import com.rebootech.fruitlogix.auth.presentation.components.AuthLanguageSwitch
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.auth.presentation.components.PasswordRules
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Create account in three short steps. Saving the session at the end makes RootNavHost
 * open the shell of the chosen role.
 */
@Composable
fun SignUpScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val goBack = { if (!viewModel.back()) onNavigateBackToLogin() }

    BackHandler(onBack = goBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 480.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = goBack, modifier = Modifier.size(FruitLogixTheme.sizing.minTouch)) {
                    AppIcon(
                        id = R.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.common_back),
                        tint = scheme.onSurface,
                        modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                AuthLanguageSwitch()
            }

            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = stringResource(R.string.signup_step, state.step.number, SignUpUiState.TOTAL_STEPS),
                style = FruitLogixTheme.typography.labelLarge,
                color = scheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            LimeProgressBar(
                progress = state.step.number / SignUpUiState.TOTAL_STEPS.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.md))

            when (state.step) {
                SignUpStep.ROLE -> RoleStep(
                    selected = state.role,
                    onSelect = viewModel::selectRole,
                    onSignIn = onNavigateBackToLogin
                )
                SignUpStep.ABOUT -> AboutStep(state, viewModel)
                SignUpStep.ACCESS -> AccessStep(state, viewModel)
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    val scheme = MaterialTheme.colorScheme
    Text(title, style = FruitLogixTheme.typography.headlineSmall, color = scheme.onSurface)
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
    Text(subtitle, style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.md))
}

// Step 1: role -------------------------------------------------------------

@Composable
private fun ColumnScope.RoleStep(
    selected: UserRole?,
    onSelect: (UserRole) -> Unit,
    onSignIn: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    StepTitle(stringResource(R.string.signup_role_title), stringResource(R.string.signup_role_subtitle))
    RoleCard(
        iconRes = R.drawable.ic_leaf,
        title = stringResource(R.string.signup_role_producer),
        description = stringResource(R.string.signup_role_producer_desc),
        selected = selected == UserRole.PRODUCER,
        onClick = { onSelect(UserRole.PRODUCER) }
    )
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
    RoleCard(
        iconRes = R.drawable.ic_dispatch_truck,
        title = stringResource(R.string.signup_role_distributor),
        description = stringResource(R.string.signup_role_distributor_desc),
        selected = selected == UserRole.DISTRIBUTOR,
        onClick = { onSelect(UserRole.DISTRIBUTOR) }
    )

    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.lg))
    Text(
        text = stringResource(R.string.signup_have_account),
        style = FruitLogixTheme.typography.bodyLarge,
        color = scheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    TextActionButton(
        text = stringResource(R.string.signup_sign_in),
        onClick = onSignIn,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@Composable
private fun RoleCard(
    iconRes: Int,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(FruitLogixTheme.shapes.Card)
            .background(if (selected) scheme.secondaryContainer else scheme.surfaceContainerLow)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = FruitLogixTheme.shapes.Card
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.minTouch)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(scheme.primary),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = iconRes, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        AppIcon(id = R.drawable.ic_chevron_right, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
    }
}

// Step 2: about you --------------------------------------------------------

@Composable
private fun AboutStep(state: SignUpUiState, viewModel: SignUpViewModel) {
    val spacing = FruitLogixTheme.spacing
    val focusManager = LocalFocusManager.current
    val isProducer = state.role == UserRole.PRODUCER

    RoleChip(state.role)
    StepTitle(stringResource(R.string.signup_about_title), stringResource(R.string.signup_about_subtitle))

    LabeledTextField(
        label = stringResource(R.string.signup_full_name),
        value = state.fullName,
        onValueChange = viewModel::onFullNameChange,
        placeholder = stringResource(R.string.signup_full_name_placeholder),
        leadingIconRes = R.drawable.ic_person,
        error = state.fullNameError?.let { stringResource(it) },
        enabled = !state.isLoading,
        capitalization = KeyboardCapitalization.Words,
        autofill = ContentType.PersonFullName
    )
    Spacer(modifier = Modifier.height(spacing.sm))
    LabeledTextField(
        label = stringResource(if (isProducer) R.string.signup_farm_name else R.string.signup_company_name),
        value = state.organizationName,
        onValueChange = viewModel::onOrganizationChange,
        placeholder = stringResource(if (isProducer) R.string.signup_farm_name_placeholder else R.string.signup_company_name_placeholder),
        leadingIconRes = if (isProducer) R.drawable.ic_leaf else R.drawable.ic_warehouse,
        error = state.organizationError?.let { stringResource(it) },
        enabled = !state.isLoading,
        capitalization = KeyboardCapitalization.Words
    )
    Spacer(modifier = Modifier.height(spacing.sm))
    LabeledTextField(
        label = stringResource(R.string.signup_email),
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        placeholder = stringResource(R.string.signup_email_placeholder),
        leadingIconRes = R.drawable.ic_send,
        error = state.emailError?.let { stringResource(it) },
        enabled = !state.isLoading,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
        onImeAction = {
            focusManager.clearFocus()
            viewModel.continueFromAbout()
        },
        autofill = ContentType.EmailAddress
    )

    Spacer(modifier = Modifier.height(spacing.md))
    PrimaryButton(
        text = stringResource(R.string.signup_next),
        onClick = {
            focusManager.clearFocus()
            viewModel.continueFromAbout()
        },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = loadingIcon(state.isLoading)
    )
}

@Composable
private fun RoleChip(role: UserRole?) {
    val scheme = MaterialTheme.colorScheme
    val roleName = stringResource(if (role == UserRole.DISTRIBUTOR) R.string.signup_role_distributor else R.string.signup_role_producer)
    Text(
        text = stringResource(R.string.signup_role_chip, roleName),
        style = FruitLogixTheme.typography.labelLarge,
        color = scheme.onSecondaryContainer,
        modifier = Modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(scheme.secondaryContainer)
            .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = 6.dp)
    )
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
}

// Step 3: access -----------------------------------------------------------

@Composable
private fun ColumnScope.AccessStep(state: SignUpUiState, viewModel: SignUpViewModel) {
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val focusManager = LocalFocusManager.current

    RoleChip(state.role)
    StepTitle(stringResource(R.string.signup_account_title), stringResource(R.string.signup_account_subtitle))

    LabeledTextField(
        label = stringResource(R.string.signup_username),
        value = state.username,
        onValueChange = viewModel::onUsernameChange,
        leadingIconRes = R.drawable.ic_person,
        error = state.usernameError?.let { stringResource(it) },
        helper = stringResource(R.string.signup_username_help),
        enabled = !state.isLoading,
        keyboardType = KeyboardType.Ascii,
        autofill = ContentType.NewUsername
    )
    Spacer(modifier = Modifier.height(spacing.sm))
    LabeledTextField(
        label = stringResource(R.string.signup_password),
        value = state.password,
        onValueChange = viewModel::onPasswordChange,
        leadingIconRes = R.drawable.ic_fleet_lock,
        error = state.passwordError?.let { stringResource(it) },
        enabled = !state.isLoading,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
        onImeAction = { focusManager.clearFocus() },
        autofill = ContentType.NewPassword,
        isPassword = true,
        passwordVisible = state.isPasswordVisible,
        onTogglePasswordVisible = viewModel::onTogglePasswordVisibility
    )
    PasswordRules(
        rules = listOf(
            stringResource(R.string.signup_req_length) to state.hasMinLength,
            stringResource(R.string.signup_req_letter) to state.hasLetter,
            stringResource(R.string.signup_req_number) to state.hasNumber
        )
    )

    if (state.role == UserRole.PRODUCER) {
        Spacer(modifier = Modifier.height(spacing.sm))
        if (state.showInvitationCode) {
            LabeledTextField(
                label = stringResource(R.string.signup_invitation_label),
                value = state.invitationCode,
                onValueChange = viewModel::onInvitationCodeChange,
                helper = stringResource(R.string.signup_invitation_help),
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )
        } else {
            TextActionButton(
                text = stringResource(R.string.signup_invitation_toggle),
                onClick = viewModel::onShowInvitationCode,
                enabled = !state.isLoading
            )
        }
    }

    Spacer(modifier = Modifier.height(spacing.sm))
    AuthCheckRow(
        label = stringResource(R.string.signup_terms),
        checked = state.termsAccepted,
        onCheckedChange = viewModel::onTermsChange,
        enabled = !state.isLoading,
        isError = state.termsError != null
    )
    state.termsError?.let {
        Text(stringResource(it), style = FruitLogixTheme.typography.bodySmall, color = scheme.error)
    }

    state.generalError?.let {
        Spacer(modifier = Modifier.height(spacing.xs))
        AuthErrorBanner(message = stringResource(it))
    }

    Spacer(modifier = Modifier.height(spacing.md))
    PrimaryButton(
        text = stringResource(R.string.signup_submit),
        onClick = {
            focusManager.clearFocus()
            viewModel.submit()
        },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = loadingIcon(state.isLoading)
    )
    Spacer(modifier = Modifier.height(spacing.sm))
}

private fun loadingIcon(isLoading: Boolean): (@Composable () -> Unit)? =
    if (isLoading) {
        {
            CircularProgressIndicator(
                color = androidx.compose.material3.LocalContentColor.current,
                strokeWidth = 3.dp,
                modifier = Modifier.size(20.dp)
            )
        }
    } else null
