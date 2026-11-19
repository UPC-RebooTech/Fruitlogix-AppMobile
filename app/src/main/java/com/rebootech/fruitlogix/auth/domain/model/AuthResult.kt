package com.rebootech.fruitlogix.auth.domain.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object UsernameTaken : AuthError
    data object EmailTaken : AuthError
    data object WeakPassword : AuthError
    data object Network : AuthError
    data object Unknown : AuthError
}

sealed interface AuthResult {
    data class Success(val session: Session) : AuthResult
    data class Failure(val error: AuthError) : AuthResult

    /**
     * The password is right, but this phone does not know the account's role yet
     * (the server only stores username and password). The person picks it once.
     */
    data class NeedsRole(val username: String, val token: String) : AuthResult
}
