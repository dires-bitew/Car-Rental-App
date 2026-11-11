package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SimulationRoute
import com.example.model.Vehicle
import com.example.model.VehicleRentalStatus
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun LiveMapTrackingScreen(
    activeVehicle: Vehicle?,
    allVehicles: List<Vehicle>,
    onSelectVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeVehicle == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No vehicle telemetry available", color = TextSecondaryDark)
        }
        return
    }

    var isFleetMode by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing radar animation
    val infiniteTransition = rememberInfiniteTransition(label = "mapRadar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    // Map bounding box around SF Bay area: Lat 37.70 to 37.82, Lng -122.52 to -122.38
    val minLat = 37.7100
    val maxLat = 37.8180
    val minLng = -122.5200
    val maxLng = -122.3900

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("live_map_screen")
    ) {
        // High-Performance Interactive Vector Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.7f, 3.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Helper to project GPS coordinates to Canvas XY
            fun project(lat: Double, lng: Double): Offset {
                val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat()) // Lat inverted in screen Y
                val rawX = normX * width
                val rawY = normY * height

                val centerX = width / 2
                val centerY = height / 2

                val scaledX = (rawX - centerX) * zoomScale + centerX + panOffsetX
                val scaledY = (rawY - centerY) * zoomScale + centerY + panOffsetY
                return Offset(scaledX, scaledY)
            }

            // Draw dark futuristic map background grid
            drawMapGrid(width, height, zoomScale, panOffsetX, panOffsetY)

            // Draw Coastline & Bay Water Body
            drawCoastline(::project, width, height)

            // Draw Highway & City Street Network
            drawStreetNetwork(::project)

            // Draw Geofence Safety Perimeter Ring
            val centerPoint = project(activeVehicle.geofenceCenter.latitude, activeVehicle.geofenceCenter.longitude)
            val perimeterRadiusPx = 180.dp.toPx() * zoomScale
            drawCircle(
                color = if (activeVehicle.isGeofenceBreached) AccentRed.copy(alpha = 0.15f) else PrimaryCyan.copy(alpha = 0.08f),
                radius = perimeterRadiusPx,
                center = centerPoint
            )
            drawCircle(
                color = if (activeVehicle.isGeofenceBreached) AccentRed else PrimaryCyan.copy(alpha = 0.5f),
                radius = perimeterRadiusPx,
                center = centerPoint,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Planned Route Path Polyline
            val waypoints = SimulationRoute.waypoints
            if (waypoints.isNotEmpty()) {
                val routePath = Path()
                waypoints.forEachIndexed { index, wp ->
                    val pt = project(wp.point.latitude, wp.point.longitude)
                    if (index == 0) routePath.moveTo(pt.x, pt.y) else routePath.lineTo(pt.x, pt.y)
                }
                drawPath(
                    path = routePath,
                    color = PrimaryCyan.copy(alpha = 0.45f),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Start pin & End pin
                val startPt = project(waypoints.first().point.latitude, waypoints.first().point.longitude)
                drawCircle(color = AccentGreen, radius = 6.dp.toPx(), center = startPt)

                val destPt = project(waypoints.last().point.latitude, waypoints.last().point.longitude)
                drawCircle(color = AccentAmber, radius = 6.dp.toPx(), center = destPt)
            }

            // Draw Other Fleet Vehicles if in Fleet Mode
            if (isFleetMode) {
                allVehicles.forEach { veh ->
                    if (veh.id != activeVehicle.id) {
                        val pt = project(veh.location.latitude, veh.location.longitude)
                        val pinColor = when (veh.status) {
                            VehicleRentalStatus.AVAILABLE -> AccentGreen
                            VehicleRentalStatus.RENTED_ACTIVE -> ElectricBlue
                            VehicleRentalStatus.IN_MAINTENANCE -> AccentAmber
                            VehicleRentalStatus.RESERVED -> AccentPurple
                        }
                        drawCircle(color = pinColor, radius = 7.dp.toPx(), center = pt)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pt)
                    }
                }
            }

            // Draw Active Vehicle Marker with Live Heading & Pulsing Radar Wave
            val carPt = project(activeVehicle.location.latitude, activeVehicle.location.longitude)

            // Radar ripple
            val maxRadarRadius = 45.dp.toPx()
            drawCircle(
                color = (if (activeVehicle.isGeofenceBreached) AccentRed else PrimaryCyan).copy(alpha = (1f - radarPulse) * 0.4f),
                radius = 12.dp.toPx() + maxRadarRadius * radarPulse,
                center = carPt
            )

            // Vehicle Glow & Core
            drawCircle(
                color = if (activeVehicle.isGeofenceBreached) AccentRed.copy(alpha = 0.3f) else PrimaryCyan.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = carPt
            )
            drawCircle(
                color = if (activeVehicle.isGeofenceBreached) AccentRed else PrimaryCyan,
                radius = 11.dp.toPx(),
                center = carPt
            )

            // Arrow direction pointer rotated according to vehicle heading
            rotate(degrees = activeVehicle.location.headingDeg, pivot = carPt) {
                val arrowPath = Path().apply {
                    moveTo(carPt.x, carPt.y - 14.dp.toPx())
                    lineTo(carPt.x + 7.dp.toPx(), carPt.y + 7.dp.toPx())
                    lineTo(carPt.x, carPt.y + 3.dp.toPx())
                    lineTo(carPt.x - 7.dp.toPx(), carPt.y + 7.dp.toPx())
                    close()
                }
                drawPath(path = arrowPath, color = OnPrimaryDark)
            }
        }

        // Top Telemetry Status Ribbon
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceDark.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (activeVehicle.engineRunning) AccentGreen else AccentAmber,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${activeVehicle.make} ${activeVehicle.model}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "PLATE: ${activeVehicle.licensePlate} • GPS 5G LINK",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Live Speed Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (activeVehicle.currentSpeedMph > activeVehicle.speedLimitMph) AccentRed else PrimaryCyanDark
                    ) {
                        Text(
                            text = "${activeVehicle.currentSpeedMph.toInt()} MPH",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Warning Banners (Geofence Breach or Overspeed)
            AnimatedVisibility(visible = activeVehicle.isGeofenceBreached) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GEOFENCE ALERT: Vehicle exceeded 15 mi designated rental perimeter!",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Center, Fleet Mode)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.0f) },
                containerColor = SurfaceDark.copy(alpha = 0.9f),
                contentColor = PrimaryCyan,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("btn_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.7f) },
                containerColor = SurfaceDark.copy(alpha = 0.9f),
                contentColor = PrimaryCyan,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("btn_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                containerColor = SurfaceDark.copy(alpha = 0.9f),
                contentColor = PrimaryCyan,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("btn_recenter")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(
                onClick = { isFleetMode = !isFleetMode },
                containerColor = if (isFleetMode) PrimaryCyan else SurfaceDark.copy(alpha = 0.9f),
                contentColor = if (isFleetMode) OnPrimaryDark else PrimaryCyan,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("btn_fleet_mode")
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = "Fleet Mode")
            }
        }

        // Bottom Turn-by-Turn Navigation HUD Card
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = SurfaceDark.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ElectricBlueContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Turn icon",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IN 450 METERS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = PrimaryCyan
                        )
                        Text(
                            text = "Follow ${activeVehicle.location.streetName.ifBlank { "Scenic Highway Loop" }}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark,
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ETA 14 MIN",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AccentGreen
                        )
                        Text(
                            text = "8.4 mi remain",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry Quick Stats row (Battery, Odometer, Engine status, Doors)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStat(
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Energy",
                        value = "${activeVehicle.batteryFuelPercent.toInt()}%",
                        color = if (activeVehicle.batteryFuelPercent < 20f) AccentRed else AccentGreen
                    )
                    QuickStat(
                        icon = Icons.Default.Speed,
                        label = "Odometer",
                        value = "${String.format("%.1f", activeVehicle.odometerMiles)} mi",
                        color = TextPrimaryDark
                    )
                    QuickStat(
                        icon = if (activeVehicle.doorsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        label = "Doors",
                        value = if (activeVehicle.doorsLocked) "Locked" else "Unlocked",
                        color = if (activeVehicle.doorsLocked) PrimaryCyan else AccentAmber
                    )
                    QuickStat(
                        icon = Icons.Default.PowerSettingsNew,
                        label = "Engine",
                        value = if (activeVehicle.engineRunning) "RUNNING" else "OFF",
                        color = if (activeVehicle.engineRunning) AccentGreen else TextSecondaryDark
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondaryDark)
    }
}

// Map Canvas Drawing Helpers
fun DrawScope.drawMapGrid(width: Float, height: Float, scale: Float, panX: Float, panY: Float) {
    val gridSize = 45.dp.toPx() * scale
    val startX = (panX % gridSize) - gridSize
    val startY = (panY % gridSize) - gridSize

    var x = startX
    while (x < width + gridSize) {
        drawLine(
            color = SurfaceContainerDark.copy(alpha = 0.35f),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
        x += gridSize
    }

    var y = startY
    while (y < height + gridSize) {
        drawLine(
            color = SurfaceContainerDark.copy(alpha = 0.35f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
        y += gridSize
    }
}

fun DrawScope.drawCoastline(project: (Double, Double) -> Offset, width: Float, height: Float) {
    // Pacific coastline coordinates
    val coastPts = listOf(
        project(37.7100, -122.5050),
        project(37.7250, -122.5040),
        project(37.7450, -122.5080),
        project(37.7700, -122.5120),
        project(37.7880, -122.5080),
        project(37.8050, -122.4800),
        project(37.8100, -122.4600)
    )

    val waterPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(coastPts.first().x, coastPts.first().y)
        coastPts.forEach { pt -> lineTo(pt.x, pt.y) }
        lineTo(0f, height)
        close()
    }

    drawPath(path = waterPath, color = Color(0xFF07111E).copy(alpha = 0.85f))

    // Coastline edge glow
    val coastLinePath = Path().apply {
        moveTo(coastPts.first().x, coastPts.first().y)
        coastPts.forEach { pt -> lineTo(pt.x, pt.y) }
    }
    drawPath(
        path = coastLinePath,
        color = ElectricBlue.copy(alpha = 0.4f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

fun DrawScope.drawStreetNetwork(project: (Double, Double) -> Offset) {
    // Key artery streets
    val arteries = listOf(
        // Market Street corridor
        listOf(project(37.7950, -122.3950), project(37.7749, -122.4194), project(37.7600, -122.4350)),
        // Van Ness / 101 North-South
        listOf(project(37.7200, -122.4100), project(37.7650, -122.4200), project(37.8050, -122.4250)),
        // Geary Blvd East-West
        listOf(project(37.7850, -122.4050), project(37.7820, -122.4600), project(37.7790, -122.5100)),
        // 19th Ave & Sloat
        listOf(project(37.7200, -122.4750), project(37.7400, -122.4780), project(37.7680, -122.4750)),
        // Bay shore line
        listOf(project(37.8080, -122.4100), project(37.7950, -122.3900), project(37.7600, -122.3850))
    )

    arteries.forEach { line ->
        if (line.size >= 2) {
            val path = Path().apply {
                moveTo(line[0].x, line[0].y)
                for (i in 1 until line.size) {
                    lineTo(line[i].x, line[i].y)
                }
            }
            drawPath(
                path = path,
                color = SurfaceContainerHighDark.copy(alpha = 0.7f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
