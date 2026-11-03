package com.rebootech.fruitlogix.auth.data

import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository : SessionRepository {
    private val _currentRole = MutableStateFlow<UserRole?>(null)
    override val currentRole: StateFlow<UserRole?> = _currentRole.asStateFlow()

    override suspend fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    override suspend fun clearSession() {
        _currentRole.value = null
    }

    companion object {
        val instance by lazy { FakeSessionRepository() }
    }
}

