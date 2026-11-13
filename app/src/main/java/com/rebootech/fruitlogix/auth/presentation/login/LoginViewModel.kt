package com.rebootech.fruitlogix.auth.presentation.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.auth.data.RemoteAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.AccountProfile
import com.rebootech.fruitlogix.auth.domain.model.AuthError
import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sign in with username and password against the FruitLogix server. Saving the session
 * makes RootNavHost switch to the shell of the user's role, so no navigation is needed here.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = RemoteAuthRepository.getInstance(application)
    private val sessionRepository: SessionRepository = DataStoreSessionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        // Usernames are lowercase without spaces
        _uiState.update {
            it.copy(username = value.replace(" ", "").lowercase(), usernameError = null, generalError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onKeepSignedInChange(value: Boolean) {
        _uiState.update { it.copy(keepSignedIn = value) }
    }

    fun onForgotPassword() {
        _uiState.update { it.copy(infoMessage = R.string.login_forgot_soon) }
    }

    fun onInfoShown() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.isLoading) return
        val username = state.username.trim()
        val usernameError = if (username.isEmpty()) R.string.login_error_username_empty else null
        val passwordError = if (state.password.isEmpty()) R.string.login_error_password_empty else null
        if (usernameError != null || passwordError != null) {
            _uiState.update { it.copy(usernameError = usernameError, passwordError = passwordError, generalError = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, slowServer = false, generalError = null) }
            val slowHint = launch {
                delay(SLOW_SERVER_MS)
                _uiState.update { it.copy(slowServer = true) }
            }
            val result = authRepository.signIn(username, state.password)
            slowHint.cancel()
            when (result) {
                is AuthResult.Success -> {
                    sessionRepository.save(result.session, remember = state.keepSignedIn)
                    _uiState.update { it.copy(isLoading = false, slowServer = false) }
                }
                is AuthResult.NeedsRole -> _uiState.update {
                    it.copy(isLoading = false, slowServer = false, pendingRole = PendingRole(result.username, result.token))
                }
                is AuthResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        slowServer = false,
                        // Only the password is cleared; the username stays so it is not typed again
                        password = if (result.error == AuthError.InvalidCredentials) "" else it.password,
                        generalError = when (result.error) {
                            AuthError.Network -> R.string.iam_error_network
                            AuthError.InvalidCredentials -> R.string.login_error_credentials
                            else -> R.string.iam_error_unknown
                        }
                    )
                }
            }
        }
    }

    /** First sign in on this phone: the person says whether they are a producer or a distributor. */
    fun chooseRole(role: UserRole) {
        val state = _uiState.value
        val pending = state.pendingRole ?: return
        viewModelScope.launch {
            val session = authRepository.completeWithRole(
                username = pending.username,
                token = pending.token,
                profile = AccountProfile(
                    username = pending.username,
                    displayName = AuthValidators.nameFromUsername(pending.username),
                    role = role
                )
            )
            sessionRepository.save(session, remember = state.keepSignedIn)
            _uiState.update { it.copy(pendingRole = null) }
        }
    }

    fun dismissRoleChoice() {
        _uiState.update { it.copy(pendingRole = null) }
    }

    private companion object {
        const val SLOW_SERVER_MS = 4_000L
    }
}
