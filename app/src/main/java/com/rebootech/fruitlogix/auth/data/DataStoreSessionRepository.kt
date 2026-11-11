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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class DataStoreSessionRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : SessionRepository {

    private val KEY_USERNAME = stringPreferencesKey("session_username")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("session_display_name")
    private val KEY_ROLE = stringPreferencesKey("session_role")

    private val _session = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        scope.launch {
            try {
                val prefs = context.sessionDataStore.data.first()
                val username = prefs[KEY_USERNAME]
                val displayName = prefs[KEY_DISPLAY_NAME]
                val roleStr = prefs[KEY_ROLE]

                if (!username.isNullOrBlank() && !displayName.isNullOrBlank() && !roleStr.isNullOrBlank()) {
                    val role = try {
                        UserRole.valueOf(roleStr)
                    } catch (_: Exception) {
                        null
                    }
                    if (role != null) {
                        _session.value = Session(
                            username = username,
                            displayName = displayName,
                            role = role
                        )
                    }
                }
            } catch (_: Exception) {
                // If corrupted or reading error, remain null
            } finally {
                _isInitialized.value = true
            }
        }
    }

    override suspend fun save(session: Session, remember: Boolean) {
        _session.value = session
        if (remember) {
            context.sessionDataStore.edit { prefs ->
                prefs[KEY_USERNAME] = session.username
                prefs[KEY_DISPLAY_NAME] = session.displayName
                prefs[KEY_ROLE] = session.role.name
            }
        } else {
            // Memory only: clear any previous persisted session
            context.sessionDataStore.edit { prefs ->
                prefs.remove(KEY_USERNAME)
                prefs.remove(KEY_DISPLAY_NAME)
                prefs.remove(KEY_ROLE)
            }
        }
    }

    override suspend fun signOut() {
        _session.value = null
        try {
            context.sessionDataStore.edit { prefs ->
                prefs.remove(KEY_USERNAME)
                prefs.remove(KEY_DISPLAY_NAME)
                prefs.remove(KEY_ROLE)
            }
        } catch (_: Exception) {
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

