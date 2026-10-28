package com.quimia.android.presentation.screens.proximity

import com.quimia.android.data.auth.AuthAddress
import com.quimia.android.data.auth.AuthApiFactory
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class HomeMapAddressTest {
    private val address = AuthAddress("01001-000", "SP", "Sé", "Praça da Sé", 100)
    private val house = NearbyCoordinate(-23.55052, -46.63331)
    private val gps = NearbyCoordinate(-23.6, -46.7)

    @Test fun `API coordinates round trip without changing the registered address`() {
        val source = address.copy(latitude = house.latitude, longitude = house.longitude)
        val result = AuthApiFactory.json.decodeFromString<AuthAddress>(AuthApiFactory.json.encodeToString(source))
        assertEquals(house, result.homeCoordinate())
        assertEquals("Praça da Sé", result.rua)
        assertEquals(100, result.numero)
    }
    @Test fun `missing partial out of range or nonfinite coordinates never create a house marker`() {
        assertNull(address.homeCoordinate())
        assertNull(address.copy(latitude = 1.0).homeCoordinate())
        assertNull(address.copy(latitude = 91.0, longitude = 0.0).homeCoordinate())
        assertNull(address.copy(latitude = Double.NaN, longitude = 0.0).homeCoordinate())
    }
    @Test fun `home and GPS controls always select distinct sources`() {
        assertEquals(house, nearbyCameraCoordinate(NearbyMapFocus.Home, house, gps))
        assertEquals(gps, nearbyCameraCoordinate(NearbyMapFocus.User, house, gps))
        assertEquals(house, nearbyCameraCoordinate(NearbyMapFocus.Initial, house, gps))
        assertNull(nearbyCameraCoordinate(NearbyMapFocus.Manual, house, gps))
        assertNull(nearbyCameraCoordinate(NearbyMapFocus.Point, house, gps))
    }
    @Test fun `late GPS update cannot move camera away from home and GPS is never used as the house`() {
        assertEquals(house, nearbyCameraCoordinate(NearbyMapFocus.Home, house, gps.copy(latitude = -24.0)))
        assertNull(nearbyCameraCoordinate(NearbyMapFocus.Home, null, gps))
        assertNull(nearbyCameraCoordinate(NearbyMapFocus.User, house, null))
    }
    @Test fun `geocoding requires a Brazilian street address with matching CEP and house number`() {
        val match = HomeAddressMatch(house, "01001000", "100", "Praça da Sé", "BR")
        assertEquals(house, matchingHome(address, listOf(match)))
        listOf(match.copy(number = null), match.copy(number = "200"), match.copy(postalCode = "99999999"),
            match.copy(street = null), match.copy(country = "PT")).forEach {
            assertNull(matchingHome(address, listOf(it)))
        }
        assertTrue(address.homeQuery()!!.contains("100"))
        assertNull(address.copy(rua = null).homeQuery())
    }
}
