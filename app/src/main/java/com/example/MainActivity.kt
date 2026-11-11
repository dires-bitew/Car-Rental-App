package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoPulseTheme {
                AutoPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoPulseApp(viewModel: MainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredVehicles by viewModel.filteredVehicles.collectAsStateWithLifecycle()
    val allVehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val activeCustomerVehicle by viewModel.activeCustomerVehicle.collectAsStateWithLifecycle()
    val selectedVehicle by viewModel.selectedVehicle.collectAsStateWithLifecycle()
    val activeBooking by viewModel.activeCustomerBooking.collectAsStateWithLifecycle()
    val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val unackAlertsCount by viewModel.unacknowledgedAlertsCount.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val isBookingOpen by viewModel.isBookingDialogOpen.collectAsStateWithLifecycle()
    val isReturnOpen by viewModel.isReturnDialogOpen.collectAsStateWithLifecycle()

    // Handle back button: return to Fleet Catalog if on another tab
    BackHandler(enabled = selectedTab != AppTab.FLEET_CATALOG) {
        viewModel.selectTab(AppTab.FLEET_CATALOG)
    }

    // Auto-dismiss in-app notification banner
    LaunchedEffect(notification) {
        if (notification != null) {
            kotlinx.coroutines.delay(3500L)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("autopulse_scaffold"),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryCyanDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = "Logo",
                                tint = OnPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AutoPulse",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextPrimaryDark
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(AccentGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "IoT Telemetry Live",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick alert notification icon
                    IconButton(
                        onClick = { viewModel.selectTab(AppTab.FLEET_ADMIN) },
                        modifier = Modifier.testTag("top_alerts_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unackAlertsCount > 0) {
                                    Badge(
                                        containerColor = AccentRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unackAlertsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = if (unackAlertsCount > 0) AccentAmber else TextSecondaryDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimaryDark
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Fleet Catalog Tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.FLEET_CATALOG,
                    onClick = { viewModel.selectTab(AppTab.FLEET_CATALOG) },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Fleet") },
                    label = { Text("Fleet") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryCyan,
                        selectedTextColor = PrimaryCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextSecondaryDark,
                        indicatorColor = ElectricBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_fleet")
                )

                // Active Cockpit Tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.ACTIVE_MONITOR,
                    onClick = { viewModel.selectTab(AppTab.ACTIVE_MONITOR) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Cockpit") },
                    label = { Text("Cockpit") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryCyan,
                        selectedTextColor = PrimaryCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextSecondaryDark,
                        indicatorColor = ElectricBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_cockpit")
                )

                // Live Map Tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.LIVE_MAP,
                    onClick = { viewModel.selectTab(AppTab.LIVE_MAP) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
                    label = { Text("Live Map") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryCyan,
                        selectedTextColor = PrimaryCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextSecondaryDark,
                        indicatorColor = ElectricBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_map")
                )

                // Fleet Admin Hub Tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.FLEET_ADMIN,
                    onClick = { viewModel.selectTab(AppTab.FLEET_ADMIN) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unackAlertsCount > 0) {
                                    Badge(
                                        containerColor = AccentRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unackAlertsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Fleet Hub")
                        }
                    },
                    label = { Text("Fleet Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryCyan,
                        selectedTextColor = PrimaryCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextSecondaryDark,
                        indicatorColor = ElectricBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_hub")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.FLEET_CATALOG -> {
                    FleetCatalogScreen(
                        vehicles = filteredVehicles,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectVehicle = { id ->
                            viewModel.selectVehicle(id)
                            viewModel.selectTab(AppTab.ACTIVE_MONITOR)
                        },
                        onBookVehicle = { id ->
                            viewModel.openBookingDialog(id)
                        },
                        onMonitorVehicle = { id ->
                            viewModel.selectVehicle(id)
                            viewModel.selectTab(AppTab.ACTIVE_MONITOR)
                        }
                    )
                }

                AppTab.ACTIVE_MONITOR -> {
                    ActiveRentalMonitorScreen(
                        vehicle = activeCustomerVehicle,
                        booking = activeBooking,
                        trip = activeTrip,
                        onToggleDoors = { viewModel.toggleDoors(it) },
                        onToggleEngine = { viewModel.toggleEngine(it) },
                        onToggleClimate = { id, temp -> viewModel.toggleClimate(id, temp) },
                        onTriggerHornFlash = { viewModel.triggerHornFlash(it) },
                        onToggleImmobilizer = { viewModel.toggleImmobilizer(it) },
                        onOpenReturnDialog = { viewModel.openReturnDialog() }
                    )
                }

                AppTab.LIVE_MAP -> {
                    LiveMapTrackingScreen(
                        activeVehicle = activeCustomerVehicle,
                        allVehicles = allVehicles,
                        onSelectVehicle = { id -> viewModel.selectVehicle(id) }
                    )
                }

                AppTab.FLEET_ADMIN -> {
                    FleetManagerScreen(
                        vehicles = allVehicles,
                        alerts = alerts,
                        onAcknowledgeAlert = { viewModel.acknowledgeAlert(it) },
                        onToggleImmobilizer = { viewModel.toggleImmobilizer(it) },
                        onToggleDoors = { viewModel.toggleDoors(it) },
                        onLocateVehicle = { id ->
                            viewModel.selectVehicle(id)
                            viewModel.selectTab(AppTab.LIVE_MAP)
                        }
                    )
                }
            }

            // In-app Notification Banner
            AnimatedVisibility(
                visible = notification != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                notification?.let { notif ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (notif.isAlert) AccentRed else ElectricBlueContainer,
                        tonalElevation = 10.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (notif.isAlert) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (notif.isAlert) Color.White else PrimaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Booking Flow Dialog
            if (isBookingOpen && selectedVehicle != null) {
                BookingFlowDialog(
                    vehicle = selectedVehicle!!,
                    onDismiss = { viewModel.closeBookingDialog() },
                    onConfirm = { name, email, license, days, insurance, child, gps, unl ->
                        viewModel.confirmBooking(
                            vehicleId = selectedVehicle!!.id,
                            name = name,
                            email = email,
                            license = license,
                            days = days,
                            insurance = insurance,
                            childSeat = child,
                            gps = gps,
                            unlimited = unl
                        )
                    }
                )
            }

            // Vehicle Return & Inspection Dialog
            if (isReturnOpen && activeBooking != null && activeCustomerVehicle != null) {
                ReturnInspectionDialog(
                    booking = activeBooking,
                    vehicle = activeCustomerVehicle,
                    onDismiss = { viewModel.closeReturnDialog() },
                    onSubmitReturn = { bookingId, scratches, clean, fob, notes ->
                        viewModel.submitReturn(bookingId, scratches, clean, fob, notes)
                    }
                )
            }
        }
    }
}
