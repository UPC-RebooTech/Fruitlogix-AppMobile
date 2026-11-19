package com.rebootech.fruitlogix.auth.data

import com.rebootech.fruitlogix.auth.domain.model.Session
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository : SessionRepository {
    private val _session = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun save(session: Session, remember: Boolean) {
        _session.value = session
    }

    override suspend fun signOut() {
        _session.value = null
    }

    companion object {
        val instance by lazy { FakeSessionRepository() }
    }
}
