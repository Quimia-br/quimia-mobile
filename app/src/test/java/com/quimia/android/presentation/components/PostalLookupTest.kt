package com.quimia.android.presentation.components

import com.quimia.android.data.address.*
import org.junit.Assert.*
import org.junit.Test

class PostalLookupTest {
    private val validJson = """{"cep":"01001-000","logradouro":"Praça da Sé","bairro":"Sé","localidade":"São Paulo","uf":"SP","ibge":"3550308"}"""

    @Test fun decodesAllAddressFieldsWithoutHardcodedFallbacks() {
        val result = decodePostalAddress(validJson, "01001000") as PostalLookupResult.Found
        assertEquals(PostalAddress("01001000", "SP", "São Paulo", "Sé", "Praça da Sé"), result.address)
        assertTrue(postalLookupState("01001000", result).ready)
    }

    @Test fun notFoundMalformedAndMismatchedResponsesCannotEnableSaving() {
        assertEquals(PostalLookupResult.NotFound, decodePostalAddress("""{"erro":true}""", "99999999"))
        assertEquals(PostalLookupResult.NotFound, decodePostalAddress("""{"erro":"true"}""", "99999999"))
        listOf("not JSON", "{}", validJson.replace("SP", "XX")).forEach {
            assertEquals(PostalLookupResult.InvalidResponse, decodePostalAddress(it, "01001000"))
        }
        assertEquals(PostalLookupResult.InvalidResponse, decodePostalAddress(validJson, "12345678"))
        listOf(PostalLookupResult.NotFound, PostalLookupResult.Unavailable, PostalLookupResult.InvalidResponse).forEach {
            assertFalse(postalLookupState("01001000", it).ready)
            assertNull(postalLookupState("01001000", it).address)
        }
    }

    @Test fun aGeneralPostalCodeWithoutDistrictIsNotInventedOrManuallyCompleted() {
        val result = decodePostalAddress(validJson.replace("\"bairro\":\"Sé\"", "\"bairro\":\"\""), "01001000")
        val state = postalLookupState("01001000", result)
        assertEquals("São Paulo", state.address?.city)
        assertEquals("", state.address?.district)
        assertFalse(state.ready)
        assertNotNull(state.error)
    }

    @Test fun changingOrClearingPostalCodeImmediatelyRemovesTheOldAddress() {
        val previous = postalLookupState("01001000", decodePostalAddress(validJson, "01001000"))
        assertNull(currentPostalState("0100100", previous).address)
        assertNull(currentPostalState("", previous).address)
        val next = currentPostalState("04001000", previous)
        assertTrue(next.loading)
        assertNull(next.address)
        assertFalse(next.ready)
        assertEquals(previous, currentPostalState("01001000", previous))
    }

    @Test fun lookupDataMustBelongToTheCurrentPostalCode() {
        val address = PostalAddress("01001000", "SP", "São Paulo", "Sé", "Praça da Sé")
        assertFalse(postalLookupState("04001000", PostalLookupResult.Found(address)).ready)
    }

    @Test fun formattedPostalCodesAreNormalizedAndLimitedToEightDigits() {
        assertEquals("01001000", normalizedPostalCode("01001-000"))
        assertEquals("12345678", normalizedPostalCode("123456789"))
        assertEquals("", normalizedPostalCode("abc"))
    }
}
