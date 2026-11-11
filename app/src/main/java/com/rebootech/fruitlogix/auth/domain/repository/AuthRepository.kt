package com.rebootech.fruitlogix.auth.domain.repository

import com.rebootech.fruitlogix.auth.domain.model.AuthResult
import com.rebootech.fruitlogix.auth.domain.model.SignUpRequest

interface AuthRepository {
    suspend fun signIn(username: String, password: String): AuthResult
    suspend fun signUp(request: SignUpRequest): AuthResult
}

