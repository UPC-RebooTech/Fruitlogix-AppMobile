package com.rebootech.fruitlogix.auth.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rebootech.fruitlogix.auth.domain.model.Session
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.auth.domain.rules.JwtExpiry
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Signed-in session. With "Keep me signed in" it is saved on the phone (DataStore) together
 * with the server token; the token is handed to [RetrofitClient] so every API call carries it.
 */
class DataStoreSessionRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : SessionRepository {

    private val keyUsername = stringPreferencesKey("session_username")
    private val keyDisplayName = stringPreferencesKey("session_display_name")
    private val keyRole = stringPreferencesKey("session_role")
    private val keyToken = stringPreferencesKey("session_token")

    private val _session = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        scope.launch {
            try {
                val prefs = context.sessionDataStore.data.first()
                val username = prefs[keyUsername]
                val displayName = prefs[keyDisplayName]
                val role = prefs[keyRole]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                val token = prefs[keyToken]

                // Sessions without a server token (older versions) or with an expired one ask to sign in again
                val valid = !username.isNullOrBlank() && !displayName.isNullOrBlank() && role != null &&
                    !token.isNullOrBlank() && !JwtExpiry.isExpired(token)
                if (valid) {
                    publish(Session(username = username!!, displayName = displayName!!, role = role!!, token = token))
                } else if (username != null) {
                    clearStored()
                }
            } catch (_: Exception) {
                // Corrupted or unreadable: stay signed out
            } finally {
                _isInitialized.value = true
            }
        }
    }

    override suspend fun save(session: Session, remember: Boolean) {
        publish(session)
        if (remember) {
            context.sessionDataStore.edit { prefs ->
                prefs[keyUsername] = session.username
                prefs[keyDisplayName] = session.displayName
                prefs[keyRole] = session.role.name
                session.token?.let { prefs[keyToken] = it } ?: prefs.remove(keyToken)
            }
        } else {
            // Memory only: clear any previous persisted session
            clearStored()
        }
    }

    override suspend fun signOut() {
        publish(null)
        try {
            clearStored()
        } catch (_: Exception) {
        }
    }

    private fun publish(session: Session?) {
        RetrofitClient.jwtToken = session?.token
        _session.value = session
    }

    private suspend fun clearStored() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(keyUsername)
            prefs.remove(keyDisplayName)
            prefs.remove(keyRole)
            prefs.remove(keyToken)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataStoreSessionRepository? = null

        fun getInstance(context: Context): DataStoreSessionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreSessionRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
