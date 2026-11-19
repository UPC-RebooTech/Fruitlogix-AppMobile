package com.rebootech.fruitlogix.producer.profilesManagement.domain.repository

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val profile: StateFlow<ProducerProfile>
    suspend fun updateProfile(profile: ProducerProfile)
}
