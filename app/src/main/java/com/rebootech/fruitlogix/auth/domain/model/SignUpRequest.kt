package com.rebootech.fruitlogix.auth.domain.model

data class SignUpRequest(
    val role: UserRole,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String?,
    val organizationName: String,
    val password: String,
    val invitationCode: String? = null
)

