package com.rebootech.fruitlogix.auth.domain.repository

import com.rebootech.fruitlogix.auth.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentRole: Flow<UserRole?>
    suspend fun setRole(role: UserRole)
    suspend fun clearSession()
}

