package com.rebootech.fruitlogix.auth.data

import android.content.Context
import androidx.core.content.edit
import com.rebootech.fruitlogix.auth.domain.model.AccountProfile
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.AccountProfileStore

/** Accounts used on this phone (SharedPreferences), keyed by lowercase username. */
class PrefsAccountProfileStore(context: Context) : AccountProfileStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun get(username: String): AccountProfile? {
        val key = username.trim().lowercase()
        val role = prefs.getString("$key.$ROLE", null)
            ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            ?: return null
        return AccountProfile(
            username = key,
            displayName = prefs.getString("$key.$NAME", null).orEmpty().ifBlank { key },
            role = role,
            email = prefs.getString("$key.$EMAIL", null).orEmpty(),
            organizationName = prefs.getString("$key.$ORG", null).orEmpty()
        )
    }

    override fun save(profile: AccountProfile) {
        val key = profile.username.trim().lowercase()
        prefs.edit {
            putString("$key.$ROLE", profile.role.name)
            putString("$key.$NAME", profile.displayName)
            putString("$key.$EMAIL", profile.email)
            putString("$key.$ORG", profile.organizationName)
        }
    }

    private companion object {
        const val PREFS = "auth_accounts"
        const val ROLE = "role"
        const val NAME = "name"
        const val EMAIL = "email"
        const val ORG = "org"
    }
}
