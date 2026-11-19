package com.rebootech.fruitlogix.auth.domain.repository

import com.rebootech.fruitlogix.auth.domain.model.Session
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val session: StateFlow<Session?>
    suspend fun save(session: Session, remember: Boolean)
    suspend fun signOut()
}
