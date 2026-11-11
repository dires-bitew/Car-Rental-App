package com.example.model

enum class AlertType(val title: String) {
    SPEED_EXCEEDED("Overspeed Alert"),
    GEOFENCE_BREACH("Geofence Boundary Exceeded"),
    LOW_BATTERY("Low Energy Warning"),
    TIRE_PRESSURE_WARNING("Tire Pressure Alert"),
    HARSH_BRAKING("Harsh Braking Event"),
    SECURITY_IMMOBILIZED("Engine Immobilized (Security)")
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class TelematicsAlert(
    val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val acknowledged: Boolean = false
)

data class TripRecord(
    val id: String,
    val bookingId: String,
    val vehicleId: String,
    val startTimestampMs: Long,
    val endTimestampMs: Long? = null,
    val distanceDrivenMiles: Double = 0.0,
    val currentDurationSec: Long = 0,
    val maxSpeedMph: Float = 0f,
    val avgSpeedMph: Float = 0f,
    val energyUsedKwh: Float = 0f,
    val ecoScore: Int = 94,
    val waypoints: List<GeoPoint> = emptyList()
)
