package com.rebootech.fruitlogix.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.auth.data.FakeSessionRepository
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectRoleViewModel(
    private val sessionRepository: SessionRepository = FakeSessionRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectRoleUiState())
    val uiState: StateFlow<SelectRoleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.currentRole.collect { role ->
                _uiState.update { it.copy(selectedRole = role) }
            }
        }
    }

    fun onSelectRole(role: UserRole, onRoleConfirmed: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedRole = role) }
            sessionRepository.setRole(role)
            onRoleConfirmed(role)
        }
    }
}

