package com.rebootech.fruitlogix.distributor.profilesManagement.data

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerItem
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
object FakeProfilesRepository : ProfilesRepository {

    private val _producers = MutableStateFlow(
        listOf(
            ProducerItem(
                id = "PROD-01",
                name = "Agropecuaria El Valle",
                farmName = "Fundo Las Lilas",
                location = "Ica, Perú",
                cropType = "Palta Hass",
                email = "contacto@elvalle.pe",
                phone = "+51 988 112 233",
                entityType = ProducerEntityType.COMPANY,
                qualityRating = 4.8,
                weeklyTons = 4.2,
                currentStockSummary = "Palta Hass: 450kg • Extra: 120kg",
                activeOrdersCount = 2,
                incidents30d = 0
            ),
            ProducerItem(
                id = "PROD-02",
                name = "Juan Quispe",
                farmName = "Finca El Paraíso",
                location = "Lurín, Lima",
                cropType = "Palta Hass",
                email = "juan.paraiso@agro.pe",
                phone = "+51 977 445 566",
                entityType = ProducerEntityType.INDIVIDUAL,
                qualityRating = 4.5,
                weeklyTons = 2.1,
                currentStockSummary = "Palta Hass: 150kg • Variedad Extra: 50kg",
                activeOrdersCount = 2,
                incidents30d = 0
            ),
            ProducerItem(
                id = "PROD-03",
                name = "Agrícola Olmos S.A.C.",
                farmName = "Sector B3",
                location = "Lambayeque, Perú",
                cropType = "Arándanos & Mango",
                email = "operaciones@agricolaolmos.pe",
                phone = "+51 955 889 900",
                entityType = ProducerEntityType.COMPANY,
                qualityRating = 4.9,
                weeklyTons = 5.4,
                currentStockSummary = "Mango Kent: 600kg • Arándano: 200kg",
                activeOrdersCount = 3,
                incidents30d = 0
            ),
            ProducerItem(
                id = "PROD-04",
                name = "Campos del Sur",
                farmName = "Valle de Majes",
                location = "Arequipa, Perú",
                cropType = "Uva de Mesa",
                email = "camposdelsur@agro.pe",
                phone = "+51 944 332 211",
                entityType = ProducerEntityType.COMPANY,
                qualityRating = 4.2,
                weeklyTons = 1.8,
                currentStockSummary = "Uva Red Globe: 300kg",
                activeOrdersCount = 0,
                incidents30d = 1,
                isPending = true
            )
        )
    )
    override val producers: StateFlow<List<ProducerItem>> = _producers.asStateFlow()

    private val _distributorProfile = MutableStateFlow(
        DistributorProfile(
            fullName = "Carlos Mendoza",
            roleTitle = "Distribuidor Logístico / Admin",
            companyHub = "Lima Central Hub - Panamericana",
            email = "carlos.mendoza@fruitlogix.pe",
            phone = "+51 987 654 321",
            ruc = "20601234567"
        )
    )
    override val distributorProfile: StateFlow<DistributorProfile> = _distributorProfile.asStateFlow()

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    init {
        syncProducersFromBackend()
    }

    fun syncProducersFromBackend() {
        scope.launch {
            runCatching {
                com.rebootech.fruitlogix.shared.core.network.RetrofitClient.api.getProducers()
            }.onSuccess { remoteList ->
                com.rebootech.fruitlogix.shared.core.network.RetrofitClient.updateSyncStatus(
                    com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus.ONLINE
                )
                if (remoteList.isNotEmpty()) {
                    val mapped = remoteList.mapIndexed { idx, dto ->
                        ProducerItem(
                            id = dto.id?.toString() ?: "API-${idx + 1}",
                            name = dto.name ?: "Productor API",
                            farmName = dto.rucOrDni?.let { "RUC/DNI: $it" } ?: "Fundo Registrado",
                            location = dto.location ?: "Perú",
                            cropType = dto.specialty ?: "Fruta de Exportación",
                            email = dto.email ?: "contacto@fruitlogix.pe",
                            phone = dto.phone ?: "+51 987 654 321",
                            entityType = if (dto.type.equals("Company", ignoreCase = true)) {
                                ProducerEntityType.COMPANY
                            } else {
                                ProducerEntityType.INDIVIDUAL
                            },
                            qualityRating = dto.rating ?: 4.8
                        )
                    }
                    _producers.value = mapped + _producers.value.filter { it.id.startsWith("PROD-") }
                }
            }.onFailure {
                com.rebootech.fruitlogix.shared.core.network.RetrofitClient.updateSyncStatus(
                    com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus.OFFLINE_FALLBACK
                )
            }
        }
    }

    override fun registerProducer(
        name: String,
        farmName: String,
        location: String,
        cropType: String,
        email: String,
        phone: String,
        entityType: ProducerEntityType
    ): Result<ProducerItem> {
        if (name.isBlank() || farmName.isBlank() || location.isBlank() || cropType.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos los campos obligatorios deben completarse."))
        }
        if (email.isNotBlank() && _producers.value.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("El correo ingresado ya pertenece a un productor activo en la red."))
        }

        val newItem = ProducerItem(
            id = "PROD-0${_producers.value.size + 1}",
            name = name.trim(),
            farmName = farmName.trim(),
            location = location.trim(),
            cropType = cropType.trim(),
            email = email.trim().ifBlank { "sin-correo@fruitlogix.pe" },
            phone = phone.trim().ifBlank { "+51 900 000 000" },
            entityType = entityType,
            qualityRating = 5.0,
            weeklyTons = 1.5,
            currentStockSummary = "${cropType.trim()}: 100kg (Inicial)",
            activeOrdersCount = 0,
            incidents30d = 0,
            isPending = false
        )
        _producers.value = listOf(newItem) + _producers.value
        scope.launch {
            runCatching {
                com.rebootech.fruitlogix.shared.core.network.RetrofitClient.api.createProducer(
                    com.rebootech.fruitlogix.shared.core.network.ProducerDto(
                        name = newItem.name,
                        rucOrDni = "20600000001",
                        type = newItem.entityType.backendValue,
                        location = newItem.location,
                        specialty = newItem.cropType,
                        email = newItem.email,
                        phone = newItem.phone
                    )
                )
            }
        }
        return Result.success(newItem)
    }

    override fun inviteProducer(
        name: String,
        emailOrPhone: String,
        location: String,
        cropType: String
    ): Result<ProducerItem> {
        val cleanContact = emailOrPhone.trim()
        val isEmail = cleanContact.contains("@") && cleanContact.contains(".")
        val isPhone = cleanContact.replace(" ", "").replace("+", "").all { it.isDigit() } && cleanContact.length >= 9

        if (!isEmail && !isPhone) {
            return Result.failure(
                IllegalArgumentException("Formato inválido. Ingresa un correo válido (ej. correo@dominio.com) o teléfono de al menos 9 dígitos.")
            )
        }

        val alreadyExists = _producers.value.any {
            it.email.equals(cleanContact, ignoreCase = true) ||
                    it.phone.replace(" ", "") == cleanContact.replace(" ", "")
        }
        if (alreadyExists) {
            return Result.failure(
                IllegalArgumentException("Este productor ya forma parte activa de la plataforma FruitLogix.")
            )
        }

        val invited = ProducerItem(
            id = "PROD-0${_producers.value.size + 1}",
            name = name.trim(),
            farmName = "Fundo por confirmar",
            location = location.trim().ifBlank { "Lima, Perú" },
            cropType = cropType.trim().ifBlank { "Fruta de estación" },
            email = if (isEmail) cleanContact else "pendiente@fruitlogix.pe",
            phone = if (isPhone) cleanContact else "+51 900 000 000",
            entityType = ProducerEntityType.INDIVIDUAL,
            qualityRating = 4.0,
            weeklyTons = 0.0,
            currentStockSummary = "Invitación enviada • Pendiente de activación",
            activeOrdersCount = 0,
            incidents30d = 0,
            isPending = true
        )
        _producers.value = listOf(invited) + _producers.value
        return Result.success(invited)
    }

    override fun updateProducer(updated: ProducerItem): Result<ProducerItem> {
        if (updated.name.isBlank() || updated.farmName.isBlank() || updated.location.isBlank() || updated.cropType.isBlank()) {
            return Result.failure(IllegalArgumentException("No puedes dejar campos vacíos en la ficha técnica del productor."))
        }
        _producers.value = _producers.value.map { if (it.id == updated.id) updated else it }
        return Result.success(updated)
    }

    override fun deleteProducer(producerId: String) {
        _producers.value = _producers.value.filterNot { it.id == producerId }
    }

    override fun updateDistributorProfile(profile: DistributorProfile): Result<DistributorProfile> {
        if (profile.fullName.isBlank() || !profile.email.contains("@") || profile.phone.length < 7 || profile.ruc.length != 11) {
            return Result.failure(
                IllegalArgumentException("Formato inválido: Verifica que el correo contenga '@', el teléfono sea válido y el RUC tenga 11 dígitos.")
            )
        }
        _distributorProfile.value = profile
        return Result.success(profile)
    }
}