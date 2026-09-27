package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddPhotographerDialog
import com.example.ui.components.LiveBillSummaryCard
import com.example.ui.components.formatRupee
import com.example.ui.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBillScreen(
    viewModel: BillingViewModel,
    onBillCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.newBillForm.collectAsState()
    val photographers by viewModel.allPhotographers.collectAsState()

    var showAddPhotogDialog by remember { mutableStateOf(false) }
    var photogDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf("UPI", "Cash", "Bank Transfer", "Cheque")
    val selectedPhotographer = photographers.firstOrNull { it.id == formState.selectedPhotographerId }

    if (showAddPhotogDialog) {
        AddPhotographerDialog(
            onDismiss = { showAddPhotogDialog = false },
            onConfirm = { name, phone, studio, notes ->
                viewModel.addPhotographer(name, phone, studio, notes) { newId ->
                    viewModel.updateSelectedPhotographer(newId)
                    showAddPhotogDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Financial Summary right near top/middle so it's always immediately clear
            LiveBillSummaryCard(
                totalBill = formState.calculatedTotalBill,
                received = formState.calculatedReceived,
                remaining = formState.calculatedRemaining,
                status = formState.calculatedStatus,
                errorMessage = formState.validationError,
                modifier = Modifier.testTag("live_bill_summary_card")
            )

            // Section 1: Photographer & Customer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ORDER DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Photographer selector with Add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = photogDropdownExpanded,
                            onExpandedChange = { photogDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedPhotographer?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Photographer *") },
                                placeholder = { Text("Tap to select photographer") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = photogDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("photographer_selector")
                            )
                            ExposedDropdownMenu(
                                expanded = photogDropdownExpanded,
                                onDismissRequest = { photogDropdownExpanded = false }
                            ) {
                                photographers.forEach { photog ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(photog.name, fontWeight = FontWeight.SemiBold)
                                                if (photog.phoneNumber.isNotBlank()) {
                                                    Text(photog.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.updateSelectedPhotographer(photog.id)
                                            photogDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showAddPhotogDialog = true },
                            modifier = Modifier.testTag("quick_add_photographer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Photographer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Customer Name
                    OutlinedTextField(
                        value = formState.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("Customer Name (e.g. Rahul & Pooja) *") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                        singleLine = true
                    )

                    // Album Type
                    OutlinedTextField(
                        value = formState.albumType,
                        onValueChange = { viewModel.updateAlbumType(it) },
                        label = { Text("Album Type (e.g. Wedding Album)") },
                        modifier = Modifier.fillMaxWidth().testTag("album_type_input"),
                        singleLine = true
                    )

                    // Compact Row: Pages & Rate / Page
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.pagesText,
                            onValueChange = { viewModel.updatePages(it) },
                            label = { Text("Pages *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("pages_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.rateText,
                            onValueChange = { viewModel.updateRate(it) },
                            label = { Text("Rate / Page (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("rate_input"),
                            singleLine = true
                        )
                    }

                    // Compact Row: Extra Charges & Discount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.extraChargesText,
                            onValueChange = { viewModel.updateExtraCharges(it) },
                            label = { Text("Extra Charges (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("extra_charges_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.discountText,
                            onValueChange = { viewModel.updateDiscount(it) },
                            label = { Text("Discount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("discount_input"),
                            singleLine = true
                        )
                    }

                    // Lab / Printing Cost (for Net Profit calculation)
                    OutlinedTextField(
                        value = formState.costExpenseText,
                        onValueChange = { viewModel.updateCostExpense(it) },
                        label = { Text("Lab / Printing Cost (₹, for Net Profit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("cost_expense_input"),
                        singleLine = true
                    )
                }
            }

            // Section 2: Payment Received
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💵 PAYMENT RECEIVED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        letterSpacing = 1.sp
                    )

                    // Payment / Advance
                    OutlinedTextField(
                        value = formState.advanceAmountText,
                        onValueChange = { viewModel.updateAdvanceAmount(it) },
                        label = { Text("Payment / Advance (₹)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("advance_amount_input"),
                        singleLine = true,
                        isError = formState.isAdvanceGreaterThanTotal,
                        supportingText = if (formState.isAdvanceGreaterThanTotal) {
                            { Text("Received amount cannot be greater than the total bill.", color = Color(0xFFDC2626)) }
                        } else null
                    )

                    // Compact Row: Method
                    ExposedDropdownMenuBox(
                        expanded = methodDropdownExpanded,
                        onExpandedChange = { methodDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = formState.paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("payment_method_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = methodDropdownExpanded,
                            onDismissRequest = { methodDropdownExpanded = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        viewModel.updatePaymentMethod(method)
                                        methodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Order Notes / Specifications (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("bill_notes_input"),
                        singleLine = true
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (formState.canSave) {
                        viewModel.saveNewBill { newBillId ->
                            onBillCreated(newBillId)
                        }
                    }
                },
                enabled = formState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_bill_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE BILL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
