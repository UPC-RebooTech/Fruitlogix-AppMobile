package com.rebootech.fruitlogix.auth

import com.rebootech.fruitlogix.auth.data.RemoteAuthRepository
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.shared.core.network.ApiErrors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Rules from CAMBIOS-API.md: roles of this app and the error body the app shows. */
class BackendContractTest {

    @Test
    fun onlyProducerAndDistributor_openThisApp() {
        assertEquals(UserRole.PRODUCER, RemoteAuthRepository.roleFromServer("Producer"))
        assertEquals(UserRole.DISTRIBUTOR, RemoteAuthRepository.roleFromServer("distributor"))
        assertNull(RemoteAuthRepository.roleFromServer("CommercialClient"))
        assertNull(RemoteAuthRepository.roleFromServer("Admin"))
        assertNull(RemoteAuthRepository.roleFromServer(null))
    }

    @Test
    fun signUp_sendsTheRoleAsTheServerSpellsIt() {
        assertEquals("Producer", RemoteAuthRepository.roleForServer(UserRole.PRODUCER))
        assertEquals("Distributor", RemoteAuthRepository.roleForServer(UserRole.DISTRIBUTOR))
    }

    @Test
    fun errorBody_messageThenDetailThenValidationThenTitle() {
        assertEquals("Stock insuficiente", ApiErrors.parseMessage("""{"message":"Stock insuficiente"}"""))
        assertEquals("Not allowed", ApiErrors.parseMessage("""{"title":"Forbidden","detail":"Not allowed"}"""))
        assertEquals(
            "The Reason field is required.",
            ApiErrors.parseMessage("""{"title":"One or more validation errors occurred.","errors":{"Reason":["The Reason field is required."]}}""")
        )
        assertEquals("Conflict", ApiErrors.parseMessage("""{"title":"Conflict","traceId":"00-abc"}"""))
        assertEquals("Producer profile already exists", ApiErrors.parseMessage("\"Producer profile already exists\""))
    }

    @Test
    fun errorBody_emptyOrNotJson_hasNoMessage() {
        assertNull(ApiErrors.parseMessage(null))
        assertNull(ApiErrors.parseMessage(""))
        assertNull(ApiErrors.parseMessage("""{"message":""}"""))
    }
}
