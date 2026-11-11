package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.FakeAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.AuthRepository
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class SignUpViewModel(
    private val authRepository: AuthRepository = FakeAuthRepository.instance,
    private val fakeRepoHelper: FakeAuthRepository? = authRepository as? FakeAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SignUpUiState(
            selectedLanguage = detectCurrentLanguage()
        )
    )
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

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

    fun onLanguageSelected(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
        val localeTag = when (language) {
            AppLanguage.ES -> "es"
            AppLanguage.EN -> "en"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    fun onSelectRole(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun onNextStep() {
        if (_uiState.value.isStep1Valid) {
            _uiState.update { it.copy(step = 2) }
        }
    }

    fun onPreviousStep() {
        if (_uiState.value.step == 2) {
            _uiState.update { it.copy(step = 1) }
        }
    }

    fun onFullNameChange(name: String) {
        _uiState.update {
            it.copy(
                fullName = name,
                fullNameErrorRes = null,
                generalErrorRes = null
            )
        }
    }

    fun onFullNameBlur() {
        val name = _uiState.value.fullName.trim()
        if (name.isNotEmpty() && !AuthValidators.isValidName(name)) {
            _uiState.update { it.copy(fullNameErrorRes = R.string.auth_signup_error_fullname) }
        }
    }

    fun onUsernameChange(username: String) {
        val clean = username.replace("@", "").replace(" ", "").lowercase()
        val taken = fakeRepoHelper?.isUsernameTaken(clean) ?: false
        _uiState.update {
            it.copy(
                username = clean,
                usernameErrorRes = if (taken) R.string.auth_signup_error_username_taken else null,
                isUsernameTaken = taken,
                generalErrorRes = null
            )
        }
    }

    fun onUsernameBlur() {
        val u = _uiState.value.username.trim()
        if (u.isEmpty()) return
        if (!AuthValidators.isValidUsername(u)) {
            _uiState.update {
                it.copy(
                    usernameErrorRes = R.string.auth_signup_error_username_invalid,
                    isUsernameTaken = false
                )
            }
        } else {
            val taken = fakeRepoHelper?.isUsernameTaken(u) ?: false
            _uiState.update {
                it.copy(
                    isUsernameTaken = taken,
                    usernameErrorRes = if (taken) R.string.auth_signup_error_username_taken else null
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        val clean = email.replace(" ", "").lowercase()
        val taken = fakeRepoHelper?.isEmailTaken(clean) ?: false
        _uiState.update {
            it.copy(
                email = clean,
                emailErrorRes = if (taken) R.string.auth_signup_error_email_taken else null,
                isEmailTaken = taken,
                generalErrorRes = null
            )
        }
    }

    fun onEmailBlur() {
        val e = _uiState.value.email.trim()
        if (e.isEmpty()) return
        if (!AuthValidators.isValidEmail(e)) {
            _uiState.update { it.copy(emailErrorRes = R.string.auth_signup_error_email_invalid) }
        } else {
            val taken = fakeRepoHelper?.isEmailTaken(e) ?: false
            _uiState.update {
                it.copy(
                    isEmailTaken = taken,
                    emailErrorRes = if (taken) R.string.auth_signup_error_email_taken else null
                )
            }
        }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(
                phone = phone,
                phoneErrorRes = null,
                generalErrorRes = null
            )
        }
    }

    fun onPhoneBlur() {
        val p = _uiState.value.phone.trim()
        if (p.isNotEmpty() && !AuthValidators.isValidPhone(p)) {
            _uiState.update { it.copy(phoneErrorRes = R.string.auth_signup_error_phone_invalid) }
        }
    }

    fun onOrganizationNameChange(name: String) {
        _uiState.update {
            it.copy(
                organizationName = name,
                organizationErrorRes = null,
                generalErrorRes = null
            )
        }
    }

    fun onOrganizationNameBlur() {
        val org = _uiState.value.organizationName.trim()
        if (org.isNotEmpty() && !AuthValidators.isValidName(org)) {
            val err = if (_uiState.value.selectedRole == UserRole.PRODUCER) {
                R.string.auth_signup_error_farm_empty
            } else {
                R.string.auth_signup_error_company_empty
            }
            _uiState.update { it.copy(organizationErrorRes = err) }
        }
    }

    fun onInvitationCodeChange(code: String) {
        _uiState.update { it.copy(invitationCode = code.uppercase()) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            val match = if (it.confirmPassword.isNotEmpty()) password == it.confirmPassword else true
            it.copy(
                password = password,
                passwordErrorRes = null,
                confirmPasswordErrorRes = if (!match && it.confirmPassword.isNotEmpty()) R.string.auth_signup_error_passwords_dont_match else null,
                generalErrorRes = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            val match = it.password == confirmPassword
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordErrorRes = if (!match && confirmPassword.isNotEmpty()) R.string.auth_signup_error_passwords_dont_match else null,
                generalErrorRes = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted) }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onSubmitSignUp(sessionRepository: SessionRepository) {
        val state = _uiState.value
        val role = state.selectedRole ?: return

        var hasError = false
        var fnError: Int? = null
        var uError: Int? = null
        var eError: Int? = null
        var pError: Int? = null
        var orgError: Int? = null
        var pwdError: Int? = null
        var cpError: Int? = null

        if (!AuthValidators.isValidName(state.fullName)) {
            fnError = R.string.auth_signup_error_fullname
            hasError = true
        }

        if (!AuthValidators.isValidUsername(state.username)) {
            uError = R.string.auth_signup_error_username_invalid
            hasError = true
        } else if (state.isUsernameTaken) {
            uError = R.string.auth_signup_error_username_taken
            hasError = true
        }

        if (!AuthValidators.isValidEmail(state.email)) {
            eError = R.string.auth_signup_error_email_invalid
            hasError = true
        } else if (state.isEmailTaken) {
            eError = R.string.auth_signup_error_email_taken
            hasError = true
        }

        if (!AuthValidators.isValidPhone(state.phone)) {
            pError = R.string.auth_signup_error_phone_invalid
            hasError = true
        }

        if (!AuthValidators.isValidName(state.organizationName)) {
            orgError = if (role == UserRole.PRODUCER) R.string.auth_signup_error_farm_empty else R.string.auth_signup_error_company_empty
            hasError = true
        }

        if (!AuthValidators.isValidPassword(state.password)) {
            pwdError = R.string.auth_signup_error_username_invalid // or password invalid
            hasError = true
        }

        if (!AuthValidators.doPasswordsMatch(state.password, state.confirmPassword)) {
            cpError = R.string.auth_signup_error_passwords_dont_match
            hasError = true
        }

        if (!state.termsAccepted) {
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    fullNameErrorRes = fnError,
                    usernameErrorRes = uError,
                    emailErrorRes = eError,
                    phoneErrorRes = pError,
                    organizationErrorRes = orgError,
                    passwordErrorRes = pwdError,
                    confirmPasswordErrorRes = cpError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalErrorRes = null) }

            val request = SignUpRequest(
                role = role,
                fullName = state.fullName.trim(),
                username = state.username.trim(),
                email = state.email.trim(),
                phone = state.phone.trim().ifEmpty { null },
                organizationName = state.organizationName.trim(),
                password = state.password,
                invitationCode = if (role == UserRole.PRODUCER) state.invitationCode.trim().ifEmpty { null } else null
            )

            when (val result = authRepository.signUp(request)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Guardar sesión sin recordar dispositivo (remember = false)
                    sessionRepository.save(result.session, remember = false)
                }
                is AuthResult.Failure -> {
                    when (result.error) {
                        is AuthError.UsernameTaken -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isUsernameTaken = true,
                                    usernameErrorRes = R.string.auth_signup_error_username_taken
                                )
                            }
                        }
                        is AuthError.EmailTaken -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isEmailTaken = true,
                                    emailErrorRes = R.string.auth_signup_error_email_taken
                                )
                            }
                        }
                        is AuthError.Network -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalErrorRes = R.string.auth_error_network
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalErrorRes = R.string.auth_error_network
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
