package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.LanguageTogglePill
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun SignUpScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sessionRepo = remember { DataStoreSessionRepository.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarDismissed()
        }
    }

    BackHandler {
        if (uiState.step == 2) {
            viewModel.onPreviousStep()
        } else {
            onNavigateBackToLogin()
        }
    }

    SignUpScreenContent(
        uiState = uiState,
        onBackClick = {
            if (uiState.step == 2) {
                viewModel.onPreviousStep()
            } else {
                onNavigateBackToLogin()
            }
        },
        onLanguageSelected = viewModel::onLanguageSelected,
        onSelectRole = viewModel::onSelectRole,
        onContinueToStep2 = viewModel::onNextStep,
        onFullNameChange = viewModel::onFullNameChange,
        onFullNameBlur = viewModel::onFullNameBlur,
        onUsernameChange = viewModel::onUsernameChange,
        onUsernameBlur = viewModel::onUsernameBlur,
        onEmailChange = viewModel::onEmailChange,
        onEmailBlur = viewModel::onEmailBlur,
        onPhoneChange = viewModel::onPhoneChange,
        onPhoneBlur = viewModel::onPhoneBlur,
        onOrganizationNameChange = viewModel::onOrganizationNameChange,
        onOrganizationNameBlur = viewModel::onOrganizationNameBlur,
        onInvitationCodeChange = viewModel::onInvitationCodeChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
        onSubmitSignUp = { viewModel.onSubmitSignUp(sessionRepo) },
        onNavigateBackToLogin = onNavigateBackToLogin,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun SignUpScreenContent(
    uiState: SignUpUiState,
    onBackClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onContinueToStep2: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onFullNameBlur: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onUsernameBlur: () -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhoneBlur: () -> Unit,
    onOrganizationNameChange: (String) -> Unit,
    onOrganizationNameBlur: () -> Unit,
    onInvitationCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSubmitSignUp: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.surfaceDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal Header: Back Arrow, Brand Logo, Language Toggle (No title, no avatar)
            SignUpTopBar(
                onBackClick = onBackClick,
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = onLanguageSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stepper Header
            StepperHeader(step = uiState.step)

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.step == 1) {
                Step1RoleSelection(
                    selectedRole = uiState.selectedRole,
                    onSelectRole = onSelectRole,
                    onContinue = onContinueToStep2,
                    onNavigateBackToLogin = onNavigateBackToLogin
                )
            } else {
                Step2FormContent(
                    uiState = uiState,
                    onFullNameChange = onFullNameChange,
                    onFullNameBlur = onFullNameBlur,
                    onUsernameChange = onUsernameChange,
                    onUsernameBlur = onUsernameBlur,
                    onEmailChange = onEmailChange,
                    onEmailBlur = onEmailBlur,
                    onPhoneChange = onPhoneChange,
                    onPhoneBlur = onPhoneBlur,
                    onOrganizationNameChange = onOrganizationNameChange,
                    onOrganizationNameBlur = onOrganizationNameBlur,
                    onInvitationCodeChange = onInvitationCodeChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                    onTermsAcceptedChange = onTermsAcceptedChange,
                    onSubmitSignUp = onSubmitSignUp,
                    onNavigateBackToLogin = onNavigateBackToLogin
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SignUpTopBar(
    onBackClick: () -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.auth_signup_back_arrow_desc),
                tint = FruitLogixTheme.colors.textOnDark,
                modifier = Modifier.size(24.dp)
            )
        }

        // Small brand icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.8f))
                .border(
                    width = 1.dp,
                    color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fruitlogix_brand),
                contentDescription = stringResource(R.string.auth_brand_logo_desc),
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }

        LanguageTogglePill(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected
        )
    }
}

@Composable
private fun StepperHeader(
    step: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction by animateFloatAsState(
        targetValue = if (step == 1) 0.5f else 1.0f,
        label = "stepper_progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.primary)
                )
                Text(
                    text = stringResource(
                        if (step == 1) R.string.auth_signup_step1_header else R.string.auth_signup_step2_header
                    ),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = FruitLogixTheme.colors.textOnDark,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = stringResource(
                    if (step == 1) R.string.auth_signup_step1_progress else R.string.auth_signup_step2_progress
                ),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = FruitLogixTheme.colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressFraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.primary)
            )
        }
    }
}

@Composable
private fun Step1RoleSelection(
    selectedRole: UserRole?,
    onSelectRole: (UserRole) -> Unit,
    onContinue: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.auth_signup_step1_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = FruitLogixTheme.colors.textOnDark,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auth_signup_step1_subtitle),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = FruitLogixTheme.colors.textMuted,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Card: Productor
        RoleSelectionCard(
            title = stringResource(R.string.auth_signup_role_producer),
            tag = stringResource(R.string.auth_signup_role_producer_tag),
            description = stringResource(R.string.auth_signup_role_producer_desc),
            iconRes = R.drawable.ic_leaf,
            isSelected = selectedRole == UserRole.PRODUCER,
            onClick = { onSelectRole(UserRole.PRODUCER) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Role Card: Distribuidor
        RoleSelectionCard(
            title = stringResource(R.string.auth_signup_role_distributor),
            tag = stringResource(R.string.auth_signup_role_distributor_tag),
            description = stringResource(R.string.auth_signup_role_distributor_desc),
            iconRes = R.drawable.ic_fleet,
            isSelected = selectedRole == UserRole.DISTRIBUTOR,
            onClick = { onSelectRole(UserRole.DISTRIBUTOR) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Notice Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = null,
                tint = FruitLogixTheme.colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.auth_signup_notice),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = FruitLogixTheme.colors.textMuted,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Continue Button
        PrimaryButton(
            text = stringResource(R.string.auth_signup_btn_continue),
            onClick = onContinue,
            enabled = selectedRole != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Footer: ¿Ya tienes cuenta? Iniciar sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_signup_have_account_prompt),
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = FruitLogixTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.auth_signup_login_link),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = FruitLogixTheme.colors.primary,
                modifier = Modifier.clickable(onClick = onNavigateBackToLogin)
            )
        }
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    tag: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
        label = "role_card_border"
    )
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.6f))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = FruitLogixTheme.colors.textOnDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FruitLogixTheme.colors.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = FruitLogixTheme.colors.primary
                        )
                    }
                }

                // Check indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) FruitLogixTheme.colors.primary else Color.Transparent)
                        .border(
                            width = if (isSelected) 0.dp else 2.dp,
                            color = if (isSelected) Color.Transparent else FruitLogixTheme.colors.textMuted.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.surfaceDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = FruitLogixTheme.colors.textMuted,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun Step2FormContent(
    uiState: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onFullNameBlur: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onUsernameBlur: () -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhoneBlur: () -> Unit,
    onOrganizationNameChange: (String) -> Unit,
    onOrganizationNameBlur: () -> Unit,
    onInvitationCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSubmitSignUp: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val role = uiState.selectedRole ?: UserRole.PRODUCER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Role pill indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(FruitLogixTheme.colors.primary.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        id = if (role == UserRole.PRODUCER) R.drawable.ic_leaf else R.drawable.ic_fleet
                    ),
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        if (role == UserRole.PRODUCER) R.string.auth_signup_badge_producer else R.string.auth_signup_badge_distributor
                    ),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = FruitLogixTheme.colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.auth_signup_step2_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = FruitLogixTheme.colors.textOnDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main form card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Full Name
                FieldLabel(label = stringResource(R.string.auth_signup_label_fullname))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = onFullNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onFullNameBlur() },
                    placeholder = {
                        Text(
                            text = stringResource(
                                if (role == UserRole.PRODUCER) R.string.auth_signup_placeholder_fullname_producer
                                else R.string.auth_signup_placeholder_fullname_distributor
                            ),
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_id),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (AuthValidators.isValidName(uiState.fullName)) {
                            ValidCheckmarkIcon()
                        }
                    },
                    singleLine = true,
                    isError = uiState.fullNameErrorRes != null,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                FieldErrorText(errorRes = uiState.fullNameErrorRes)

                Spacer(modifier = Modifier.height(14.dp))

                // Username
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldLabel(label = stringResource(R.string.auth_signup_label_username))
                    if (uiState.isUsernameTaken) {
                        Text(
                            text = stringResource(R.string.auth_signup_username_taken_badge),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FruitLogixTheme.colors.danger
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onUsernameBlur() },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.auth_signup_placeholder_username),
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Text(
                            text = "@",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FruitLogixTheme.colors.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.isUsernameTaken) {
                            ErrorCrossIcon()
                        } else if (AuthValidators.isValidUsername(uiState.username)) {
                            ValidCheckmarkIcon()
                        }
                    },
                    singleLine = true,
                    isError = uiState.usernameErrorRes != null || uiState.isUsernameTaken,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                FieldErrorText(errorRes = uiState.usernameErrorRes)

                Spacer(modifier = Modifier.height(14.dp))

                // Email
                FieldLabel(label = stringResource(R.string.auth_signup_label_email))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onEmailBlur() },
                    placeholder = {
                        Text(
                            text = stringResource(
                                if (role == UserRole.PRODUCER) R.string.auth_signup_placeholder_email_producer
                                else R.string.auth_signup_placeholder_email_distributor
                            ),
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.isEmailTaken) {
                            ErrorCrossIcon()
                        } else if (AuthValidators.isValidEmail(uiState.email)) {
                            ValidCheckmarkIcon()
                        }
                    },
                    singleLine = true,
                    isError = uiState.emailErrorRes != null || uiState.isEmailTaken,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                FieldErrorText(errorRes = uiState.emailErrorRes)

                Spacer(modifier = Modifier.height(14.dp))

                // Phone (Optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldLabel(label = stringResource(R.string.auth_signup_label_phone))
                    Text(
                        text = stringResource(R.string.auth_signup_optional_badge),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPhoneBlur() },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.auth_signup_placeholder_phone),
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact_driver),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.phone.isNotEmpty() && AuthValidators.isValidPhone(uiState.phone)) {
                            ValidCheckmarkIcon()
                        }
                    },
                    singleLine = true,
                    isError = uiState.phoneErrorRes != null,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                FieldErrorText(errorRes = uiState.phoneErrorRes)

                Spacer(modifier = Modifier.height(14.dp))

                // Role-Specific Fields
                if (role == UserRole.PRODUCER) {
                    // Farm name
                    FieldLabel(label = stringResource(R.string.auth_signup_label_farm_name))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.organizationName,
                        onValueChange = onOrganizationNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) onOrganizationNameBlur() },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.auth_signup_placeholder_farm_name),
                                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_leaf),
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (AuthValidators.isValidName(uiState.organizationName)) {
                                ValidCheckmarkIcon()
                            }
                        },
                        singleLine = true,
                        isError = uiState.organizationErrorRes != null,
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    FieldErrorText(errorRes = uiState.organizationErrorRes)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Invitation Code (Optional)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FieldLabel(label = stringResource(R.string.auth_signup_label_invitation_code))
                        Text(
                            text = stringResource(R.string.auth_signup_optional_badge),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.invitationCode,
                        onValueChange = onInvitationCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.auth_signup_placeholder_invitation_code),
                                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_limits),
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.auth_signup_help_invitation_code),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FruitLogixTheme.colors.textMuted,
                        lineHeight = 15.sp
                    )
                } else {
                    // Company name (Distributor)
                    FieldLabel(label = stringResource(R.string.auth_signup_label_company_name))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.organizationName,
                        onValueChange = onOrganizationNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) onOrganizationNameBlur() },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.auth_signup_placeholder_company_name),
                                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_warehouse),
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (AuthValidators.isValidName(uiState.organizationName)) {
                                ValidCheckmarkIcon()
                            }
                        },
                        singleLine = true,
                        isError = uiState.organizationErrorRes != null,
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.auth_signup_help_company_name),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FruitLogixTheme.colors.textMuted,
                        lineHeight = 15.sp
                    )
                    FieldErrorText(errorRes = uiState.organizationErrorRes)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldLabel(label = stringResource(R.string.auth_signup_label_password))
                    if (uiState.password.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                if (uiState.isPasswordValid) R.string.auth_signup_password_secure
                                else R.string.auth_signup_password_insecure
                            ),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (uiState.isPasswordValid) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.danger
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fleet_lock),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (uiState.isPasswordVisible) R.drawable.ic_diagnostic else R.drawable.ic_radar
                                ),
                                contentDescription = stringResource(
                                    if (uiState.isPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password
                                ),
                                tint = FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password Checklist Box
                PasswordRequirementsBox(
                    isLengthValid = uiState.isPasswordLengthValid,
                    hasLetter = uiState.hasPasswordLetter,
                    hasNumber = uiState.hasPasswordNumber
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldLabel(label = stringResource(R.string.auth_signup_label_confirm_password))
                    if (uiState.confirmPassword.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                if (uiState.doPasswordsMatch) R.string.auth_signup_password_match
                                else R.string.auth_signup_password_mismatch
                            ),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (uiState.doPasswordsMatch) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.danger
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fleet_lock),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.confirmPassword.isNotEmpty()) {
                            if (uiState.doPasswordsMatch) {
                                ValidCheckmarkIcon()
                            } else {
                                ErrorCrossIcon()
                            }
                        }
                    },
                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.confirmPasswordErrorRes != null,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (uiState.isStep2Valid) {
                                onSubmitSignUp()
                            }
                        }
                    )
                )
                FieldErrorText(errorRes = uiState.confirmPasswordErrorRes)

                Spacer(modifier = Modifier.height(14.dp))

                // Terms and Conditions Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTermsAcceptedChange(!uiState.termsAccepted) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = onTermsAcceptedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = FruitLogixTheme.colors.primary,
                            checkmarkColor = FruitLogixTheme.colors.surfaceDark,
                            uncheckedColor = FruitLogixTheme.colors.textMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.auth_signup_terms_checkbox),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FruitLogixTheme.colors.textOnDark,
                        lineHeight = 16.sp
                    )
                }

                if (uiState.generalErrorRes != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GeneralErrorBanner(messageRes = uiState.generalErrorRes)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                PrimaryButton(
                    text = stringResource(R.string.auth_signup_btn_submit),
                    onClick = {
                        focusManager.clearFocus()
                        onSubmitSignUp()
                    },
                    enabled = uiState.isStep2Valid && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer: ¿Ya tienes cuenta? Iniciar sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_signup_have_account_prompt),
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = FruitLogixTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.auth_signup_login_link),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = FruitLogixTheme.colors.primary,
                modifier = Modifier.clickable(onClick = onNavigateBackToLogin)
            )
        }
    }
}

@Composable
private fun PasswordRequirementsBox(
    isLengthValid: Boolean,
    hasLetter: Boolean,
    hasNumber: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FruitLogixTheme.colors.surfaceDark)
            .border(
                width = 1.dp,
                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(R.string.auth_signup_password_req_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = FruitLogixTheme.colors.textMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        ChecklistItem(
            text = stringResource(R.string.auth_signup_req_min_chars),
            isMet = isLengthValid
        )
        Spacer(modifier = Modifier.height(4.dp))
        ChecklistItem(
            text = stringResource(R.string.auth_signup_req_one_letter),
            isMet = hasLetter
        )
        Spacer(modifier = Modifier.height(4.dp))
        ChecklistItem(
            text = stringResource(R.string.auth_signup_req_one_number),
            isMet = hasNumber
        )
    }
}

@Composable
private fun ChecklistItem(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(
                id = if (isMet) R.drawable.ic_check else R.drawable.ic_close
            ),
            contentDescription = null,
            tint = if (isMet) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.danger,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = if (isMet) FruitLogixTheme.colors.textOnDark else FruitLogixTheme.colors.danger
        )
    }
}

@Composable
private fun GeneralErrorBanner(@androidx.annotation.StringRes messageRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FruitLogixTheme.colors.danger.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = FruitLogixTheme.colors.danger.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_alert),
            contentDescription = null,
            tint = FruitLogixTheme.colors.danger,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(messageRes),
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = FruitLogixTheme.colors.danger,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun FieldLabel(label: String) {
    Text(
        text = label,
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = FruitLogixTheme.colors.textMuted,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun FieldErrorText(@androidx.annotation.StringRes errorRes: Int?) {
    if (errorRes != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(errorRes),
            fontFamily = PoppinsFontFamily,
            fontSize = 11.sp,
            color = FruitLogixTheme.colors.danger,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun ValidCheckmarkIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_check),
        contentDescription = null,
        tint = FruitLogixTheme.colors.primary,
        modifier = Modifier
            .padding(end = 12.dp)
            .size(18.dp)
    )
}

@Composable
private fun ErrorCrossIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_close),
        contentDescription = null,
        tint = FruitLogixTheme.colors.danger,
        modifier = Modifier
            .padding(end = 12.dp)
            .size(18.dp)
    )
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FruitLogixTheme.colors.primary,
    unfocusedBorderColor = FruitLogixTheme.colors.textMuted.copy(alpha = 0.25f),
    errorBorderColor = FruitLogixTheme.colors.danger,
    focusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.8f),
    unfocusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.5f),
    errorContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.8f),
    focusedTextColor = FruitLogixTheme.colors.textOnDark,
    unfocusedTextColor = FruitLogixTheme.colors.textOnDark,
    errorTextColor = FruitLogixTheme.colors.textOnDark,
    cursorColor = FruitLogixTheme.colors.primary
)

// PREVIEWS according to section 16.8

@Preview(name = "Sign Up - Step 1: Role Selection", showBackground = true, backgroundColor = 0xFF0D140E)
@Composable
fun SignUpStep1Preview() {
    FruitLogixTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(
                step = 1,
                selectedRole = null
            ),
            onBackClick = {},
            onLanguageSelected = {},
            onSelectRole = {},
            onContinueToStep2 = {},
            onFullNameChange = {},
            onFullNameBlur = {},
            onUsernameChange = {},
            onUsernameBlur = {},
            onEmailChange = {},
            onEmailBlur = {},
            onPhoneChange = {},
            onPhoneBlur = {},
            onOrganizationNameChange = {},
            onOrganizationNameBlur = {},
            onInvitationCodeChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onTermsAcceptedChange = {},
            onSubmitSignUp = {},
            onNavigateBackToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Sign Up - Step 2: Producer", showBackground = true, backgroundColor = 0xFF0D140E)
@Composable
fun SignUpStep2ProducerPreview() {
    FruitLogixTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(
                step = 2,
                selectedRole = UserRole.PRODUCER,
                fullName = "Roberto Garza",
                username = "roberto.agricola",
                email = "roberto@fundo.pe",
                phone = "+51 987 654 321",
                organizationName = "Fundo Santa Rosa",
                invitationCode = "DIST-LIMA-101",
                password = "Password123",
                confirmPassword = "Password123",
                termsAccepted = true
            ),
            onBackClick = {},
            onLanguageSelected = {},
            onSelectRole = {},
            onContinueToStep2 = {},
            onFullNameChange = {},
            onFullNameBlur = {},
            onUsernameChange = {},
            onUsernameBlur = {},
            onEmailChange = {},
            onEmailBlur = {},
            onPhoneChange = {},
            onPhoneBlur = {},
            onOrganizationNameChange = {},
            onOrganizationNameBlur = {},
            onInvitationCodeChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onTermsAcceptedChange = {},
            onSubmitSignUp = {},
            onNavigateBackToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Sign Up - Step 2: Distributor", showBackground = true, backgroundColor = 0xFF0D140E)
@Composable
fun SignUpStep2DistributorPreview() {
    FruitLogixTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(
                step = 2,
                selectedRole = UserRole.DISTRIBUTOR,
                fullName = "Carlos Mendoza",
                username = "carlos.mendoza",
                email = "carlos@fruitlogix.pe",
                phone = "+51 955 876 543",
                organizationName = "FruitLogix Distributor",
                password = "Distributor123",
                confirmPassword = "Distributor123",
                termsAccepted = true
            ),
            onBackClick = {},
            onLanguageSelected = {},
            onSelectRole = {},
            onContinueToStep2 = {},
            onFullNameChange = {},
            onFullNameBlur = {},
            onUsernameChange = {},
            onUsernameBlur = {},
            onEmailChange = {},
            onEmailBlur = {},
            onPhoneChange = {},
            onPhoneBlur = {},
            onOrganizationNameChange = {},
            onOrganizationNameBlur = {},
            onInvitationCodeChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onTermsAcceptedChange = {},
            onSubmitSignUp = {},
            onNavigateBackToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Sign Up - Step 2: Validation Errors", showBackground = true, backgroundColor = 0xFF0D140E)
@Composable
fun SignUpStep2ErrorsPreview() {
    FruitLogixTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(
                step = 2,
                selectedRole = UserRole.PRODUCER,
                fullName = "Carlos Martínez",
                username = "carlos.mendoza",
                isUsernameTaken = true,
                usernameErrorRes = R.string.auth_signup_error_username_taken,
                email = "carlos@fundo.pe",
                organizationName = "Fundo Santa Rosa",
                password = "test",
                confirmPassword = "different",
                confirmPasswordErrorRes = R.string.auth_signup_error_passwords_dont_match,
                termsAccepted = false
            ),
            onBackClick = {},
            onLanguageSelected = {},
            onSelectRole = {},
            onContinueToStep2 = {},
            onFullNameChange = {},
            onFullNameBlur = {},
            onUsernameChange = {},
            onUsernameBlur = {},
            onEmailChange = {},
            onEmailBlur = {},
            onPhoneChange = {},
            onPhoneBlur = {},
            onOrganizationNameChange = {},
            onOrganizationNameBlur = {},
            onInvitationCodeChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onTermsAcceptedChange = {},
            onSubmitSignUp = {},
            onNavigateBackToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
