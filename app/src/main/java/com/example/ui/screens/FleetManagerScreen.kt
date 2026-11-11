package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSeverity
import com.example.model.TelematicsAlert
import com.example.model.Vehicle
import com.example.model.VehicleRentalStatus
import com.example.ui.theme.*

@Composable
fun FleetManagerScreen(
    vehicles: List<Vehicle>,
    alerts: List<TelematicsAlert>,
    onAcknowledgeAlert: (String) -> Unit,
    onToggleImmobilizer: (String) -> Unit,
    onToggleDoors: (String) -> Unit,
    onLocateVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVehicles = vehicles.size
    val activeCount = vehicles.count { it.status == VehicleRentalStatus.RENTED_ACTIVE }
    val availableCount = vehicles.count { it.status == VehicleRentalStatus.AVAILABLE }
    val maintenanceCount = vehicles.count { it.status == VehicleRentalStatus.IN_MAINTENANCE }
    val unacknowledgedAlerts = alerts.filter { !it.acknowledged }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("fleet_manager_screen")
    ) {
        item {
            Text(
                text = "FLEET TELEMETRICS COMMAND",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark
            )
            Text(
                text = "Real-time automated IoT health, geofencing, and remote safety.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "ON ROAD",
                    count = "$activeCount",
                    color = ElectricBlue,
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "AVAILABLE",
                    count = "$availableCount",
                    color = AccentGreen,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "IN DEPOT",
                    count = "$maintenanceCount",
                    color = AccentAmber,
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "ALERTS",
                    count = "${unacknowledgedAlerts.size}",
                    color = if (unacknowledgedAlerts.isNotEmpty()) AccentRed else TextSecondaryDark,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-time Alerts Feed Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME ALERTS (${alerts.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextSecondaryDark
                )
                if (unacknowledgedAlerts.isNotEmpty()) {
                    Text(
                        text = "${unacknowledgedAlerts.size} pending",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (alerts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("All telematics systems operating within normal parameters.", color = AccentGreen)
                    }
                }
            }
        } else {
            items(alerts, key = { it.id }) { alert ->
                AlertItemCard(
                    alert = alert,
                    onAcknowledge = { onAcknowledgeAlert(alert.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "FLEET VEHICLE INVENTORY & TELEMETRY",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(vehicles, key = { it.id }) { vehicle ->
            FleetVehicleRow(
                vehicle = vehicle,
                onToggleImmobilizer = { onToggleImmobilizer(vehicle.id) },
                onToggleDoors = { onToggleDoors(vehicle.id) },
                onLocate = { onLocateVehicle(vehicle.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    count: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimaryDark
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AlertItemCard(
    alert: TelematicsAlert,
    onAcknowledge: () -> Unit
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> AccentRed
        AlertSeverity.WARNING -> AccentAmber
        AlertSeverity.INFO -> PrimaryCyan
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (alert.acknowledged) SurfaceDark else SurfaceContainerDark
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("alert_card_${alert.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(borderColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.type.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = borderColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = alert.severity.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = borderColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${alert.vehicleName}: ${alert.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            if (!alert.acknowledged) {
                TextButton(onClick = onAcknowledge) {
                    Text("Clear", color = PrimaryCyan, style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Text(
                    text = "Acknowledged",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryDark,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FleetVehicleRow(
    vehicle: Vehicle,
    onToggleImmobilizer: () -> Unit,
    onToggleDoors: () -> Unit,
    onLocate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("fleet_row_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${vehicle.make} ${vehicle.model}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Plate: ${vehicle.licensePlate} • Odo: ${vehicle.odometerMiles.toInt()} mi",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }

                // Speed / Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (vehicle.engineRunning) "${vehicle.currentSpeedMph.toInt()} MPH" else "PARKED",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (vehicle.engineRunning) PrimaryCyan else TextSecondaryDark
                    )
                    Text(
                        text = "Batt: ${vehicle.batteryFuelPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vehicle.batteryFuelPercent < 20f) AccentRed else AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick IoT actions for fleet manager
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLocate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track Map", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onToggleDoors,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(
                        if (vehicle.doorsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (vehicle.doorsLocked) "Unlock" else "Lock", fontSize = 12.sp)
                }

                Button(
                    onClick = onToggleImmobilizer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.immobilizerActive) AccentGreen else AccentRed.copy(alpha = 0.2f),
                        contentColor = if (vehicle.immobilizerActive) OnPrimaryDark else AccentRed
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (vehicle.immobilizerActive) "Release" else "Kill Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
