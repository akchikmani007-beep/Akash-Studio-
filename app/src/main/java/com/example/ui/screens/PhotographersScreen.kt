package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillWithPayments
import com.example.data.model.PhotographerSummary
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.AddPhotographerDialog
import com.example.ui.components.PaymentProgressBar
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.ThreeAmountsRow
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorRemaining
import com.example.ui.theme.ColorTotalBill
import com.example.ui.components.formatRupee
import com.example.ui.viewmodel.BillingViewModel
import com.example.util.CommunicationUtils
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotographersScreen(
    viewModel: BillingViewModel,
    onPhotographerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summaries by viewModel.photographerSummaries.collectAsState()
    val allBills by viewModel.allBills.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // State for "+ Payment" quick button on card
    var paymentTargetBill by remember { mutableStateOf<BillWithPayments?>(null) }

    val filteredSummaries = summaries.filter {
        it.photographer.name.contains(searchQuery, ignoreCase = true) ||
                it.photographer.studioName.contains(searchQuery, ignoreCase = true) ||
                it.latestCustomer.contains(searchQuery, ignoreCase = true)
    }

    if (showAddDialog) {
        AddPhotographerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, studio, notes ->
                viewModel.addPhotographer(name, phone, studio, notes) {
                    showAddDialog = false
                }
            }
        )
    }

    paymentTargetBill?.let { billWithPayments ->
        AddPaymentDialog(
            billWithPayments = billWithPayments,
            onDismiss = { paymentTargetBill = null },
            onConfirm = { amount, method, notes ->
                viewModel.addPayment(
                    billId = billWithPayments.bill.id,
                    photographerId = billWithPayments.bill.photographerId,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
                paymentTargetBill = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Photographers", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_photographer_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Photographer")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, studio or customer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("photographer_search_input")
            )

            if (filteredSummaries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📸", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No photographers registered yet" else "No matching photographers found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Add First Photographer")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
                ) {
                    items(filteredSummaries, key = { it.photographer.id }) { summary ->
                        PhotographerCard(
                            summary = summary,
                            onView = { onPhotographerClick(summary.photographer.id) },
                            onAddPayment = {
                                // Find recent pending bill for this photographer
                                val pendingBill = allBills.firstOrNull {
                                    it.bill.photographerId == summary.photographer.id && it.remaining > 0
                                } ?: allBills.firstOrNull { it.bill.photographerId == summary.photographer.id }
                                if (pendingBill != null) {
                                    paymentTargetBill = pendingBill
                                } else {
                                    onPhotographerClick(summary.photographer.id)
                                }
                            },
                            onWhatsApp = {
                                val latestBill = allBills.firstOrNull {
                                    it.bill.photographerId == summary.photographer.id
                                }
                                val customer = latestBill?.bill?.customerName ?: summary.latestCustomer
                                val msg = CommunicationUtils.buildWhatsAppMessage(
                                    photographerName = summary.photographer.name,
                                    customerName = customer,
                                    totalBill = summary.totalBilled,
                                    received = summary.totalReceived,
                                    remaining = summary.totalRemaining
                                )
                                CommunicationUtils.openWhatsApp(
                                    context = context,
                                    phoneNumber = summary.photographer.phoneNumber,
                                    message = msg
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Requirement 6: PHOTOGRAPHER CARD MUST SHOW ALL THREE
 * On the Photographer list, each photographer should have a simple card.
 * Example:
 * 📸 ABC Photography
 * Rahul & Pooja
 * 💰 Total Bill: ₹2,500
 * ✅ Received: ₹500
 * ⏳ Remaining: ₹2,000
 * 🟡 Partially Paid
 * Buttons:
 * [View] [＋ Payment] [WhatsApp]
 */
@Composable
fun PhotographerCard(
    summary: PhotographerSummary,
    onView: () -> Unit,
    onAddPayment: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("photographer_card_${summary.photographer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name, Customer & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📸", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = summary.photographer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (summary.latestCustomer.isNotBlank() && summary.latestCustomer != "No orders yet") {
                            Text(
                                text = summary.latestCustomer,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (summary.photographer.studioName.isNotBlank()) {
                            Text(
                                text = summary.photographer.studioName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                PaymentStatusBadge(status = summary.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Three Amounts prominently displayed
            ThreeAmountsRow(
                totalBill = summary.totalBilled,
                received = summary.totalReceived,
                remaining = summary.totalRemaining
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment progress bar
            PaymentProgressBar(
                totalBill = summary.totalBilled,
                received = summary.totalReceived,
                remaining = summary.totalRemaining
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Requirement 6 Buttons: [View] [＋ Payment] [WhatsApp]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f).height(38.dp).testTag("view_photographer_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onAddPayment,
                    modifier = Modifier.weight(1.2f).height(38.dp).testTag("card_add_payment_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("＋ Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1.1f).height(38.dp).testTag("card_whatsapp_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
