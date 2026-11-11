package com.rebootech.fruitlogix.auth.presentation.login

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.BuildConfig
import com.rebootech.fruitlogix.auth.data.FakeAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.repository.AuthRepository
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class LoginViewModel(
    private val authRepository: AuthRepository = FakeAuthRepository.instance,
    private val sessionRepository: SessionRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            appVersionName = try {
                BuildConfig.VERSION_NAME
            } catch (_: Exception) {
                "1.0"
            },
            selectedLanguage = detectCurrentLanguage()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private fun detectCurrentLanguage(): AppLanguage {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val firstLocale = currentLocales.get(0)
            if (firstLocale != null && firstLocale.language.equals("en", ignoreCase = true)) {
                return AppLanguage.EN
            }
        }
        val defaultLang = Locale.getDefault().language
        return if (defaultLang.equals("en", ignoreCase = true)) AppLanguage.EN else AppLanguage.ES
    }

    fun onUsernameChange(username: String) {
        // Sin mayúsculas automáticas ni espacios
        val cleaned = username.replace(" ", "").lowercase()
        _uiState.update {
            it.copy(
                username = cleaned,
                usernameError = null,
                generalError = null,
                hasCredentialsError = false
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null,
                hasCredentialsError = false
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRememberDeviceChange(remember: Boolean) {
        _uiState.update { it.copy(rememberDevice = remember) }
    }

    fun onLanguageSelected(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
        val localeTag = when (language) {
            AppLanguage.ES -> "es"
            AppLanguage.EN -> "en"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    fun onForgotPasswordClick() {
        _uiState.update { it.copy(snackbarMessage = "Próximamente disponible") }
    }

    fun onBiometricClick() {
        _uiState.update { it.copy(snackbarMessage = "Autenticación biométrica próximamente") }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onLoginClick(targetSessionRepo: SessionRepository? = sessionRepository) {
        val state = _uiState.value
        val username = state.username.trim()
        val password = state.password

        var hasError = false
        var uError: String? = null
        var pError: String? = null

        if (username.isEmpty()) {
            uError = "Ingresa tu usuario"
            hasError = true
        }

        if (password.isEmpty()) {
            pError = "Ingresa tu contraseña"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    usernameError = uError,
                    passwordError = pError,
                    generalError = null,
                    hasCredentialsError = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, hasCredentialsError = false) }

            when (val result = authRepository.signIn(username, password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    targetSessionRepo?.save(result.session, remember = state.rememberDevice)
                }
                is AuthResult.Failure -> {
                    val errorMsg = when (result.error) {
                        is AuthError.Network -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
                        else -> "Usuario o contraseña incorrectos. Verifica tus datos."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            password = "", // Se limpia solo la contraseña según 16.4
                            generalError = errorMsg,
                            hasCredentialsError = true
                        )
                    }
                }
            }
        }
    }
}
