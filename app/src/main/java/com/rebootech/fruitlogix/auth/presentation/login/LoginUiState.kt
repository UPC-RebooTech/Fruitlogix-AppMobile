package com.rebootech.fruitlogix.auth.presentation.login

import androidx.annotation.StringRes

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    /** On by default: people in the field should not have to type the password every day. */
    val keepSignedIn: Boolean = true,
    val isLoading: Boolean = false,
    /** The server is taking long (Render waking up): show a hint so the person waits. */
    val slowServer: Boolean = false,
    @field:StringRes val usernameError: Int? = null,
    @field:StringRes val passwordError: Int? = null,
    @field:StringRes val generalError: Int? = null,
    /** One-off message (e.g. "password recovery coming soon"). */
    @field:StringRes val infoMessage: Int? = null,
    /** Signed in, but this phone does not know the role yet: ask it once. */
    val pendingRole: PendingRole? = null
)

data class PendingRole(val username: String, val token: String)
