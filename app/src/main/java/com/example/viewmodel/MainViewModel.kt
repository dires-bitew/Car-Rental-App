package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.telematics.TelematicsEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    FLEET_CATALOG("Fleet"),
    ACTIVE_MONITOR("Cockpit"),
    LIVE_MAP("Live Map"),
    FLEET_ADMIN("Fleet Hub")
}

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isAlert: Boolean = false
)

class MainViewModel(
    val telematicsEngine: TelematicsEngine = TelematicsEngine()
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AppTab.FLEET_CATALOG)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(VehicleCategory.ALL)
    val selectedCategory: StateFlow<VehicleCategory> = _selectedCategory.asStateFlow()

    private val _selectedVehicleId = MutableStateFlow<String?>("veh-001")
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    private val _isBookingDialogOpen = MutableStateFlow(false)
    val isBookingDialogOpen: StateFlow<Boolean> = _isBookingDialogOpen.asStateFlow()

    private val _isReturnDialogOpen = MutableStateFlow(false)
    val isReturnDialogOpen: StateFlow<Boolean> = _isReturnDialogOpen.asStateFlow()

    val vehicles: StateFlow<List<Vehicle>> = telematicsEngine.vehicles
    val bookings: StateFlow<List<RentalBooking>> = telematicsEngine.bookings
    val activeTrip: StateFlow<TripRecord?> = telematicsEngine.activeTrip
    val alerts: StateFlow<List<TelematicsAlert>> = telematicsEngine.alerts

    val unacknowledgedAlertsCount: StateFlow<Int> = alerts.map { list ->
        list.count { !it.acknowledged }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeCustomerBooking: StateFlow<RentalBooking?> = bookings.map { list ->
        list.firstOrNull { it.status == RentalStatus.ACTIVE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCustomerVehicle: StateFlow<Vehicle?> = combine(vehicles, activeCustomerBooking) { list, booking ->
        if (booking != null) {
            list.find { it.id == booking.vehicleId }
        } else {
            list.firstOrNull { it.status == VehicleRentalStatus.RENTED_ACTIVE } ?: list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredVehicles: StateFlow<List<Vehicle>> = combine(vehicles, searchQuery, selectedCategory) { list, query, cat ->
        list.filter { veh ->
            val matchesCategory = cat == VehicleCategory.ALL || veh.category == cat
            val matchesQuery = query.isBlank() ||
                    veh.make.contains(query, ignoreCase = true) ||
                    veh.model.contains(query, ignoreCase = true) ||
                    veh.licensePlate.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedVehicle: StateFlow<Vehicle?> = combine(vehicles, selectedVehicleId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: VehicleCategory) {
        _selectedCategory.value = category
    }

    fun selectVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }

    fun openBookingDialog(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
        _isBookingDialogOpen.value = true
    }

    fun closeBookingDialog() {
        _isBookingDialogOpen.value = false
    }

    fun openReturnDialog() {
        _isReturnDialogOpen.value = true
    }

    fun closeReturnDialog() {
        _isReturnDialogOpen.value = false
    }

    fun showNotification(message: String, isAlert: Boolean = false) {
        _notification.value = UiNotification(message = message, isAlert = isAlert)
    }

    fun dismissNotification() {
        _notification.value = null
    }

    // IoT Actions with user feedback
    fun toggleDoors(vehicleId: String) {
        val veh = vehicles.value.find { it.id == vehicleId } ?: return
        telematicsEngine.toggleDoors(vehicleId)
        val state = if (veh.doorsLocked) "Unlocked" else "Locked"
        showNotification("${veh.make} ${veh.model} Doors $state")
    }

    fun toggleEngine(vehicleId: String) {
        val veh = vehicles.value.find { it.id == vehicleId } ?: return
        if (veh.immobilizerActive) {
            showNotification("Cannot start engine: Powertrain immobilized by security!", isAlert = true)
            return
        }
        telematicsEngine.toggleEngine(vehicleId)
        val state = if (veh.engineRunning) "Stopped" else "Started (Ready to Drive)"
        showNotification("Engine $state")
    }

    fun toggleClimate(vehicleId: String, temp: Float? = null) {
        val veh = vehicles.value.find { it.id == vehicleId } ?: return
        telematicsEngine.toggleClimate(vehicleId, temp)
        val msg = if (temp != null) "Climate set to ${temp.toInt()}°C" else if (!veh.isClimateOn) "Pre-conditioning AC On" else "Climate Off"
        showNotification(msg)
    }

    fun triggerHornFlash(vehicleId: String) {
        val veh = vehicles.value.find { it.id == vehicleId } ?: return
        telematicsEngine.triggerHornAndFlash(vehicleId)
        showNotification("Beep! Hazard lights flashing & horn sounded.")
    }

    fun toggleImmobilizer(vehicleId: String) {
        val veh = vehicles.value.find { it.id == vehicleId } ?: return
        telematicsEngine.toggleImmobilizer(vehicleId)
        val state = if (veh.immobilizerActive) "Deactivated (Vehicle Released)" else "ACTIVATED: Remote Kill Switch Engaged"
        showNotification("Security Immobilizer $state", isAlert = !veh.immobilizerActive)
    }

    fun confirmBooking(
        vehicleId: String,
        name: String,
        email: String,
        license: String,
        days: Int,
        insurance: InsuranceTier,
        childSeat: Boolean,
        gps: Boolean,
        unlimited: Boolean
    ) {
        val booking = telematicsEngine.bookVehicle(
            vehicleId = vehicleId,
            customerName = name,
            customerEmail = email,
            licenseNumber = license,
            days = days,
            insuranceTier = insurance,
            childSeat = childSeat,
            gpsDashcam = gps,
            unlimitedMiles = unlimited
        )
        if (booking != null) {
            _isBookingDialogOpen.value = false
            _selectedTab.value = AppTab.ACTIVE_MONITOR
            showNotification("Booking Confirmed! Smart key assigned to ${booking.vehicleName}")
        }
    }

    fun submitReturn(
        bookingId: String,
        scratches: Boolean,
        interiorClean: Boolean,
        fobReturned: Boolean,
        notes: String
    ) {
        val inspection = ReturnInspection(
            bodyScratchesFound = scratches,
            interiorClean = interiorClean,
            keyFobReturned = fobReturned,
            inspectorNotes = notes.ifBlank { "Vehicle inspected and accepted into depot." }
        )
        telematicsEngine.completeReturn(bookingId, inspection)
        _isReturnDialogOpen.value = false
        _selectedTab.value = AppTab.FLEET_CATALOG
        showNotification("Vehicle Return Completed! Security deposit refunded.")
    }

    fun acknowledgeAlert(alertId: String) {
        telematicsEngine.acknowledgeAlert(alertId)
    }
}
