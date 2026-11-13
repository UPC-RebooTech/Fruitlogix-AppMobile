package com.rebootech.fruitlogix.distributor.profilesManagement.data

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.shared.core.network.ProducerResourceDto
import com.rebootech.fruitlogix.shared.core.network.SaveProducerRequestDto

/**
 * Field mapping between the app and the server's ProducerResource:
 * name ↔ fullName, farm ↔ legalName, crop ↔ crop, RUC ↔ taxId,
 * location "Huaraz, Áncash" ↔ city "Huaraz" + region "Áncash" (country Perú).
 */
internal object ProducerMapper {

    private const val COUNTRY = "Perú"

    fun toProducer(dto: ProducerResourceDto): Producer? {
        val name = dto.fullName?.takeIf { it.isNotBlank() } ?: return null
        return Producer(
            id = "SRV-${dto.id}",
            name = name,
            farmName = dto.legalName.orEmpty(),
            location = listOf(dto.city, dto.region).filterNot { it.isNullOrBlank() }.joinToString(", "),
            cropType = dto.crop.orEmpty(),
            email = dto.email.orEmpty(),
            phone = dto.phone.orEmpty(),
            entityType = if (dto.producerType.equals("Individual", ignoreCase = true)) ProducerEntityType.INDIVIDUAL else ProducerEntityType.COMPANY,
            qualityRating = dto.rating ?: 0.0,
            // Volume, stock and orders are not part of the server's producer profile
            weeklyTons = 0.0,
            stockKg = 0,
            activeOrders = 0,
            incidents30d = 0,
            ruc = dto.taxId.orEmpty(),
            remoteId = dto.id
        )
    }

    /** @param current the server copy being edited, so fields the app does not show are kept. */
    fun toRequest(draft: ProducerDraft, current: ProducerResourceDto? = null): SaveProducerRequestDto {
        val (city, region) = splitLocation(draft.location)
        return SaveProducerRequestDto(
            producerType = draft.entityType.backendValue,
            fullName = draft.name.trim(),
            taxId = draft.ruc.trim(),
            legalName = draft.farmName.trim(),
            email = draft.email.trim(),
            phone = draft.phone.trim(),
            country = current?.country?.takeIf { it.isNotBlank() } ?: COUNTRY,
            region = region,
            city = city,
            address = current?.address.orEmpty(),
            crop = draft.cropType.trim(),
            cultivatedHectares = current?.cultivatedHectares ?: 0.0,
            monthlyProduction = current?.monthlyProduction.orEmpty(),
            operationsStartDate = current?.operationsStartDate,
            certifications = current?.certifications.orEmpty(),
            photo = current?.photo.orEmpty()
        )
    }

    /** "Huaraz, Áncash" → ("Huaraz", "Áncash"); "Ica" → ("Ica", ""). */
    fun splitLocation(location: String): Pair<String, String> {
        val parts = location.split(',', limit = 2).map { it.trim() }
        return parts[0] to parts.getOrElse(1) { "" }
    }
}
