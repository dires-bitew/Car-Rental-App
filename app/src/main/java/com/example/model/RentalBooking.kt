package com.example.model

enum class InsuranceTier(val displayName: String, val dailyPriceUsd: Double, val deductibleUsd: Double, val description: String) {
    BASIC("Standard Protection", 15.0, 1000.0, "Third-party liability & standard damage coverage"),
    PREMIUM("Premium Shield", 28.0, 250.0, "Comprehensive collision, theft protection & glass cover"),
    ZERO_DEDUCTIBLE("Zero Risk Peace of Mind", 42.0, 0.0, "100% zero deductible, roadside assistance & tire coverage")
}

enum class RentalStatus(val label: String) {
    UPCOMING("Upcoming"),
    ACTIVE("Active Trip"),
    COMPLETED("Completed & Returned"),
    CANCELLED("Cancelled")
}

data class ReturnInspection(
    val bodyScratchesFound: Boolean = false,
    val interiorClean: Boolean = true,
    val keyFobReturned: Boolean = true,
    val returnFuelPercent: Float = 90f,
    val returnOdometerMiles: Double = 0.0,
    val inspectorNotes: String = "No damage detected. Excellent condition."
)

data class RentalBooking(
    val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val customerName: String,
    val customerEmail: String,
    val driverLicenseNumber: String,
    val startDayOffset: Int = 0,
    val totalRentalDays: Int = 3,
    val dailyRateUsd: Double,
    val insuranceTier: InsuranceTier = InsuranceTier.PREMIUM,
    val includeChildSeat: Boolean = false,
    val includeGpsDashcam: Boolean = true,
    val includeUnlimitedMiles: Boolean = true,
    val depositAmountUsd: Double = 500.0,
    val totalAmountUsd: Double,
    val bookingTimeEpochMs: Long = System.currentTimeMillis(),
    val status: RentalStatus = RentalStatus.ACTIVE,
    val startOdometerMiles: Double = 12450.0,
    val startingFuelPercent: Float = 92f,
    val returnInspection: ReturnInspection? = null
)
