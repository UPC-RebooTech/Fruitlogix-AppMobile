package com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import kotlinx.coroutines.flow.StateFlow

/** Validation happens in domain rules before calling these methods. */
interface ProfilesRepository {
    val producers: StateFlow<List<Producer>>
    val distributorProfile: StateFlow<DistributorProfile>

    suspend fun registerProducer(draft: ProducerDraft): Producer

    /** Adds the producer as "invited" until they finish their sign-up. */
    suspend fun inviteProducer(name: String, contact: String, location: String, cropType: String): Producer

    suspend fun updateProducer(id: String, draft: ProducerDraft): Producer?

    suspend fun deleteProducer(id: String)

    suspend fun updateDistributorProfile(profile: DistributorProfile)

    /**
     * Uses the signed-in account's name for the profile the first time it is opened,
     * so a newly registered distributor sees their own name. Later edits win.
     */
    fun adoptAccountName(name: String) {}

    /** Pulls producers from the server and merges them; does nothing when offline. */
    suspend fun refresh() {}
}
