package com.rebootech.fruitlogix.distributor.profilesManagement.data

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ContactRules
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.ProducerDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Producers of the distributor's network (the same names used by Orders, Fleet and Invoices).
 * When the Render backend answers (/api/v1/producers) its producers are merged in, and new or
 * edited producers are sent there. Without connection the sample data keeps the app usable.
 */
class FakeProfilesRepository private constructor() : ProfilesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextNumber = 10

    private val _producers = MutableStateFlow(initialProducers())
    override val producers: StateFlow<List<Producer>> = _producers.asStateFlow()

    private val _profile = MutableStateFlow(
        DistributorProfile(
            fullName = "Carlos Mendoza",
            roleTitle = "Jefe de operaciones",
            companyHub = "Lima Central Hub · Callao",
            email = "carlos.mendoza@fruitlogix.pe",
            phone = "+51 987 654 321",
            ruc = "20601234567"
        )
    )
    override val distributorProfile: StateFlow<DistributorProfile> = _profile.asStateFlow()

    @Volatile private var accountNameAdopted = false

    override fun adoptAccountName(name: String) {
        if (accountNameAdopted || name.isBlank()) return
        accountNameAdopted = true
        _profile.value = _profile.value.copy(fullName = name.trim())
    }

    override suspend fun registerProducer(draft: ProducerDraft): Producer {
        val producer = Producer(
            id = "PROD-${nextNumber++}",
            name = draft.name.trim(), farmName = draft.farmName.trim(), location = draft.location.trim(),
            cropType = draft.cropType.trim(), email = draft.email.trim(), phone = draft.phone.trim(),
            entityType = draft.entityType, qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0,
            activeOrders = 0, incidents30d = 0
        )
        _producers.update { listOf(producer) + it }
        scope.launch { runCatching { RetrofitClient.api.createProducer(producer.toDto()) } }
        return producer
    }

    override suspend fun inviteProducer(name: String, contact: String, location: String, cropType: String): Producer {
        val clean = contact.trim()
        val producer = Producer(
            id = "PROD-${nextNumber++}",
            name = name.trim(), farmName = "", location = location.trim(), cropType = cropType.trim(),
            email = if (ContactRules.isEmail(clean)) clean else "",
            phone = if (ContactRules.isPhone(clean)) clean else "",
            entityType = ProducerEntityType.INDIVIDUAL, qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0,
            activeOrders = 0, incidents30d = 0, status = ProducerStatus.INVITED
        )
        _producers.update { listOf(producer) + it }
        return producer
    }

    override suspend fun updateProducer(id: String, draft: ProducerDraft): Producer? {
        var updated: Producer? = null
        _producers.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    name = draft.name.trim(), farmName = draft.farmName.trim(), location = draft.location.trim(),
                    cropType = draft.cropType.trim(), email = draft.email.trim(), phone = draft.phone.trim(),
                    entityType = draft.entityType
                ).also { p -> updated = p } else it
            }
        }
        updated?.let { p -> scope.launch { runCatching { RetrofitClient.api.updateProducer(p.id, p.toDto()) } } }
        return updated
    }

    override suspend fun deleteProducer(id: String) {
        _producers.update { list -> list.filterNot { it.id == id } }
        scope.launch { runCatching { RetrofitClient.api.deleteProducer(id) } }
    }

    override suspend fun updateDistributorProfile(profile: DistributorProfile) {
        accountNameAdopted = true
        _profile.value = profile.copy(fullName = profile.fullName.trim(), email = profile.email.trim(), phone = profile.phone.trim())
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getProducers() } }
        remote.onSuccess { dtos ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            val mapped = dtos.mapIndexedNotNull { index, dto ->
                Producer(
                    id = dto.id?.toString() ?: "API-${index + 1}",
                    name = dto.name ?: return@mapIndexedNotNull null,
                    farmName = dto.rucOrDni?.let { "RUC/DNI $it" }.orEmpty(),
                    location = dto.location.orEmpty(),
                    cropType = dto.specialty.orEmpty(),
                    email = dto.email.orEmpty(),
                    phone = dto.phone.orEmpty(),
                    entityType = if (dto.type.equals("Company", ignoreCase = true)) ProducerEntityType.COMPANY else ProducerEntityType.INDIVIDUAL,
                    qualityRating = dto.rating ?: 0.0, weeklyTons = 0.0, stockKg = 0, activeOrders = 0, incidents30d = 0
                )
            }
            if (mapped.isNotEmpty()) _producers.update { (mapped + it).distinctBy { p -> p.name.lowercase() } }
        }.onFailure {
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
        }
    }

    private fun Producer.toDto() = ProducerDto(
        name = name, type = entityType.backendValue, location = location, specialty = cropType,
        email = email.ifBlank { null }, phone = phone.ifBlank { null }, rating = qualityRating
    )

    companion object {
        val instance: FakeProfilesRepository by lazy { FakeProfilesRepository() }

        private fun initialProducers() = listOf(
            Producer("PROD-01", "Fundo Santa Rosa", "Fundo Santa Rosa", "Huaraz, Áncash", "Palta Hass",
                "contacto@fundosantarosa.pe", "+51 956 410 228", ProducerEntityType.COMPANY,
                qualityRating = 4.8, weeklyTons = 4.2, stockKg = 570, activeOrders = 1, incidents30d = 0),
            Producer("PROD-02", "Viñedos de Ica", "Viñedos de Ica", "Ica", "Uva Red Globe",
                "ventas@vinedosdeica.pe", "+51 987 112 340", ProducerEntityType.COMPANY,
                qualityRating = 4.9, weeklyTons = 5.4, stockKg = 800, activeOrders = 1, incidents30d = 0),
            Producer("PROD-03", "Campos del Norte", "Sector B3", "Piura", "Mango Kent",
                "operaciones@camposdelnorte.pe", "+51 944 870 615", ProducerEntityType.COMPANY,
                qualityRating = 4.4, weeklyTons = 3.9, stockKg = 600, activeOrders = 2, incidents30d = 1),
            Producer("PROD-04", "Agro Valle Verde", "Fundo El Paraíso", "Huaral, Lima", "Fresa",
                "juan.quispe@vallev.pe", "+51 977 445 566", ProducerEntityType.INDIVIDUAL,
                qualityRating = 4.5, weeklyTons = 2.1, stockKg = 150, activeOrders = 1, incidents30d = 0),
            Producer("PROD-05", "Finca Los Andes", "Finca Los Andes", "Arequipa", "Uva Red Globe",
                "fincalosandes@agro.pe", "+51 944 332 211", ProducerEntityType.COMPANY,
                qualityRating = 4.2, weeklyTons = 1.8, stockKg = 300, activeOrders = 0, incidents30d = 1),
            Producer("PROD-06", "Rosa Quispe", "", "Chanchamayo, Junín", "Mango Kent",
                "", "+51 912 345 678", ProducerEntityType.INDIVIDUAL,
                qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0, activeOrders = 0, incidents30d = 0,
                status = ProducerStatus.INVITED)
        )
    }
}
