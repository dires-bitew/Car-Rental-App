package com.example

import com.example.model.InsuranceTier
import com.example.model.VehicleCategory
import com.example.model.VehicleRentalStatus
import com.example.telematics.TelematicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `telematics engine initializes with vehicles and allows IoT door toggle`() {
        val engine = TelematicsEngine()
        val vehicles = engine.vehicles.value
        assertTrue("Fleet should have vehicles", vehicles.isNotEmpty())

        val vehicleId = vehicles.first().id
        val initialDoorsLocked = vehicles.first().doorsLocked
        engine.toggleDoors(vehicleId)

        val updatedVehicle = engine.vehicles.value.first { it.id == vehicleId }
        assertEquals(!initialDoorsLocked, updatedVehicle.doorsLocked)
    }

    @Test
    fun `booking changes vehicle status to rented active and creates booking record`() {
        val engine = TelematicsEngine()
        val availableVeh = engine.vehicles.value.firstOrNull { it.status == VehicleRentalStatus.AVAILABLE }
        assertNotNull(availableVeh)

        val booking = engine.bookVehicle(
            vehicleId = availableVeh!!.id,
            customerName = "Jordan Lee",
            customerEmail = "jordan@test.com",
            licenseNumber = "DL-CA99201",
            days = 3,
            insuranceTier = InsuranceTier.PREMIUM,
            childSeat = false,
            gpsDashcam = true,
            unlimitedMiles = true
        )
        assertNotNull(booking)
        assertEquals(VehicleRentalStatus.RENTED_ACTIVE, engine.vehicles.value.first { it.id == availableVeh.id }.status)
        assertTrue(engine.bookings.value.any { it.id == booking!!.id })
    }

    @Test
    fun `immobilizer activation cuts engine power and creates security alert`() {
        val engine = TelematicsEngine()
        val vehicle = engine.vehicles.value.first()
        
        // Start engine
        if (!vehicle.engineRunning) {
            engine.toggleEngine(vehicle.id)
        }
        
        // Engage immobilizer
        engine.toggleImmobilizer(vehicle.id)
        val immobilizedVehicle = engine.vehicles.value.first { it.id == vehicle.id }
        assertTrue(immobilizedVehicle.immobilizerActive)
        assertFalse(immobilizedVehicle.engineRunning)
        assertEquals(0f, immobilizedVehicle.currentSpeedMph)
        assertTrue(engine.alerts.value.any { it.type == com.example.model.AlertType.SECURITY_IMMOBILIZED })
    }

    @Test
    fun `climate pre-conditioning updates vehicle temperature`() {
        val engine = TelematicsEngine()
        val vehicle = engine.vehicles.value.first()
        engine.toggleClimate(vehicle.id, 19.5f)
        
        val updatedVehicle = engine.vehicles.value.first { it.id == vehicle.id }
        assertTrue(updatedVehicle.isClimateOn)
        assertEquals(19.5f, updatedVehicle.climateTempCelsius, 0.01f)
    }
}
