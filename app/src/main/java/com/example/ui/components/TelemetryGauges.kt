package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TirePressure
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedMph: Float,
    maxSpeedMph: Float = 140f,
    speedLimitMph: Float = 80f,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedMph,
        animationSpec = tween(durationMillis = 350),
        label = "speedAnimation"
    )

    val isOverSpeed = animatedSpeed > speedLimitMph
    val speedGaugeColor = when {
        isOverSpeed -> AccentRed
        animatedSpeed > 65f -> AccentAmber
        else -> PrimaryCyan
    }

    Box(
        modifier = modifier
            .size(210.dp)
            .testTag("speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 145f
            val sweepAngle = 250f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track
            drawArc(
                color = SurfaceContainerHighDark.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Speed Arc
            val progress = (animatedSpeed / maxSpeedMph).coerceIn(0f, 1f)
            val currentSweep = sweepAngle * progress

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        PrimaryCyanDark,
                        speedGaugeColor,
                        if (isOverSpeed) AccentRed else PrimaryCyan
                    )
                ),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tick marks
            val totalTicks = 14
            for (i in 0..totalTicks) {
                val tickFraction = i.toFloat() / totalTicks
                val angle = startAngle + sweepAngle * tickFraction
                val angleRad = Math.toRadians(angle.toDouble())

                val outerRadius = (size.width - strokeWidth * 2) / 2
                val innerRadius = outerRadius - if (i % 2 == 0) 10.dp.toPx() else 6.dp.toPx()
                val center = Offset(size.width / 2, size.height / 2)

                val start = Offset(
                    center.x + (innerRadius * cos(angleRad)).toFloat(),
                    center.y + (innerRadius * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    center.x + (outerRadius * cos(angleRad)).toFloat(),
                    center.y + (outerRadius * sin(angleRad)).toFloat()
                )

                val isPassed = tickFraction <= progress
                drawLine(
                    color = if (isPassed) speedGaugeColor else TextTertiaryDark,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 2 == 0) 2.5.dp.toPx() else 1.5.dp.toPx()
                )
            }
        }

        // Center Digital Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedSpeed.toInt()}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 44.sp
                ),
                color = if (isOverSpeed) AccentRed else TextPrimaryDark
            )
            Text(
                text = "MPH",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = speedGaugeColor
            )
            if (isOverSpeed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Overspeed",
                        tint = AccentRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LIMIT EXCEEDED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = AccentRed
                    )
                }
            }
        }
    }
}

@Composable
fun TirePressureWidget(
    tirePressure: TirePressure,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerDark, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("tpms_widget")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIRE PRESSURE (TPMS)",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = TextSecondaryDark
                )
                Text(
                    text = if (tirePressure.isAnyLow) "PRESSURE WARNING" else "ALL OPTIMAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (tirePressure.isAnyLow) AccentAmber else AccentGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Wheels layout surrounding car representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Wheels (Front Left, Rear Left)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TireBadge("FL", tirePressure.frontLeftPsi)
                    Spacer(modifier = Modifier.height(18.dp))
                    TireBadge("RL", tirePressure.rearLeftPsi)
                }

                // Center Car Body Graphic
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(100.dp)
                        .background(SurfaceDark, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        // Minimalist vehicle chassis outline
                        val bodyColor = PrimaryCyan.copy(alpha = 0.6f)
                        drawRoundRect(
                            color = bodyColor,
                            topLeft = Offset(size.width * 0.15f, 0f),
                            size = Size(size.width * 0.7f, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Windshield line
                        drawLine(
                            color = bodyColor,
                            start = Offset(size.width * 0.25f, size.height * 0.35f),
                            end = Offset(size.width * 0.75f, size.height * 0.35f),
                            strokeWidth = 2.dp.toPx()
                        )
                        // Rear glass line
                        drawLine(
                            color = bodyColor,
                            start = Offset(size.width * 0.25f, size.height * 0.75f),
                            end = Offset(size.width * 0.75f, size.height * 0.75f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Right Wheels (Front Right, Rear Right)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TireBadge("FR", tirePressure.frontRightPsi)
                    Spacer(modifier = Modifier.height(18.dp))
                    TireBadge("RR", tirePressure.rearRightPsi)
                }
            }
        }
    }
}

@Composable
fun TireBadge(position: String, psi: Float) {
    val isLow = psi < 30f
    val color = if (isLow) AccentAmber else AccentGreen

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(SurfaceDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = position,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextTertiaryDark
        )
        Text(
            text = String.format("%.1f", psi),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = "PSI",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextSecondaryDark
        )
    }
}
