package com.rebootech.fruitlogix.auth.domain.repository

import com.rebootech.fruitlogix.auth.domain.model.AccountProfile

/** Role and personal data of the accounts used on this phone. Kept after signing out. */
interface AccountProfileStore {
    fun get(username: String): AccountProfile?
    fun save(profile: AccountProfile)
}
