package com.example.model

enum class VehicleCategory(val label: String) {
    ALL("All"),
    ELECTRIC_HYPERCAR("Hypercar"),
    LUXURY_SUV("Luxury SUV"),
    EXECUTIVE_SEDAN("Executive"),
    SPORT_COUPE("Sport Coupe"),
    URBAN_EV("Urban EV")
}

enum class VehicleRentalStatus(val label: String) {
    AVAILABLE("Available"),
    RENTED_ACTIVE("In Rental"),
    IN_MAINTENANCE("Maintenance"),
    RESERVED("Reserved")
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val headingDeg: Float = 0f,
    val streetName: String = ""
)

data class TirePressure(
    val frontLeftPsi: Float = 34.0f,
    val frontRightPsi: Float = 34.2f,
    val rearLeftPsi: Float = 33.8f,
    val rearRightPsi: Float = 34.1f
) {
    val isAnyLow: Boolean
        get() = frontLeftPsi < 30f || frontRightPsi < 30f || rearLeftPsi < 30f || rearRightPsi < 30f
}

data class VehicleSpecs(
    val horsepower: Int,
    val acceleration0to60: Double,
    val topSpeedMph: Int,
    val rangeMiles: Int,
    val seats: Int,
    val transmission: String = "Automatic",
    val driveType: String = "AWD",
    val isElectric: Boolean = true
)

data class Vehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val category: VehicleCategory,
    val licensePlate: String,
    val vin: String,
    val dailyRateUsd: Double,
    val securityDepositUsd: Double,
    val specs: VehicleSpecs,
    val drawableResName: String,
    val location: GeoPoint,
    val status: VehicleRentalStatus = VehicleRentalStatus.AVAILABLE,
    
    // Live Telematics Data
    val currentSpeedMph: Float = 0f,
    val engineRpm: Int = 0,
    val batteryFuelPercent: Float = 92f,
    val motorTempCelsius: Float = 72f,
    val tirePressure: TirePressure = TirePressure(),
    val odometerMiles: Double = 12450.0,
    val doorsLocked: Boolean = true,
    val engineRunning: Boolean = false,
    val isClimateOn: Boolean = false,
    val climateTempCelsius: Float = 21.5f,
    val headlightsOn: Boolean = false,
    val hazardLightsActive: Boolean = false,
    val immobilizerActive: Boolean = false,
    
    // Geofencing & Safety
    val geofenceCenter: GeoPoint = GeoPoint(37.7749, -122.4194),
    val geofenceRadiusMiles: Double = 15.0,
    val isGeofenceBreached: Boolean = false,
    val speedLimitMph: Float = 80f,
    val activeTripId: String? = null
)
