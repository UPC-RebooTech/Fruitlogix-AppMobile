package com.rebootech.fruitlogix.auth.presentation.signup

import androidx.annotation.StringRes
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage

data class SignUpUiState(
    val step: Int = 1,
    val selectedRole: UserRole? = null,
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val organizationName: String = "",
    val invitationCode: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.ES,
    val snackbarMessage: String? = null,

    // Inline errors as StringRes IDs
    @field:StringRes val generalErrorRes: Int? = null,
    @field:StringRes val fullNameErrorRes: Int? = null,
    @field:StringRes val usernameErrorRes: Int? = null,
    val isUsernameTaken: Boolean = false,
    @field:StringRes val emailErrorRes: Int? = null,
    val isEmailTaken: Boolean = false,
    @field:StringRes val phoneErrorRes: Int? = null,
    @field:StringRes val organizationErrorRes: Int? = null,
    @field:StringRes val passwordErrorRes: Int? = null,
    @field:StringRes val confirmPasswordErrorRes: Int? = null
) {
    val isPasswordLengthValid: Boolean get() = AuthValidators.isPasswordLongEnough(password)
    val hasPasswordLetter: Boolean get() = AuthValidators.hasPasswordLetter(password)
    val hasPasswordNumber: Boolean get() = AuthValidators.hasPasswordNumber(password)
    val isPasswordValid: Boolean get() = AuthValidators.isValidPassword(password)
    val doPasswordsMatch: Boolean get() = AuthValidators.doPasswordsMatch(password, confirmPassword)

    val isStep1Valid: Boolean get() = selectedRole != null

    val isStep2Valid: Boolean
        get() = AuthValidators.isValidName(fullName) &&
            AuthValidators.isValidUsername(username) &&
            !isUsernameTaken &&
            AuthValidators.isValidEmail(email) &&
            !isEmailTaken &&
            AuthValidators.isValidPhone(phone) &&
            AuthValidators.isValidName(organizationName) &&
            isPasswordValid &&
            doPasswordsMatch &&
            termsAccepted
}
