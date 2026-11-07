package com.rebootech.fruitlogix.auth.presentation

import com.rebootech.fruitlogix.auth.domain.model.UserRole

data class SelectRoleUiState(
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false
)

