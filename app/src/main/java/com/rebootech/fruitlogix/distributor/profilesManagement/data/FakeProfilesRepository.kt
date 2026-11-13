package com.rebootech.fruitlogix.distributor.profilesManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ContactRules
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.ProducerRequestDto
import com.rebootech.fruitlogix.shared.core.network.ProducerResponseDto
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
import java.util.concurrent.ConcurrentHashMap

class FakeProfilesRepository private constructor() : ProfilesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextNumber = 10
    private val backendIds = ConcurrentHashMap<String, Int>()

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

    init {
        scope.launch { refresh() }
    }

    override fun adoptAccountName(name: String) {
        if (accountNameAdopted || name.isBlank()) return
        accountNameAdopted = true
        _profile.value = _profile.value.copy(fullName = name.trim())
    }

    override suspend fun registerProducer(draft: ProducerDraft): Producer = withContext(Dispatchers.IO) {
        var assignedId = "PROD-${nextNumber++}"
        val tempProducer = Producer(
            id = assignedId,
            name = draft.name.trim(),
            farmName = draft.farmName.trim(),
            location = draft.location.trim(),
            cropType = draft.cropType.trim(),
            email = draft.email.trim(),
            phone = draft.phone.trim(),
            entityType = draft.entityType,
            qualityRating = 0.0,
            weeklyTons = 0.0,
            stockKg = 0,
            activeOrders = 0,
            incidents30d = 0
        )

        val remoteResult = runCatching { RetrofitClient.api.createProducer(tempProducer.toRequestDto()) }
        remoteResult.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                response.body()?.id?.let { realId ->
                    assignedId = "PROD-$realId"
                    backendIds[assignedId] = realId
                }
            } else {
                Log.w("FruitLogix-API", "POST /producers rechazado con HTTP ${response.code()}")
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, err.message)
        }

        val finalProducer = tempProducer.copy(id = assignedId)
        _producers.update { listOf(finalProducer) + it.filterNot { p -> p.id == finalProducer.id } }
        finalProducer
    }

    override suspend fun inviteProducer(name: String, contact: String, location: String, cropType: String): Producer {
        val clean = contact.trim()
        val producer = Producer(
            id = "PROD-${nextNumber++}",
            name = name.trim(),
            farmName = "",
            location = location.trim(),
            cropType = cropType.trim(),
            email = if (ContactRules.isEmail(clean)) clean else "",
            phone = if (ContactRules.isPhone(clean)) clean else "",
            entityType = ProducerEntityType.INDIVIDUAL,
            qualityRating = 0.0,
            weeklyTons = 0.0,
            stockKg = 0,
            activeOrders = 0,
            incidents30d = 0,
            status = ProducerStatus.INVITED
        )
        _producers.update { listOf(producer) + it }
        return producer
    }

    override suspend fun updateProducer(id: String, draft: ProducerDraft): Producer? = withContext(Dispatchers.IO) {
        val current = _producers.value.firstOrNull { it.id == id } ?: return@withContext null
        val candidate = current.copy(
            name = draft.name.trim(),
            farmName = draft.farmName.trim(),
            location = draft.location.trim(),
            cropType = draft.cropType.trim(),
            email = draft.email.trim(),
            phone = draft.phone.trim(),
            entityType = draft.entityType
        )

        val realId = backendIds[id]
        if (realId != null) {
            val response = runCatching {
                RetrofitClient.api.updateProducer(realId, candidate.toRequestDto())
            }.getOrNull()
            if (response != null && !response.isSuccessful) {
                Log.e("FruitLogix-API", "PUT /producers/$realId rechazado con HTTP ${response.code()}")
                return@withContext null
            }
        }

        _producers.update { list -> list.map { if (it.id == id) candidate else it } }
        candidate
    }

    override suspend fun deleteProducer(id: String) = withContext(Dispatchers.IO) {
        val realId = backendIds[id]
        if (realId != null) {
            val response = runCatching { RetrofitClient.api.deleteProducer(realId) }.getOrNull()
            if (response != null && !response.isSuccessful && response.code() != 404) {
                Log.e("FruitLogix-API", "DELETE /producers/$realId rechazado con HTTP ${response.code()}")
                return@withContext
            }
            backendIds.remove(id)
        }
        _producers.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateDistributorProfile(profile: DistributorProfile) {
        accountNameAdopted = true
        _profile.value = profile.copy(
            fullName = profile.fullName.trim(),
            email = profile.email.trim(),
            phone = profile.phone.trim()
        )
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getProducers() } }
        remote.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val dtos = response.body().orEmpty()
                val mapped = dtos.mapIndexedNotNull { index, dto -> dto.toDomain(index) }
                if (mapped.isNotEmpty()) {
                    _producers.update { (mapped + it).distinctBy { p -> p.name.lowercase() } }
                }
            } else {
                RetrofitClient.updateSyncStatus(
                    BackendSyncStatus.OFFLINE_FALLBACK,
                    "GET /producers devolvió HTTP ${response.code()}"
                )
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, err.message)
        }
    }

    private fun Producer.toRequestDto(): ProducerRequestDto {
        val parts = location.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val city = parts.firstOrNull() ?: "Lima"
        val region = parts.getOrNull(1) ?: city
        val generatedTaxId = if (entityType == ProducerEntityType.COMPANY) {
            "20${((name.hashCode() and 0x7FFFFFFF) % 899999999 + 100000000)}"
        } else {
            "${((name.hashCode() and 0x7FFFFFFF) % 89999999 + 10000000)}"
        }

        return ProducerRequestDto(
            producerType = entityType.backendValue,
            fullName = name,
            taxId = generatedTaxId,
            legalName = farmName.ifBlank { name },
            country = "Perú",
            region = region,
            city = city,
            address = farmName.ifBlank { "$city, $region" },
            crop = cropType.ifBlank { "Palta Hass" },
            email = email.ifBlank { null },
            phone = phone.ifBlank { null }
        )
    }

    private fun ProducerResponseDto.toDomain(index: Int): Producer? {
        val displayName = fullName?.takeIf { it.isNotBlank() }
            ?: legalName?.takeIf { it.isNotBlank() }
            ?: return null

        val code = id?.let { "PROD-$it" } ?: "PROD-API-${index + 1}"
        if (id != null) {
            backendIds[code] = id
        }

        val combinedLocation = listOfNotNull(
            city?.takeIf { it.isNotBlank() },
            region?.takeIf { it.isNotBlank() && !it.equals(city, ignoreCase = true) }
        ).joinToString(", ").ifBlank { country ?: "Perú" }

        return Producer(
            id = code,
            name = displayName,
            farmName = legalName?.takeIf { it.isNotBlank() } ?: address.orEmpty(),
            location = combinedLocation,
            cropType = crop.orEmpty().ifBlank { "Palta Hass" },
            email = email.orEmpty(),
            phone = phone.orEmpty(),
            entityType = if (producerType.equals("Company", ignoreCase = true)) {
                ProducerEntityType.COMPANY
            } else {
                ProducerEntityType.INDIVIDUAL
            },
            qualityRating = rating ?: 4.7,
            weeklyTons = 3.5,
            stockKg = 500,
            activeOrders = 1,
            incidents30d = 0
        )
    }

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