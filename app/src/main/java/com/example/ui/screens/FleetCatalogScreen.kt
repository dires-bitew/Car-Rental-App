package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleCategory
import com.example.model.VehicleRentalStatus
import com.example.ui.theme.*

@Composable
fun FleetCatalogScreen(
    vehicles: List<Vehicle>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: VehicleCategory,
    onSelectCategory: (VehicleCategory) -> Unit,
    onSelectVehicle: (String) -> Unit,
    onBookVehicle: (String) -> Unit,
    onMonitorVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("fleet_catalog_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("hero_banner_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val heroDrawableId = context.resources.getIdentifier(
                        "img_hero_car",
                        "drawable",
                        context.packageName
                    )
                    if (heroDrawableId != 0) {
                        Image(
                            painter = painterResource(id = heroDrawableId),
                            contentDescription = "Fleet Hero",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SurfaceDark.copy(alpha = 0.5f),
                                        SurfaceDark.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryCyanDark.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "REAL-TIME FLEET TELEMETRY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next-Gen Luxury & EV Rentals",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Live IoT tracking, remote digital keys, and automated check-in.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                placeholder = { Text("Search make, model, or plate...", color = TextSecondaryDark) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondaryDark)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = SurfaceContainerDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VehicleCategory.entries.toTypedArray()) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceDark,
                            selectedContainerColor = ElectricBlueContainer,
                            labelColor = TextSecondaryDark,
                            selectedLabelColor = PrimaryCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SurfaceContainerHighDark,
                            selectedBorderColor = PrimaryCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("chip_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AVAILABLE VEHICLES (${vehicles.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Vehicles List
        items(vehicles, key = { it.id }) { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onSelect = { onSelectVehicle(vehicle.id) },
                onBook = { onBookVehicle(vehicle.id) },
                onMonitor = { onMonitorVehicle(vehicle.id) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onSelect: () -> Unit,
    onBook: () -> Unit,
    onMonitor: () -> Unit
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("vehicle_card_${vehicle.id}")
    ) {
        Column {
            // Vehicle Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val drawableId = context.resources.getIdentifier(
                    vehicle.drawableResName,
                    "drawable",
                    context.packageName
                )
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = "${vehicle.make} ${vehicle.model}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceContainerDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.85f))
                            )
                        )
                )

                // Status Badge in top right
                val statusColor = when (vehicle.status) {
                    VehicleRentalStatus.AVAILABLE -> AccentGreen
                    VehicleRentalStatus.RENTED_ACTIVE -> ElectricBlue
                    VehicleRentalStatus.IN_MAINTENANCE -> AccentAmber
                    VehicleRentalStatus.RESERVED -> AccentPurple
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vehicle.status.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                // Category pill in top left
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = vehicle.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Vehicle Specs & Actions
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${vehicle.make} ${vehicle.model}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Plate: ${vehicle.licensePlate} • Year ${vehicle.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${vehicle.dailyRateUsd.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PrimaryCyan
                        )
                        Text(
                            text = "/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Specs Badges Row (HP, 0-60, Range, Drive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecChip(icon = Icons.Default.Bolt, label = "${vehicle.specs.horsepower} HP")
                    SpecChip(icon = Icons.Default.Timer, label = "${vehicle.specs.acceleration0to60}s 0-60")
                    SpecChip(icon = Icons.Default.BatteryChargingFull, label = "${vehicle.specs.rangeMiles} mi")
                    SpecChip(icon = Icons.Default.Settings, label = vehicle.specs.driveType)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (vehicle.status == VehicleRentalStatus.AVAILABLE) {
                        Button(
                            onClick = onBook,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCyan,
                                contentColor = OnPrimaryDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_book_${vehicle.id}")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Rental", fontWeight = FontWeight.Bold)
                        }
                    } else if (vehicle.status == VehicleRentalStatus.RENTED_ACTIVE) {
                        Button(
                            onClick = onMonitor,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlueContainer,
                                contentColor = PrimaryCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_monitor_${vehicle.id}")
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Monitor Live Cockpit", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSelect,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("View Diagnostics", color = TextSecondaryDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceContainerDark
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimaryDark
            )
        }
    }
}
