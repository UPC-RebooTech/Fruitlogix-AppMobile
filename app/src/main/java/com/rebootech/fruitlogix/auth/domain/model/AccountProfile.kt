package com.rebootech.fruitlogix.auth.domain.model

/**
 * What the server does not store yet (it only keeps username and password):
 * role, name, email and organization. Saved on the phone per username.
 */
data class AccountProfile(
    val username: String,
    val displayName: String,
    val role: UserRole,
    val email: String = "",
    val organizationName: String = ""
)
