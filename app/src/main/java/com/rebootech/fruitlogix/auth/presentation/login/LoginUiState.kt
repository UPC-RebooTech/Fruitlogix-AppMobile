package com.rebootech.fruitlogix.auth.presentation.login

import com.rebootech.fruitlogix.shared.ui.components.AppLanguage

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberDevice: Boolean = false,
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val hasCredentialsError: Boolean = false,
    val isBiometricsAvailable: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.ES,
    val appVersionName: String = "1.0",
    val snackbarMessage: String? = null
)

