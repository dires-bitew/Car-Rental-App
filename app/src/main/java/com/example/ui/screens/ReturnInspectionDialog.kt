package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.model.RentalBooking
import com.example.model.Vehicle
import com.example.ui.theme.*

@Composable
fun ReturnInspectionDialog(
    booking: RentalBooking?,
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSubmitReturn: (
        bookingId: String,
        scratches: Boolean,
        interiorClean: Boolean,
        fobReturned: Boolean,
        notes: String
    ) -> Unit
) {
    if (booking == null || vehicle == null) return

    var hasScratches by remember { mutableStateOf(false) }
    var interiorClean by remember { mutableStateOf(true) }
    var keyFobReturned by remember { mutableStateOf(true) }
    var inspectorNotes by remember { mutableStateOf("Depot check: Vehicle returned in pristine condition.") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("return_inspection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VEHICLE RETURN INSPECTION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AccentGreen
                        )
                        Text(
                            text = "${vehicle.make} ${vehicle.model}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry Reconciliation Summary
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AUTOMATED TELEMETRY AUDIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Odometer Start", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("${String.format("%.1f", booking.startOdometerMiles)} mi", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimaryDark)
                            }
                            Column {
                                Text("Current Odometer", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("${String.format("%.1f", vehicle.odometerMiles)} mi", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryCyan)
                            }
                            Column {
                                Text("Distance", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("+${String.format("%.1f", vehicle.odometerMiles - booking.startOdometerMiles)} mi", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Energy at Start", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("${booking.startingFuelPercent.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimaryDark)
                            }
                            Column {
                                Text("Energy at Return", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("${vehicle.batteryFuelPercent.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryCyan)
                            }
                            Column {
                                Text("Status", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                Text("Level OK", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Physical Condition Checklist
                Text(
                    text = "Physical Condition Check",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                InspectionCheckbox(
                    title = "Key Fob / Digital Smart Key Returned",
                    subtitle = "Remote access de-provisioned",
                    checked = keyFobReturned,
                    onCheckedChange = { keyFobReturned = it }
                )

                InspectionCheckbox(
                    title = "Interior Cleanliness Passed",
                    subtitle = "No excessive dirt, stains, or smoke odor",
                    checked = interiorClean,
                    onCheckedChange = { interiorClean = it }
                )

                InspectionCheckbox(
                    title = "Body & Glass Checked (Damage Free)",
                    subtitle = if (!hasScratches) "No new dings, dents or rim scratches" else "Scratches noted",
                    checked = !hasScratches,
                    onCheckedChange = { hasScratches = !it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inspector Notes
                Text(
                    text = "Inspection Notes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inspectorNotes,
                    onValueChange = { inspectorNotes = it },
                    modifier = Modifier.fillMaxWidth().testTag("input_inspection_notes"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Security Deposit Refund Notice
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Security Deposit Refund",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AccentGreen
                            )
                            Text(
                                text = "$${vehicle.securityDepositUsd.toInt()} USD will be released to original payment card.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Final Submit Action
                Button(
                    onClick = {
                        onSubmitReturn(
                            booking.id,
                            hasScratches,
                            interiorClean,
                            keyFobReturned,
                            inspectorNotes
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = OnPrimaryDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_submit_return")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Return & Close Rental",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun InspectionCheckbox(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentGreen,
                checkmarkColor = OnPrimaryDark
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimaryDark)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
        }
    }
}
