package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.annotation.StringRes
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators

/** Sign up in three short steps: role → about you → access (username and password). */
enum class SignUpStep(val number: Int) { ROLE(1), ABOUT(2), ACCESS(3) }

data class SignUpUiState(
    val step: SignUpStep = SignUpStep.ROLE,
    val role: UserRole? = null,

    // Step 2: about you
    val fullName: String = "",
    val organizationName: String = "",
    val email: String = "",

    // Step 3: access
    val username: String = "",
    /** True once the user types a username: from then on it is not suggested again. */
    val usernameEdited: Boolean = false,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val showInvitationCode: Boolean = false,
    val invitationCode: String = "",
    val termsAccepted: Boolean = false,

    val isLoading: Boolean = false,
    @field:StringRes val fullNameError: Int? = null,
    @field:StringRes val organizationError: Int? = null,
    @field:StringRes val emailError: Int? = null,
    @field:StringRes val usernameError: Int? = null,
    @field:StringRes val passwordError: Int? = null,
    @field:StringRes val termsError: Int? = null,
    @field:StringRes val generalError: Int? = null
) {
    val hasMinLength: Boolean get() = AuthValidators.isPasswordLongEnough(password)
    val hasLetter: Boolean get() = AuthValidators.hasPasswordLetter(password)
    val hasNumber: Boolean get() = AuthValidators.hasPasswordNumber(password)

    companion object {
        val TOTAL_STEPS = SignUpStep.entries.size
    }
}
