package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ServerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

data class ProfileUiState(
    val profile: DistributorProfile? = null,
    /** Signed-in account (from IAM). */
    val username: String = "",
    val enabledAlerts: Int = 0,
    val lastSyncAt: LocalDateTime? = null,
    val serverStatus: ServerStatus = ServerStatus.CONNECTING,
    val confirmSignOut: Boolean = false
)

/** My account (distributor): who is signed in and a summary of each setting. */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DistributorDependencies.profilesRepository
    private val settings = DistributorDependencies.settingsRepository(application)
    private val session = DataStoreSessionRepository.getInstance(application).session
    private val confirmSignOut = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(repository.distributorProfile, session) { profile, session ->
            // A newly registered distributor sees their own name; later edits win
            session?.displayName?.let(repository::adoptAccountName)
            profile to session?.username.orEmpty()
        },
        settings.notifications,
        settings.lastSyncAt,
        settings.serverStatus,
        confirmSignOut
    ) { (profile, username), notifications, lastSync, server, confirm ->
        ProfileUiState(
            profile = profile,
            username = username,
            enabledAlerts = notifications.enabledAlerts,
            lastSyncAt = lastSync,
            serverStatus = server,
            confirmSignOut = confirm
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun askSignOut() = confirmSignOut.update { true }
    fun cancelSignOut() = confirmSignOut.update { false }
}
