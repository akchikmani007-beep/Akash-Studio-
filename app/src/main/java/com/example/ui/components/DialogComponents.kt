package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillWithPayments
import com.example.data.model.PaymentRecord
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorRemaining

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    billWithPayments: BillWithPayments,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("UPI") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val methods = listOf("UPI", "Cash", "Bank Transfer", "Cheque")

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val newTotalReceived = billWithPayments.totalReceived + enteredAmount
    val newRemaining = (billWithPayments.totalBill - newTotalReceived).coerceAtLeast(0.0)
    val isOverpaying = enteredAmount > 0 && newTotalReceived > billWithPayments.totalBill

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Customer: ${billWithPayments.bill.customerName}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Live preview of the 3 values
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Bill:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupee(billWithPayments.totalBill), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Received:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupee(billWithPayments.totalReceived), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Remaining:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupee(billWithPayments.remaining), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorRemaining)
                        }
                        if (enteredAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("New Received:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ColorReceived)
                                Text(formatRupee(newTotalReceived), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorReceived)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("New Remaining:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (newRemaining > 0) ColorRemaining else ColorReceived)
                                Text(formatRupee(newRemaining), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (newRemaining > 0) ColorRemaining else ColorReceived)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    singleLine = true,
                    isError = isOverpaying,
                    supportingText = if (isOverpaying) {
                        { Text("Amount exceeds remaining balance (${formatRupee(billWithPayments.remaining)})", color = Color(0xFFDC2626)) }
                    } else null
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethod = method
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Transaction ID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredAmount > 0 && !isOverpaying) {
                        onConfirm(enteredAmount, selectedMethod, notes)
                    }
                },
                enabled = enteredAmount > 0 && !isOverpaying,
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Add Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPaymentDialog(
    payment: PaymentRecord,
    billTotal: Double,
    otherPaymentsTotal: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(payment.amount.toString().replace(".0", "")) }
    var selectedMethod by remember { mutableStateOf(payment.paymentMethod) }
    var notes by remember { mutableStateOf(payment.notes) }

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val newTotalReceived = otherPaymentsTotal + enteredAmount
    val isOverpaying = enteredAmount > 0 && newTotalReceived > billTotal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isOverpaying,
                    supportingText = if (isOverpaying) {
                        { Text("Total received would exceed total bill of ${formatRupee(billTotal)}", color = Color(0xFFDC2626)) }
                    } else null
                )

                OutlinedTextField(
                    value = selectedMethod,
                    onValueChange = { selectedMethod = it },
                    label = { Text("Method (e.g. UPI, Cash)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredAmount > 0 && !isOverpaying) {
                        onConfirm(enteredAmount, selectedMethod, notes)
                    }
                },
                enabled = enteredAmount > 0 && !isOverpaying
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddPhotographerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, studio: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var studio by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Photographer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Photographer / Studio Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("photographer_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (for WhatsApp/SMS) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("photographer_phone_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = studio,
                    onValueChange = { studio = it },
                    label = { Text("Studio Name / Secondary Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, studio, notes)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_photographer_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
