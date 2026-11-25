package com.rebootech.fruitlogix.auth.domain.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object UsernameTaken : AuthError
    data object EmailTaken : AuthError
    data object WeakPassword : AuthError
    data object Network : AuthError
    /** The account is a commercial client (or internal): it belongs to the client app. */
    data object WrongApp : AuthError
    data object Unknown : AuthError
}

sealed interface AuthResult {
    data class Success(val session: Session) : AuthResult
    data class Failure(val error: AuthError) : AuthResult

}
