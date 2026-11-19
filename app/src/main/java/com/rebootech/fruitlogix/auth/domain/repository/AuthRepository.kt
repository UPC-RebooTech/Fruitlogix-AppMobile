package com.rebootech.fruitlogix.auth.domain.repository

import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest

interface AuthRepository {
    suspend fun signIn(username: String, password: String): AuthResult
    suspend fun signUp(request: SignUpRequest): AuthResult

    /** Early check while signing up. Sign-up itself still returns UsernameTaken / EmailTaken. */
    suspend fun isUsernameTaken(username: String): Boolean = false
    suspend fun isEmailTaken(email: String): Boolean = false
}
