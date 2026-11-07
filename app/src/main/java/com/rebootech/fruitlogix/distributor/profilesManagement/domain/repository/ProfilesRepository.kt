package com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerItem
import kotlinx.coroutines.flow.StateFlow

interface ProfilesRepository {
    val producers: StateFlow<List<ProducerItem>>
    val distributorProfile: StateFlow<DistributorProfile>

    fun registerProducer(
        name: String,
        farmName: String,
        location: String,
        cropType: String,
        email: String,
        phone: String,
        entityType: ProducerEntityType
    ): Result<ProducerItem>

    fun inviteProducer(
        name: String,
        emailOrPhone: String,
        location: String,
        cropType: String
    ): Result<ProducerItem>

    fun updateProducer(updated: ProducerItem): Result<ProducerItem>

    fun deleteProducer(producerId: String)

    fun updateDistributorProfile(profile: DistributorProfile): Result<DistributorProfile>
}