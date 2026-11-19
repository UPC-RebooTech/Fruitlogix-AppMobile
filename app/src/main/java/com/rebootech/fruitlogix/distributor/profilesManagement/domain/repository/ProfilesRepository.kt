package com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerSaveResult
import kotlinx.coroutines.flow.StateFlow

/** Validation happens in domain rules before calling these methods. */
interface ProfilesRepository {
    val producers: StateFlow<List<Producer>>
    val distributorProfile: StateFlow<DistributorProfile>

    /** Saves on the server; without connection it stays on this phone ([ProducerSaveResult.SavedOnPhoneOnly]). */
    suspend fun registerProducer(draft: ProducerDraft): ProducerSaveResult

    /** Adds the producer as "invited" until they finish their sign-up. */
    suspend fun inviteProducer(name: String, contact: String, location: String, cropType: String): Producer

    suspend fun updateProducer(id: String, draft: ProducerDraft): ProducerSaveResult

    /** @return false when the server could not remove it (the producer is kept). */
    suspend fun deleteProducer(id: String): Boolean

    suspend fun updateDistributorProfile(profile: DistributorProfile)

    /**
     * Uses the signed-in account's name for the profile the first time it is opened,
     * so a newly registered distributor sees their own name. Later edits win.
     */
    fun adoptAccountName(name: String) {}

    /** Pulls producers from the server (replacing the previous copy); does nothing when offline. */
    suspend fun refresh() {}
}
