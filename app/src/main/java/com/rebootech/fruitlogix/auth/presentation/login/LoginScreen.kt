package com.rebootech.fruitlogix.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.LanguageTogglePill
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
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

    LoginScreenContent(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onRememberDeviceChange = viewModel::onRememberDeviceChange,
        onLanguageSelected = viewModel::onLanguageSelected,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onBiometricClick = viewModel::onBiometricClick,
        onLoginClick = { viewModel.onLoginClick(sessionRepo) },
        onNavigateToSignUp = onNavigateToSignUp,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRememberDeviceChange: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.surfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Fila superior: Chip de versión a la izquierda, LanguageTogglePill a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip versión
                Box(
                    modifier = Modifier
                        .clip(FruitLogixTheme.shapes.Pill)
                        .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FruitLogixTheme.colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.auth_version_badge, uiState.appVersionName),
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Selector de idioma ES | EN
                LanguageTogglePill(
                    selectedLanguage = uiState.selectedLanguage,
                    onLanguageSelected = onLanguageSelected
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Marca: Logo, Chip "LOGÍSTICA AGROINDUSTRIAL", "FruitLogix", Línea descriptiva
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        FruitLogixTheme.colors.textMuted.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_fruitlogix_brand,
                    contentDescription = stringResource(R.string.auth_brand_logo_desc),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chip "LOGÍSTICA AGROINDUSTRIAL"
            Box(
                modifier = Modifier
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(FruitLogixTheme.colors.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_leaf,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.auth_agro_logistics_badge),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título de la marca: "Fruit" en blanco + "Logix" en lima
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Fruit",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Text(
                    text = "Logix",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = FruitLogixTheme.colors.primary
                )
            }

            Text(
                text = stringResource(R.string.auth_tagline),
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textMuted,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Tarjeta de Login (ColorAppbar con transparencia, borde 1dp textMuted 20%, 24dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.75f))
                    .border(
                        1.dp,
                        FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Cabecera de la tarjeta: Título + candado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.auth_login_title),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = FruitLogixTheme.typography.titleLarge,
                                color = FruitLogixTheme.colors.textOnDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.auth_login_card_subtitle),
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.textMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(
                                id = R.drawable.ic_fleet_lock,
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo USUARIO
                    Text(
                        text = stringResource(R.string.auth_label_username),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = if (uiState.usernameError != null) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = onUsernameChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.auth_placeholder_username),
                                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.5f),
                                style = FruitLogixTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            AppIcon(
                                id = R.drawable.ic_person,
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = uiState.usernameError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.6f),
                            unfocusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.6f),
                            disabledContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.3f),
                            focusedTextColor = FruitLogixTheme.colors.textOnDark,
                            unfocusedTextColor = FruitLogixTheme.colors.textOnDark,
                            cursorColor = FruitLogixTheme.colors.primary,
                            focusedBorderColor = FruitLogixTheme.colors.primary,
                            unfocusedBorderColor = FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
                            errorBorderColor = FruitLogixTheme.colors.dangerStrong
                        )
                    )

                    if (uiState.usernameError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.usernameError,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.dangerStrong
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo CONTRASEÑA con cabecera y "¿Olvidaste tu contraseña?"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.hasCredentialsError) {
                                AppIcon(
                                    id = R.drawable.ic_alert,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.dangerStrong,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = stringResource(R.string.auth_label_password),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = if (uiState.hasCredentialsError || uiState.passwordError != null) {
                                    FruitLogixTheme.colors.dangerStrong
                                } else {
                                    FruitLogixTheme.colors.textMuted
                                },
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = stringResource(R.string.auth_forgot_password),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(onClick = onForgotPasswordClick)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "••••••••",
                                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.5f),
                                style = FruitLogixTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            AppIcon(
                                id = R.drawable.ic_fleet_lock,
                                contentDescription = null,
                                tint = if (uiState.hasCredentialsError) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.size(20.dp)
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
                        isError = uiState.hasCredentialsError || uiState.passwordError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginClick()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.6f),
                            unfocusedContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.6f),
                            disabledContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.3f),
                            focusedTextColor = FruitLogixTheme.colors.textOnDark,
                            unfocusedTextColor = FruitLogixTheme.colors.textOnDark,
                            cursorColor = FruitLogixTheme.colors.primary,
                            focusedBorderColor = if (uiState.hasCredentialsError) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                            unfocusedBorderColor = if (uiState.hasCredentialsError) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted.copy(alpha = 0.2f),
                            errorBorderColor = FruitLogixTheme.colors.dangerStrong
                        )
                    )

                    if (uiState.passwordError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.passwordError,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.dangerStrong
                        )
                    }

                    // Caja de error general / credenciales inválidas / sin conexión
                    if (uiState.generalError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.14f))
                                .border(
                                    1.dp,
                                    FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(
                                    id = R.drawable.ic_warning,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.dangerStrong,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.generalError,
                                    style = FruitLogixTheme.typography.bodySmall,
                                    color = FruitLogixTheme.colors.dangerStrong,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Casilla "Recordar dispositivo" y botón "Huella / Rostro"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(enabled = !uiState.isLoading) {
                                    onRememberDeviceChange(!uiState.rememberDevice)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = uiState.rememberDevice,
                                onCheckedChange = onRememberDeviceChange,
                                enabled = !uiState.isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = FruitLogixTheme.colors.primary,
                                    uncheckedColor = FruitLogixTheme.colors.textMuted,
                                    checkmarkColor = FruitLogixTheme.colors.onPrimary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auth_remember_device),
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.textOnDark
                            )
                        }

                        // Botón de biometría (oculto por defecto según 16.4)
                        if (uiState.isBiometricsAvailable) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(onClick = onBiometricClick)
                                    .padding(vertical = 4.dp)
                            ) {
                                AppIcon(
                                    id = R.drawable.ic_shield_check,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.auth_biometric_button),
                                    style = FruitLogixTheme.typography.bodySmall,
                                    color = FruitLogixTheme.colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón lima "Iniciar sesión ->" de 56dp
                    PrimaryButton(
                        text = stringResource(R.string.auth_btn_login),
                        onClick = onLoginClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        leadingIcon = if (uiState.isLoading) {
                            {
                                CircularProgressIndicator(
                                    color = FruitLogixTheme.colors.onPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else null,
                        trailingIcon = if (!uiState.isLoading) {
                            {
                                AppIcon(
                                    id = R.drawable.ic_arrow_forward,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. Pie: "¿No tienes cuenta? Crear cuenta"
            Row(
                modifier = Modifier
                    .clickable(onClick = onNavigateToSignUp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.auth_no_account_prompt),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )
                Text(
                    text = stringResource(R.string.auth_create_account_link),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Login - Inactivo")
@Composable
private fun LoginPreviewInactive() {
    FruitLogixTheme {
        LoginScreenContent(
            uiState = LoginUiState(username = "", password = ""),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberDeviceChange = {},
            onLanguageSelected = {},
            onForgotPasswordClick = {},
            onBiometricClick = {},
            onLoginClick = {},
            onNavigateToSignUp = {}
        )
    }
}

@Preview(name = "Login - Cargando")
@Composable
private fun LoginPreviewLoading() {
    FruitLogixTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                username = "carlos.mendoza",
                password = "password",
                isLoading = true
            ),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberDeviceChange = {},
            onLanguageSelected = {},
            onForgotPasswordClick = {},
            onBiometricClick = {},
            onLoginClick = {},
            onNavigateToSignUp = {}
        )
    }
}

@Preview(name = "Login - Error de Credenciales")
@Composable
private fun LoginPreviewInvalidCredentials() {
    FruitLogixTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                username = "carlos.mendoza",
                password = "",
                hasCredentialsError = true,
                generalError = "Usuario o contraseña incorrectos. Verifica tus datos."
            ),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberDeviceChange = {},
            onLanguageSelected = {},
            onForgotPasswordClick = {},
            onBiometricClick = {},
            onLoginClick = {},
            onNavigateToSignUp = {}
        )
    }
}

@Preview(name = "Login - Campos Vacíos")
@Composable
private fun LoginPreviewEmptyFields() {
    FruitLogixTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                username = "",
                password = "",
                usernameError = "Ingresa tu usuario",
                passwordError = "Ingresa tu contraseña"
            ),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberDeviceChange = {},
            onLanguageSelected = {},
            onForgotPasswordClick = {},
            onBiometricClick = {},
            onLoginClick = {},
            onNavigateToSignUp = {}
        )
    }
}
