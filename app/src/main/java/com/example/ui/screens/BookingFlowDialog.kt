package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.InsuranceTier
import com.example.model.Vehicle
import com.example.ui.theme.*

@Composable
fun BookingFlowDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        email: String,
        license: String,
        days: Int,
        insurance: InsuranceTier,
        childSeat: Boolean,
        gps: Boolean,
        unlimited: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("Jordan Lee") }
    var email by remember { mutableStateOf("jordan.lee@drive.com") }
    var licenseNumber by remember { mutableStateOf("CA-88392019") }
    var rentalDays by remember { mutableIntStateOf(3) }
    var selectedInsurance by remember { mutableStateOf(InsuranceTier.PREMIUM) }
    var includeChildSeat by remember { mutableStateOf(false) }
    var includeGpsDashcam by remember { mutableStateOf(true) }
    var includeUnlimitedMiles by remember { mutableStateOf(true) }

    val dailyRate = vehicle.dailyRateUsd
    val insuranceTotal = selectedInsurance.dailyPriceUsd * rentalDays
    val addOnsTotal = (if (includeChildSeat) 10.0 else 0.0) * rentalDays +
            (if (includeGpsDashcam) 12.0 else 0.0) * rentalDays +
            (if (includeUnlimitedMiles) 15.0 else 0.0) * rentalDays
    val rentalSubtotal = dailyRate * rentalDays
    val taxesAndFees = 45.0
    val totalEstimated = rentalSubtotal + insuranceTotal + addOnsTotal + taxesAndFees

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("booking_flow_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RENTAL CHECKOUT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = PrimaryCyan
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

                // Rental Duration Selector
                Text(
                    text = "Rental Duration",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$rentalDays Days ($${dailyRate.toInt()}/day)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyan
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (rentalDays > 1) rentalDays-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge, color = PrimaryCyan)
                        }
                        Text(
                            text = "$rentalDays",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        IconButton(
                            onClick = { if (rentalDays < 30) rentalDays++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge, color = PrimaryCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Insurance Protection Tier
                Text(
                    text = "Insurance & Collision Protection",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                InsuranceTier.entries.forEach { tier ->
                    val isSelected = selectedInsurance == tier
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElectricBlueContainer else SurfaceContainerDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedInsurance = tier }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedInsurance = tier },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tier.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PrimaryCyan else TextPrimaryDark
                                )
                                Text(
                                    text = tier.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
                            Text(
                                text = "+$${tier.dailyPriceUsd.toInt()}/d",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) PrimaryCyan else TextSecondaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Add-on options
                Text(
                    text = "Optional Extras",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                AddonRow("Unlimited Mileage", "+$15/day", includeUnlimitedMiles) { includeUnlimitedMiles = it }
                AddonRow("Telematics 4K Dashcam & GPS", "+$12/day", includeGpsDashcam) { includeGpsDashcam = it }
                AddonRow("Child Safety Seat", "+$10/day", includeChildSeat) { includeChildSeat = it }

                Spacer(modifier = Modifier.height(14.dp))

                // Driver Details
                Text(
                    text = "Driver Information",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Legal Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_driver_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth().testTag("input_driver_email"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("Driver License #") },
                    modifier = Modifier.fillMaxWidth().testTag("input_driver_license"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Breakdown Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        PriceLine("Rental Subtotal ($rentalDays days)", "$${rentalSubtotal.toInt()}")
                        PriceLine("Insurance (${selectedInsurance.displayName})", "$${insuranceTotal.toInt()}")
                        if (addOnsTotal > 0) PriceLine("Add-ons", "$${addOnsTotal.toInt()}")
                        PriceLine("State Taxes & Environmental Fee", "$${taxesAndFees.toInt()}")
                        PriceLine("Refundable Deposit (Hold)", "$${vehicle.securityDepositUsd.toInt()}", isDeposit = true)

                        Divider(color = SurfaceContainerHighDark, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL PAYABLE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "$${totalEstimated.toInt()} USD",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm Button
                Button(
                    onClick = {
                        if (name.isNotBlank() && licenseNumber.isNotBlank()) {
                            onConfirm(
                                name,
                                email,
                                licenseNumber,
                                rentalDays,
                                selectedInsurance,
                                includeChildSeat,
                                includeGpsDashcam,
                                includeUnlimitedMiles
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = OnPrimaryDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_confirm_booking")
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Booking & Activate Digital Key",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun AddonRow(label: String, price: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark)
            Text(text = price, style = MaterialTheme.typography.labelSmall, color = PrimaryCyan)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryCyan,
                checkedTrackColor = ElectricBlueContainer
            )
        )
    }
}

@Composable
fun PriceLine(label: String, value: String, isDeposit: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDeposit) AccentAmber else TextSecondaryDark
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isDeposit) AccentAmber else TextPrimaryDark
        )
    }
}
