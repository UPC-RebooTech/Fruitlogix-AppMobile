package com.rebootech.fruitlogix.auth.presentation.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.auth.data.FakeAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.AuthRepository
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Each step is validated when the user taps Continue (not while typing), so errors
 * do not appear before the person has finished writing.
 */
class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = FakeAuthRepository.instance
    private val sessionRepository: SessionRepository = DataStoreSessionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    // Step 1 ---------------------------------------------------------------

    /** Choosing a role goes straight to the next step: one tap less. */
    fun selectRole(role: UserRole) {
        _uiState.update {
            it.copy(
                role = role,
                step = SignUpStep.ABOUT,
                // The organization error text depends on the role
                organizationError = null,
                invitationCode = if (role == UserRole.PRODUCER) it.invitationCode else "",
                showInvitationCode = role == UserRole.PRODUCER && it.showInvitationCode
            )
        }
    }

    /** @return false when already on the first step (the screen then goes back to sign in). */
    fun back(): Boolean {
        val step = _uiState.value.step
        if (step == SignUpStep.ROLE) return false
        _uiState.update {
            it.copy(step = SignUpStep.entries[step.ordinal - 1], generalError = null)
        }
        return true
    }

    // Step 2 ---------------------------------------------------------------

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, fullNameError = null) }
    fun onOrganizationChange(value: String) = _uiState.update { it.copy(organizationName = value, organizationError = null) }
    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value.replace(" ", "").lowercase(), emailError = null) }

    fun continueFromAbout() {
        val state = _uiState.value
        if (state.isLoading) return
        val fullNameError = if (!AuthValidators.isValidName(state.fullName)) R.string.signup_error_full_name else null
        val organizationError = if (!AuthValidators.isValidName(state.organizationName)) {
            if (state.role == UserRole.PRODUCER) R.string.signup_error_farm else R.string.signup_error_company
        } else null
        val emailError = if (!AuthValidators.isValidEmail(state.email)) R.string.signup_error_email else null

        if (fullNameError != null || organizationError != null || emailError != null) {
            _uiState.update {
                it.copy(fullNameError = fullNameError, organizationError = organizationError, emailError = emailError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (authRepository.isEmailTaken(state.email.trim())) {
                _uiState.update { it.copy(isLoading = false, emailError = R.string.signup_error_email_taken) }
                return@launch
            }
            val suggestion = if (state.usernameEdited) null else availableSuggestion(state.fullName)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    step = SignUpStep.ACCESS,
                    username = suggestion ?: it.username,
                    usernameError = null
                )
            }
        }
    }

    /** "Rosa Quispe" → "rosa.quispe", or "rosa.quispe2"… if it is taken. */
    private suspend fun availableSuggestion(fullName: String): String? {
        val base = AuthValidators.suggestUsername(fullName) ?: return null
        if (!authRepository.isUsernameTaken(base)) return base
        for (n in 2..9) {
            val candidate = "${base.take(19)}$n"
            if (!authRepository.isUsernameTaken(candidate)) return candidate
        }
        return null
    }

    // Step 3 ---------------------------------------------------------------

    fun onUsernameChange(value: String) = _uiState.update {
        it.copy(username = AuthValidators.cleanUsername(value), usernameEdited = true, usernameError = null)
    }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onShowInvitationCode() = _uiState.update { it.copy(showInvitationCode = true) }
    fun onInvitationCodeChange(value: String) = _uiState.update { it.copy(invitationCode = value.uppercase().trim()) }
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(termsAccepted = value, termsError = null) }

    fun submit() {
        val state = _uiState.value
        val role = state.role ?: return
        if (state.isLoading) return
        val usernameError = if (!AuthValidators.isValidUsername(state.username)) R.string.signup_error_username else null
        val passwordError = if (!AuthValidators.isValidPassword(state.password)) R.string.signup_error_password else null
        val termsError = if (!state.termsAccepted) R.string.signup_error_terms else null
        if (usernameError != null || passwordError != null || termsError != null) {
            _uiState.update {
                it.copy(usernameError = usernameError, passwordError = passwordError, termsError = termsError, generalError = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val request = SignUpRequest(
                role = role,
                fullName = state.fullName.trim(),
                username = state.username.trim(),
                email = state.email.trim(),
                phone = null,
                organizationName = state.organizationName.trim(),
                password = state.password,
                invitationCode = state.invitationCode.takeIf { role == UserRole.PRODUCER && it.isNotBlank() }
            )
            when (val result = authRepository.signUp(request)) {
                is AuthResult.Success -> {
                    // Just created the account: keep it signed in, like "Keep me signed in" on sign in
                    sessionRepository.save(result.session, remember = true)
                    _uiState.update { it.copy(isLoading = false) }
                }
                is AuthResult.Failure -> _uiState.update {
                    when (result.error) {
                        AuthError.UsernameTaken -> it.copy(isLoading = false, usernameError = R.string.signup_error_username_taken)
                        // The email is on the previous step: go back there and show the error
                        AuthError.EmailTaken -> it.copy(
                            isLoading = false,
                            step = SignUpStep.ABOUT,
                            emailError = R.string.signup_error_email_taken
                        )
                        AuthError.WeakPassword -> it.copy(isLoading = false, passwordError = R.string.signup_error_password)
                        AuthError.Network -> it.copy(isLoading = false, generalError = R.string.iam_error_network)
                        else -> it.copy(isLoading = false, generalError = R.string.iam_error_unknown)
                    }
                }
            }
        }
    }
}
