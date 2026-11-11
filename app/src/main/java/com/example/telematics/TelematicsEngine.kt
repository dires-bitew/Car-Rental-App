package com.example.telematics

import com.example.data.RouteWaypoint
import com.example.data.SampleVehicles
import com.example.data.SimulationRoute
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class TelematicsEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _vehicles = MutableStateFlow<List<Vehicle>>(SampleVehicles.getInitialFleet())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _bookings = MutableStateFlow<List<RentalBooking>>(
        listOf(
            RentalBooking(
                id = "book-001",
                vehicleId = "veh-001",
                vehicleName = "Aethereus Hyperion GT",
                customerName = "Alex Mercer",
                customerEmail = "alex.mercer@telematics.io",
                driverLicenseNumber = "DL-CA992819",
                totalRentalDays = 3,
                dailyRateUsd = 285.0,
                insuranceTier = InsuranceTier.PREMIUM,
                includeGpsDashcam = true,
                includeUnlimitedMiles = true,
                depositAmountUsd = 600.0,
                totalAmountUsd = 939.0,
                status = RentalStatus.ACTIVE,
                startOdometerMiles = 4100.0,
                startingFuelPercent = 95f
            )
        )
    )
    val bookings: StateFlow<List<RentalBooking>> = _bookings.asStateFlow()

    private val _activeTrip = MutableStateFlow<TripRecord?>(
        TripRecord(
            id = "trip-001",
            bookingId = "book-001",
            vehicleId = "veh-001",
            startTimestampMs = System.currentTimeMillis() - (18 * 60 * 1000), // 18 min ago
            distanceDrivenMiles = 20.5,
            currentDurationSec = 1080,
            maxSpeedMph = 68f,
            avgSpeedMph = 44.5f,
            energyUsedKwh = 5.2f,
            ecoScore = 96
        )
    )
    val activeTrip: StateFlow<TripRecord?> = _activeTrip.asStateFlow()

    private val _alerts = MutableStateFlow<List<TelematicsAlert>>(
        listOf(
            TelematicsAlert(
                id = "alert-001",
                vehicleId = "veh-005",
                vehicleName = "NeoPulse Volt Urban Pro",
                type = AlertType.TIRE_PRESSURE_WARNING,
                severity = AlertSeverity.WARNING,
                message = "Front-Left tire pressure dropped to 28.5 PSI (Optimal 34 PSI)."
            )
        )
    )
    val alerts: StateFlow<List<TelematicsAlert>> = _alerts.asStateFlow()

    // Simulation routing pointers
    private var currentWaypointIndex = 0
    private var subWaypointStep = 0.0f
    private var simulationJob: Job? = null

    // Real-time flash horn timer
    private var hornFlashJob: Job? = null

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (isActive) {
                delay(1200L) // 1.2s realistic tick
                tickSimulation()
            }
        }
    }

    private fun tickSimulation() {
        val waypoints = SimulationRoute.waypoints
        if (waypoints.isEmpty()) return

        val activeVehicleId = "veh-001" // Primary customer demo vehicle
        val currentVehicle = _vehicles.value.find { it.id == activeVehicleId } ?: return

        if (!currentVehicle.engineRunning || currentVehicle.status != VehicleRentalStatus.RENTED_ACTIVE || currentVehicle.immobilizerActive) {
            // Engine stopped or immobilized
            if (currentVehicle.currentSpeedMph > 0f) {
                updateVehicle(activeVehicleId) {
                    it.copy(currentSpeedMph = 0f, engineRpm = 0)
                }
            }
            return
        }

        // Advance waypoint interpolation
        subWaypointStep += 0.08f
        if (subWaypointStep >= 1.0f) {
            subWaypointStep = 0.0f
            currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size
        }

        val fromWp = waypoints[currentWaypointIndex]
        val toWp = waypoints[(currentWaypointIndex + 1) % waypoints.size]

        val lat = fromWp.point.latitude + (toWp.point.latitude - fromWp.point.latitude) * subWaypointStep
        val lng = fromWp.point.longitude + (toWp.point.longitude - fromWp.point.longitude) * subWaypointStep
        val heading = calculateBearing(fromWp.point.latitude, fromWp.point.longitude, toWp.point.latitude, toWp.point.longitude)

        // Dynamic speed with natural road fluctuation
        val targetSpeed = fromWp.speedLimitMph + sin(subWaypointStep * Math.PI.toFloat()) * 4f
        val currentSpeed = (targetSpeed).coerceIn(25f, 85f)
        val rpm = (currentSpeed * 65).toInt().coerceIn(1200, 5800)

        // Calculate distance added this tick (~0.015 miles)
        val milesAdded = (currentSpeed / 3600f) * 1.2f
        val newOdo = currentVehicle.odometerMiles + milesAdded

        // Battery drainage (approx 0.01% every tick when moving)
        val newBattery = (currentVehicle.batteryFuelPercent - 0.012f).coerceAtLeast(5f)
        val newMotorTemp = 68f + (currentSpeed / 100f) * 6f

        // Geofence check
        val distFromCenterMiles = calculateDistanceMiles(
            lat, lng,
            currentVehicle.geofenceCenter.latitude,
            currentVehicle.geofenceCenter.longitude
        )
        val isBreached = distFromCenterMiles > currentVehicle.geofenceRadiusMiles

        if (isBreached && !currentVehicle.isGeofenceBreached) {
            emitAlert(
                vehicleId = activeVehicleId,
                vehicleName = "${currentVehicle.make} ${currentVehicle.model}",
                type = AlertType.GEOFENCE_BREACH,
                severity = AlertSeverity.CRITICAL,
                message = "Vehicle crossed geofence boundary: ${String.format("%.1f", distFromCenterMiles)} mi from base."
            )
        }

        // Speed check alert (simulated occasional push above limit)
        if (currentSpeed > currentVehicle.speedLimitMph) {
            emitAlert(
                vehicleId = activeVehicleId,
                vehicleName = "${currentVehicle.make} ${currentVehicle.model}",
                type = AlertType.SPEED_EXCEEDED,
                severity = AlertSeverity.WARNING,
                message = "Speed advisory: Driving at ${currentSpeed.toInt()} mph in ${fromWp.speedLimitMph.toInt()} mph zone."
            )
        }

        val updatedLocation = GeoPoint(
            latitude = lat,
            longitude = lng,
            headingDeg = heading,
            streetName = fromWp.point.streetName
        )

        updateVehicle(activeVehicleId) { veh ->
            veh.copy(
                location = updatedLocation,
                currentSpeedMph = currentSpeed,
                engineRpm = rpm,
                odometerMiles = newOdo,
                batteryFuelPercent = newBattery,
                motorTempCelsius = newMotorTemp,
                isGeofenceBreached = isBreached
            )
        }

        // Update active trip
        _activeTrip.update { trip ->
            if (trip == null) null
            else {
                val newDist = trip.distanceDrivenMiles + milesAdded
                val newDuration = trip.currentDurationSec + 1
                val newMax = max(trip.maxSpeedMph, currentSpeed)
                val newAvg = if (newDuration > 0) ((newDist / (newDuration / 3600.0)).toFloat()).coerceIn(20f, 90f) else currentSpeed
                val newEnergy = trip.energyUsedKwh + (milesAdded * 0.28f).toFloat()
                trip.copy(
                    distanceDrivenMiles = newDist,
                    currentDurationSec = newDuration,
                    maxSpeedMph = newMax,
                    avgSpeedMph = newAvg,
                    energyUsedKwh = newEnergy
                )
            }
        }
    }

    // IoT Remote Controls
    fun toggleDoors(vehicleId: String) {
        updateVehicle(vehicleId) { veh ->
            veh.copy(doorsLocked = !veh.doorsLocked)
        }
    }

    fun toggleEngine(vehicleId: String) {
        updateVehicle(vehicleId) { veh ->
            val newEngine = !veh.engineRunning
            veh.copy(
                engineRunning = newEngine,
                currentSpeedMph = if (!newEngine) 0f else veh.currentSpeedMph,
                engineRpm = if (!newEngine) 0 else 1200
            )
        }
    }

    fun toggleClimate(vehicleId: String, targetTempCelsius: Float? = null) {
        updateVehicle(vehicleId) { veh ->
            val newClimate = if (targetTempCelsius != null) true else !veh.isClimateOn
            veh.copy(
                isClimateOn = newClimate,
                climateTempCelsius = targetTempCelsius ?: veh.climateTempCelsius
            )
        }
    }

    fun triggerHornAndFlash(vehicleId: String) {
        hornFlashJob?.cancel()
        updateVehicle(vehicleId) { it.copy(hazardLightsActive = true, headlightsOn = true) }
        hornFlashJob = scope.launch {
            delay(4000L)
            updateVehicle(vehicleId) { it.copy(hazardLightsActive = false) }
        }
    }

    fun toggleImmobilizer(vehicleId: String) {
        updateVehicle(vehicleId) { veh ->
            val newImmobilizer = !veh.immobilizerActive
            if (newImmobilizer) {
                emitAlert(
                    vehicleId = veh.id,
                    vehicleName = "${veh.make} ${veh.model}",
                    type = AlertType.SECURITY_IMMOBILIZED,
                    severity = AlertSeverity.CRITICAL,
                    message = "Security Emergency Stop: Remote vehicle powertrain cut-off activated."
                )
            }
            veh.copy(
                immobilizerActive = newImmobilizer,
                engineRunning = if (newImmobilizer) false else veh.engineRunning,
                currentSpeedMph = if (newImmobilizer) 0f else veh.currentSpeedMph
            )
        }
    }

    // Rental Lifecycle
    fun bookVehicle(
        vehicleId: String,
        customerName: String,
        customerEmail: String,
        licenseNumber: String,
        days: Int,
        insuranceTier: InsuranceTier,
        childSeat: Boolean,
        gpsDashcam: Boolean,
        unlimitedMiles: Boolean
    ): RentalBooking? {
        val vehicle = _vehicles.value.find { it.id == vehicleId } ?: return null
        val insuranceCost = insuranceTier.dailyPriceUsd * days
        val addOnsCost = (if (childSeat) 10.0 else 0.0) * days +
                (if (gpsDashcam) 12.0 else 0.0) * days +
                (if (unlimitedMiles) 15.0 else 0.0) * days
        val rentalCost = vehicle.dailyRateUsd * days
        val totalAmount = rentalCost + insuranceCost + addOnsCost + 45.0 // Tax & fees

        val booking = RentalBooking(
            id = "book-${System.currentTimeMillis() % 100000}",
            vehicleId = vehicle.id,
            vehicleName = "${vehicle.make} ${vehicle.model}",
            customerName = customerName,
            customerEmail = customerEmail,
            driverLicenseNumber = licenseNumber,
            totalRentalDays = days,
            dailyRateUsd = vehicle.dailyRateUsd,
            insuranceTier = insuranceTier,
            includeChildSeat = childSeat,
            includeGpsDashcam = gpsDashcam,
            includeUnlimitedMiles = unlimitedMiles,
            depositAmountUsd = vehicle.securityDepositUsd,
            totalAmountUsd = totalAmount,
            status = RentalStatus.ACTIVE,
            startOdometerMiles = vehicle.odometerMiles,
            startingFuelPercent = vehicle.batteryFuelPercent
        )

        _bookings.update { listOf(booking) + it }

        // Start active trip
        val trip = TripRecord(
            id = "trip-${System.currentTimeMillis() % 10000}",
            bookingId = booking.id,
            vehicleId = vehicle.id,
            startTimestampMs = System.currentTimeMillis(),
            distanceDrivenMiles = 0.0,
            currentDurationSec = 0,
            maxSpeedMph = 0f,
            avgSpeedMph = 0f,
            energyUsedKwh = 0f,
            ecoScore = 98
        )
        _activeTrip.value = trip

        // Update vehicle status
        updateVehicle(vehicleId) {
            it.copy(
                status = VehicleRentalStatus.RENTED_ACTIVE,
                doorsLocked = false,
                engineRunning = true
            )
        }

        return booking
    }

    fun completeReturn(
        bookingId: String,
        inspection: ReturnInspection
    ) {
        val booking = _bookings.value.find { it.id == bookingId } ?: return
        val vehicle = _vehicles.value.find { it.id == booking.vehicleId }

        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(
                    status = RentalStatus.COMPLETED,
                    returnInspection = inspection
                ) else it
            }
        }

        if (vehicle != null) {
            updateVehicle(vehicle.id) {
                it.copy(
                    status = VehicleRentalStatus.AVAILABLE,
                    engineRunning = false,
                    currentSpeedMph = 0f,
                    doorsLocked = true
                )
            }
        }

        _activeTrip.value = null
    }

    fun acknowledgeAlert(alertId: String) {
        _alerts.update { list ->
            list.map { if (it.id == alertId) it.copy(acknowledged = true) else it }
        }
    }

    private fun emitAlert(
        vehicleId: String,
        vehicleName: String,
        type: AlertType,
        severity: AlertSeverity,
        message: String
    ) {
        // Prevent duplicate spam for the same alert within 30 seconds
        val existing = _alerts.value.firstOrNull {
            it.vehicleId == vehicleId && it.type == type && (System.currentTimeMillis() - it.timestampMs < 30_000)
        }
        if (existing == null) {
            val alert = TelematicsAlert(
                id = "alert-${System.currentTimeMillis()}",
                vehicleId = vehicleId,
                vehicleName = vehicleName,
                type = type,
                severity = severity,
                message = message
            )
            _alerts.update { listOf(alert) + it }
        }
    }

    private fun updateVehicle(vehicleId: String, transform: (Vehicle) -> Vehicle) {
        _vehicles.update { list ->
            list.map { if (it.id == vehicleId) transform(it) else it }
        }
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return ((Math.toDegrees(theta) + 360.0) % 360.0).toFloat()
    }

    private fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3958.8 // Earth radius in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
