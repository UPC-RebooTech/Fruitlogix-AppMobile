package com.rebootech.fruitlogix.profilesManagement

import com.rebootech.fruitlogix.distributor.profilesManagement.data.ProducerMapper
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.shared.core.network.ProducerResourceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProducerMapperTest {

    private val serverProducer = ProducerResourceDto(
        id = 7, producerType = "Company", fullName = "Fundo Santa Rosa", taxId = "20481234561",
        legalName = "Agrícola Santa Rosa SAC", email = "contacto@fundosantarosa.pe", phone = "+51 956 410 228",
        country = "Perú", region = "Áncash", city = "Huaraz", address = "Av. Centenario 120",
        crop = "Palta Hass", cultivatedHectares = 12.5, monthlyProduction = "18 t", rating = 4.6
    )

    @Test
    fun serverProducer_isReadWithTheAppFields() {
        val producer = ProducerMapper.toProducer(serverProducer)!!
        assertEquals("SRV-7", producer.id)
        assertEquals(7, producer.remoteId)
        assertEquals("Fundo Santa Rosa", producer.name)
        assertEquals("Agrícola Santa Rosa SAC", producer.farmName)
        assertEquals("Huaraz, Áncash", producer.location)
        assertEquals("20481234561", producer.ruc)
        assertEquals(ProducerEntityType.COMPANY, producer.entityType)
        assertEquals(4.6, producer.qualityRating, 0.0)
    }

    @Test
    fun serverProducer_withoutName_isSkipped() {
        assertNull(ProducerMapper.toProducer(serverProducer.copy(fullName = " ")))
    }

    @Test
    fun edit_keepsTheFieldsTheAppDoesNotShow() {
        val draft = ProducerDraft(
            name = "Fundo Santa Rosa", farmName = "Agrícola Santa Rosa SAC", location = "Caraz, Áncash",
            cropType = "Palta Fuerte", email = "ventas@fundosantarosa.pe", phone = "+51 956 410 229",
            entityType = ProducerEntityType.COMPANY, ruc = "20481234561"
        )
        val request = ProducerMapper.toRequest(draft, current = serverProducer)
        assertEquals("Company", request.producerType)
        assertEquals("Caraz", request.city)
        assertEquals("Áncash", request.region)
        assertEquals("Palta Fuerte", request.crop)
        assertEquals("Av. Centenario 120", request.address)
        assertEquals(12.5, request.cultivatedHectares, 0.0)
        assertEquals("18 t", request.monthlyProduction)
    }

    @Test
    fun location_withoutRegion() {
        assertEquals("Ica" to "", ProducerMapper.splitLocation("Ica"))
        assertEquals("Huaral" to "Lima", ProducerMapper.splitLocation(" Huaral ,  Lima "))
    }
}
