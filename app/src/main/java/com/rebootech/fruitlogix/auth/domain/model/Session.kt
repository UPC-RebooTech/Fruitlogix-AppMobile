package com.rebootech.fruitlogix.auth.domain.model

data class Session(
    val username: String,
    val displayName: String,
    val role: UserRole,
    /** JWT from the FruitLogix server, sent on every API call. */
    val token: String? = null
)
