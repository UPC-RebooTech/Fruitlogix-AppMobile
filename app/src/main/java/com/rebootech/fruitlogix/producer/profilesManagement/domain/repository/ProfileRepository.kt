package com.rebootech.fruitlogix.producer.profilesManagement.domain.repository

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProfileSaveResult
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProfileStatus
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val profile: StateFlow<ProducerProfile>

    /** MISSING: the account has no producer profile yet (it must be created before products or lots). */
    val status: StateFlow<ProfileStatus>

    /** Id of the producer profile on the server (null until it is known). */
    val producerId: StateFlow<Int?>

    /** Reads GET /producers/me. */
    suspend fun refresh() {}

    /** POST /producers: one profile per account (409 the second time). */
    suspend fun createProfile(profile: ProducerProfile): ProfileSaveResult = ProfileSaveResult(false)

    suspend fun updateProfile(profile: ProducerProfile): ProfileSaveResult
}
