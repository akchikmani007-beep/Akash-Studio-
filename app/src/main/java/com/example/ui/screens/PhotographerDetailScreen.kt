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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Photographer
import com.example.ui.components.PaymentProgressBar
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.QuickFinancialStatusBar
import com.example.ui.components.ThreeAmountsRow
import com.example.ui.components.formatRupee
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorRemaining
import com.example.ui.theme.ColorTotalBill
import com.example.ui.viewmodel.BillingViewModel
import com.example.util.CommunicationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotographerDetailScreen(
    photographerId: Long,
    viewModel: BillingViewModel,
    onBack: () -> Unit,
    onBillClick: (Long) -> Unit,
    onNewBillForPhotog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photographers by viewModel.allPhotographers.collectAsState()
    val allBills by viewModel.allBills.collectAsState()

    val photographer = photographers.firstOrNull { it.id == photographerId }
    val photogBills = allBills.filter { it.bill.photographerId == photographerId }

    // Automatic calculations from orders (Requirement 9)
    val totalBilled = photogBills.sumOf { it.totalBill }
    val totalReceived = photogBills.sumOf { it.totalReceived }
    val totalRemaining = (totalBilled - totalReceived).coerceAtLeast(0.0)

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (photographer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Photographer not found")
        }
        return
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Photographer?") },
            text = { Text("Are you sure you want to delete ${photographer.name}? All associated bills and payments will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhotographer(photographer) {
                            showDeleteConfirm = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(photographer.name) }
        var editPhone by remember { mutableStateOf(photographer.phoneNumber) }
        var editStudio by remember { mutableStateOf(photographer.studioName) }
        var editNotes by remember { mutableStateOf(photographer.notes) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Photographer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editStudio,
                        onValueChange = { editStudio = it },
                        label = { Text("Studio Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePhotographer(
                            photographer.copy(
                                name = editName,
                                phoneNumber = editPhone,
                                studioName = editStudio,
                                notes = editNotes
                            )
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(photographer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (photographer.studioName.isNotBlank()) {
                            Text(photographer.studioName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Bar (Call, WhatsApp, New Bill)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (photographer.phoneNumber.isNotBlank()) {
                        OutlinedButton(
                            onClick = { CommunicationUtils.dialPhone(context, photographer.phoneNumber) },
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call")
                        }
                        Button(
                            onClick = {
                                val latestCustomer = photogBills.firstOrNull()?.bill?.customerName ?: "General Account"
                                val msg = CommunicationUtils.buildWhatsAppMessage(
                                    photographerName = photographer.name,
                                    customerName = latestCustomer,
                                    totalBill = totalBilled,
                                    received = totalReceived,
                                    remaining = totalRemaining
                                )
                                CommunicationUtils.openWhatsApp(context, photographer.phoneNumber, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1.2f).height(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", color = Color.White)
                        }
                    }
                    Button(
                        onClick = { onNewBillForPhotog(photographer.id) },
                        modifier = Modifier.weight(1.2f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Bill")
                    }
                }
            }

            // Requirement 9: ACCOUNT SUMMARY CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💰 ACCOUNT SUMMARY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${photogBills.size} Orders",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Three Amounts Row
                        ThreeAmountsRow(
                            totalBill = totalBilled,
                            received = totalReceived,
                            remaining = totalRemaining
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PaymentProgressBar(
                            totalBill = totalBilled,
                            received = totalReceived,
                            remaining = totalRemaining
                        )
                    }
                }
            }

            // Orders list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INDIVIDUAL ORDERS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (photogBills.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No bills created yet for this photographer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { onNewBillForPhotog(photographer.id) }) {
                                Text("Create First Bill")
                            }
                        }
                    }
                }
            } else {
                items(photogBills, key = { it.bill.id }) { billWithPayments ->
                    PhotographerOrderItemCard(
                        billWithPayments = billWithPayments,
                        onClick = { onBillClick(billWithPayments.bill.id) }
                    )
                }

                // Cumulative Total Card at bottom (Requirement 9)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "TOTAL ACCROSS ALL ORDERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Bill", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(formatRupee(totalBilled), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                                }
                                Column {
                                    Text("Received", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(formatRupee(totalReceived), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                                }
                                Column {
                                    Text("Remaining", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(formatRupee(totalRemaining), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (totalRemaining > 0) Color(0xFFFB923C) else Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PhotographerOrderItemCard(
    billWithPayments: BillWithPayments,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = formatter.format(Date(billWithPayments.bill.createdDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_item_card_${billWithPayments.bill.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = billWithPayments.bill.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${billWithPayments.bill.albumType} • $dateStr",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(status = billWithPayments.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Three amounts for this order
            ThreeAmountsRow(
                totalBill = billWithPayments.totalBill,
                received = billWithPayments.totalReceived,
                remaining = billWithPayments.remaining
            )
        }
    }
}
