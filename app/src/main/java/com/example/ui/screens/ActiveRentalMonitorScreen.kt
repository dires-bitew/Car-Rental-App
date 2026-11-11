package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RentalBooking
import com.example.model.TripRecord
import com.example.model.Vehicle
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.TirePressureWidget
import com.example.ui.theme.*

@Composable
fun ActiveRentalMonitorScreen(
    vehicle: Vehicle?,
    booking: RentalBooking?,
    trip: TripRecord?,
    onToggleDoors: (String) -> Unit,
    onToggleEngine: (String) -> Unit,
    onToggleClimate: (String, Float?) -> Unit,
    onTriggerHornFlash: (String) -> Unit,
    onToggleImmobilizer: (String) -> Unit,
    onOpenReturnDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (vehicle == null) {
        Box(
            modifier = modifier.fillMaxSize().background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Rental Session",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Explore the Fleet tab to book a luxury or electric vehicle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark
                )
            }
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("active_rental_monitor_screen")
    ) {
        // Vehicle Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${vehicle.make} ${vehicle.model}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryCyanDark.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = vehicle.category.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VIN: ${vehicle.vin} • PLATE: ${vehicle.licensePlate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }

                // Live Connected Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (vehicle.engineRunning) AccentGreen.copy(alpha = 0.15f) else SurfaceContainerDark
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (vehicle.engineRunning) AccentGreen else AccentAmber,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (vehicle.engineRunning) "ONLINE" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (vehicle.engineRunning) AccentGreen else TextSecondaryDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Central Speedometer & Live Telemetry Cockpit
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpeedometerGauge(
                    currentSpeedMph = vehicle.currentSpeedMph,
                    speedLimitMph = vehicle.speedLimitMph
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Telemetry Triple Meters: RPM, Battery Range, Motor Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricPill(
                        label = "POWERTRAIN",
                        value = "${vehicle.engineRpm}",
                        unit = "RPM",
                        icon = Icons.Default.Speed,
                        tint = PrimaryCyan
                    )

                    val remainingRange = ((vehicle.batteryFuelPercent / 100f) * vehicle.specs.rangeMiles).toInt()
                    MetricPill(
                        label = if (vehicle.specs.isElectric) "BATTERY" else "FUEL",
                        value = "${vehicle.batteryFuelPercent.toInt()}%",
                        unit = "~$remainingRange mi",
                        icon = Icons.Default.BatteryChargingFull,
                        tint = if (vehicle.batteryFuelPercent < 20f) AccentRed else AccentGreen
                    )

                    MetricPill(
                        label = "MOTOR TEMP",
                        value = "${vehicle.motorTempCelsius.toInt()}",
                        unit = "°C",
                        icon = Icons.Default.Thermostat,
                        tint = if (vehicle.motorTempCelsius > 90f) AccentRed else PrimaryCyanDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // IoT Remote Vehicle Control Center
        Text(
            text = "REMOTE VEHICLE CONTROLS",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = TextSecondaryDark,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Primary Control Buttons (Lock, Engine, Horn, Climate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Lock / Unlock Doors
                    ControlButton(
                        title = if (vehicle.doorsLocked) "Unlock" else "Lock",
                        subtitle = if (vehicle.doorsLocked) "Locked" else "Unlocked",
                        icon = if (vehicle.doorsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        isActive = !vehicle.doorsLocked,
                        activeColor = AccentAmber,
                        onClick = { onToggleDoors(vehicle.id) },
                        testTag = "btn_toggle_doors"
                    )

                    // Engine Start / Stop
                    ControlButton(
                        title = if (vehicle.engineRunning) "Stop" else "Start",
                        subtitle = if (vehicle.engineRunning) "Running" else "Standby",
                        icon = Icons.Default.PowerSettingsNew,
                        isActive = vehicle.engineRunning,
                        activeColor = AccentGreen,
                        onClick = { onToggleEngine(vehicle.id) },
                        testTag = "btn_toggle_engine"
                    )

                    // Flash Lights & Horn
                    ControlButton(
                        title = "Beep / Flash",
                        subtitle = if (vehicle.hazardLightsActive) "Active" else "Locate",
                        icon = Icons.Default.FlashOn,
                        isActive = vehicle.hazardLightsActive,
                        activeColor = PrimaryCyan,
                        onClick = { onTriggerHornFlash(vehicle.id) },
                        testTag = "btn_horn_flash"
                    )

                    // Immobilizer Kill Switch
                    ControlButton(
                        title = if (vehicle.immobilizerActive) "Release" else "Immobilize",
                        subtitle = if (vehicle.immobilizerActive) "CUT-OFF" else "Armed",
                        icon = Icons.Default.Security,
                        isActive = vehicle.immobilizerActive,
                        activeColor = AccentRed,
                        onClick = { onToggleImmobilizer(vehicle.id) },
                        testTag = "btn_immobilizer"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Climate Control Slider Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = if (vehicle.isClimateOn) PrimaryCyan else TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CLIMATE PRE-CONDITIONING",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondaryDark
                                )
                                Text(
                                    text = if (vehicle.isClimateOn) "Target ${vehicle.climateTempCelsius.toInt()}°C (Active)" else "Off",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (vehicle.isClimateOn) PrimaryCyan else TextPrimaryDark
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleClimate(vehicle.id, (vehicle.climateTempCelsius - 1f).coerceAtLeast(16f)) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.titleLarge, color = PrimaryCyan)
                            }
                            Switch(
                                checked = vehicle.isClimateOn,
                                onCheckedChange = { onToggleClimate(vehicle.id, null) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrimaryCyan,
                                    checkedTrackColor = ElectricBlueContainer
                                )
                            )
                            IconButton(
                                onClick = { onToggleClimate(vehicle.id, (vehicle.climateTempCelsius + 1f).coerceAtMost(28f)) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge, color = PrimaryCyan)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tire Pressure Monitoring System (TPMS)
        TirePressureWidget(tirePressure = vehicle.tirePressure)

        Spacer(modifier = Modifier.height(14.dp))

        // Active Trip Live Analytics Card
        if (trip != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE TRIP TELEMETRY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TextSecondaryDark
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ECO SCORE ${trip.ecoScore}/100",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TripStatItem(
                            label = "DISTANCE",
                            value = "${String.format("%.1f", trip.distanceDrivenMiles)} mi"
                        )
                        val mins = trip.currentDurationSec / 60
                        val secs = trip.currentDurationSec % 60
                        TripStatItem(
                            label = "DURATION",
                            value = "${mins}m ${secs}s"
                        )
                        TripStatItem(
                            label = "AVG SPEED",
                            value = "${trip.avgSpeedMph.toInt()} mph"
                        )
                        TripStatItem(
                            label = "MAX SPEED",
                            value = "${trip.maxSpeedMph.toInt()} mph"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // End Rental & Vehicle Return Action Button
        Button(
            onClick = onOpenReturnDialog,
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlueContainer,
                contentColor = PrimaryCyan
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_end_rental_dialog")
        ) {
            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "End Rental & Return Vehicle",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark
            )
        }
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextSecondaryDark
        )
    }
}

@Composable
fun ControlButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.2f) else SurfaceContainerDark)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else TextSecondaryDark,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryDark
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = if (isActive) activeColor else TextTertiaryDark
        )
    }
}

@Composable
fun TripStatItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondaryDark)
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimaryDark)
    }
}
