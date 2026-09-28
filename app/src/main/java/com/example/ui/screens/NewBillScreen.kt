package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.ui.components.AddEditCompanyDialog
import com.example.ui.components.AddPhotographerDialog
import com.example.ui.components.CompanyLogoBadge
import com.example.ui.components.LiveBillSummaryCard
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
    val companies by viewModel.allCompanies.collectAsState()

    var showAddPhotogDialog by remember { mutableStateOf(false) }
    var showAddCompanyDialog by remember { mutableStateOf(false) }
    var companyDropdownExpanded by remember { mutableStateOf(false) }
    var photogDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf("UPI", "Cash", "Bank Transfer", "Cheque")
    val selectedCompany = companies.firstOrNull { it.id == formState.selectedCompanyId }
    val selectedPhotographer = photographers.firstOrNull { it.id == formState.selectedPhotographerId }

    if (showAddCompanyDialog) {
        AddEditCompanyDialog(
            initialCompany = null,
            onDismiss = { showAddCompanyDialog = false },
            onConfirm = { name, owner, address, mobile, email, gst, logoTag, details ->
                viewModel.addCompany(name, owner, address, mobile, email, gst, logoTag, details) { newCompanyId ->
                    viewModel.updateSelectedCompany(newCompanyId)
                    showAddCompanyDialog = false
                }
            }
        )
    }

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
            // Live Financial Summary right near top so it updates in real time
            LiveBillSummaryCard(
                totalBill = formState.calculatedTotalBill,
                received = formState.calculatedReceived,
                remaining = formState.calculatedRemaining,
                status = formState.calculatedStatus,
                errorMessage = formState.validationError,
                modifier = Modifier.testTag("live_bill_summary_card")
            )

            // Section 1: Requirement 3 - Prominent Company Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELECT COMPANY / STUDIO *",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        if (selectedCompany != null) {
                            Text(
                                text = "GST: ${selectedCompany.gstNumber.ifBlank { "N/A" }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = companyDropdownExpanded,
                            onExpandedChange = { companyDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedCompany?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Billing Studio / Company *") },
                                placeholder = { Text("Select Company / Studio") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyDropdownExpanded) },
                                leadingIcon = selectedCompany?.let {
                                    { CompanyLogoBadge(company = it, size = 26) }
                                },
                                isError = formState.selectedCompanyId == null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("company_selector")
                            )
                            ExposedDropdownMenu(
                                expanded = companyDropdownExpanded,
                                onDismissRequest = { companyDropdownExpanded = false }
                            ) {
                                companies.forEach { comp ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CompanyLogoBadge(company = comp, size = 28)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(comp.name, fontWeight = FontWeight.Bold)
                                                    if (comp.ownerName.isNotBlank() || comp.mobileNumber.isNotBlank()) {
                                                        Text(
                                                            "${comp.ownerName} • ${comp.mobileNumber}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.updateSelectedCompany(comp.id)
                                            companyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showAddCompanyDialog = true },
                            modifier = Modifier.testTag("quick_add_company_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Company",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (selectedCompany != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Text(
                                    text = "Header details: ${selectedCompany.name} | ${selectedCompany.mobileNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedCompany.address.isNotBlank()) {
                                    Text(
                                        text = selectedCompany.address,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Photographer & Customer Details (Requirement 7)
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
                        text = "CUSTOMER & PHOTOGRAPHER DETAILS",
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

                    // Customer Name (Required)
                    OutlinedTextField(
                        value = formState.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("Customer Name (e.g. Rahul & Pooja) *") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                        singleLine = true
                    )

                    // Customer Phone & Bill Number (Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.customerPhone,
                            onValueChange = { viewModel.updateCustomerPhone(it) },
                            label = { Text("Customer Phone") },
                            placeholder = { Text("98xxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("customer_phone_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.billNumber,
                            onValueChange = { viewModel.updateBillNumber(it) },
                            label = { Text("Bill Number (Optional)") },
                            placeholder = { Text("Auto") },
                            modifier = Modifier.weight(1f).testTag("bill_number_input"),
                            singleLine = true
                        )
                    }

                    // Customer Address
                    OutlinedTextField(
                        value = formState.customerAddress,
                        onValueChange = { viewModel.updateCustomerAddress(it) },
                        label = { Text("Customer Address (Optional)") },
                        placeholder = { Text("City / Town") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_address_input"),
                        singleLine = true
                    )
                }
            }

            // Section 3: Album & Service Details
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
                        text = "SERVICE & ALBUM CHARGES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Album Type / Event Service Details
                    OutlinedTextField(
                        value = formState.albumType,
                        onValueChange = { viewModel.updateAlbumType(it) },
                        label = { Text("Album Type / Service (e.g. Wedding Album)") },
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
                            label = { Text("Pages / Qty *") },
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

            // Section 4: Payment Received
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

                    // Payment Method selector
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

                    // Order Notes
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
