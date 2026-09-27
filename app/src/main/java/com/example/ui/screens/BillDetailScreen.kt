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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillWithPayments
import com.example.data.model.PaymentRecord
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.EditPaymentDialog
import com.example.ui.components.PaymentProgressBar
import com.example.ui.components.PaymentStatusBadge
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
fun BillDetailScreen(
    billId: Long,
    viewModel: BillingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBills by viewModel.allBills.collectAsState()
    val photographers by viewModel.allPhotographers.collectAsState()

    val billWithPayments = allBills.firstOrNull { it.bill.id == billId }
    val photographer = photographers.firstOrNull { it.id == billWithPayments?.bill?.photographerId }

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<PaymentRecord?>(null) }
    var deletingPayment by remember { mutableStateOf<PaymentRecord?>(null) }
    var showEditBillDialog by remember { mutableStateOf(false) }
    var showDeleteBillDialog by remember { mutableStateOf(false) }

    if (billWithPayments == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bill not found")
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Dialog: Add Payment (Requirement 10)
    if (showAddPaymentDialog) {
        AddPaymentDialog(
            billWithPayments = billWithPayments,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { amount, method, notes ->
                viewModel.addPayment(
                    billId = billWithPayments.bill.id,
                    photographerId = billWithPayments.bill.photographerId,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
                showAddPaymentDialog = false
            }
        )
    }

    // Dialog: Edit Payment (Requirement 11)
    editingPayment?.let { payment ->
        val otherPaymentsSum = billWithPayments.payments.filter { it.id != payment.id }.sumOf { it.amount }
        EditPaymentDialog(
            payment = payment,
            billTotal = billWithPayments.totalBill,
            otherPaymentsTotal = otherPaymentsSum,
            onDismiss = { editingPayment = null },
            onConfirm = { amount, method, notes ->
                viewModel.updatePayment(payment, amount, method, notes) {
                    editingPayment = null
                }
            }
        )
    }

    // Dialog: Delete Payment Confirmation (Requirement 11)
    deletingPayment?.let { payment ->
        AlertDialog(
            onDismissRequest = { deletingPayment = null },
            title = { Text("Delete Payment?") },
            text = { Text("Are you sure you want to delete this payment of ${formatRupee(payment.amount)}? The bill remaining amount will automatically recalculate.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePayment(payment) {
                            deletingPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Delete Bill Confirmation
    if (showDeleteBillDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteBillDialog = false },
            title = { Text("Delete Bill?") },
            text = { Text("Are you sure you want to delete this bill for ${billWithPayments.bill.customerName}? All related payment records will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBill(billWithPayments.bill) {
                            showDeleteBillDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBillDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Edit Bill Details
    if (showEditBillDialog) {
        var editPages by remember { mutableStateOf(billWithPayments.bill.pages.toString()) }
        var editRate by remember { mutableStateOf(billWithPayments.bill.ratePerPage.toString().replace(".0", "")) }
        var editExtra by remember { mutableStateOf(billWithPayments.bill.extraCharges.toString().replace(".0", "")) }
        var editDiscount by remember { mutableStateOf(billWithPayments.bill.discount.toString().replace(".0", "")) }
        var editCost by remember { mutableStateOf(billWithPayments.bill.costExpense.toString().replace(".0", "")) }
        var editCustomer by remember { mutableStateOf(billWithPayments.bill.customerName) }
        var editAlbum by remember { mutableStateOf(billWithPayments.bill.albumType) }

        val p = editPages.toIntOrNull() ?: 0
        val r = editRate.toDoubleOrNull() ?: 0.0
        val extra = editExtra.toDoubleOrNull() ?: 0.0
        val disc = editDiscount.toDoubleOrNull() ?: 0.0
        val newCalculatedTotal = ((p * r) + extra - disc).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showEditBillDialog = false },
            title = { Text("Edit Bill Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editCustomer,
                        onValueChange = { editCustomer = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAlbum,
                        onValueChange = { editAlbum = it },
                        label = { Text("Album Type") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPages,
                            onValueChange = { editPages = it },
                            label = { Text("Pages") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editRate,
                            onValueChange = { editRate = it },
                            label = { Text("Rate / Page (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editExtra,
                            onValueChange = { editExtra = it },
                            label = { Text("Extra (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editDiscount,
                            onValueChange = { editDiscount = it },
                            label = { Text("Discount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = editCost,
                        onValueChange = { editCost = it },
                        label = { Text("Printing Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "New Total Bill: ${formatRupee(newCalculatedTotal)}",
                        fontWeight = FontWeight.Bold,
                        color = ColorTotalBill,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBill(
                            billWithPayments.bill.copy(
                                customerName = editCustomer,
                                albumType = editAlbum,
                                pages = p,
                                ratePerPage = r,
                                extraCharges = extra,
                                discount = disc,
                                totalBill = newCalculatedTotal,
                                costExpense = editCost.toDoubleOrNull() ?: 0.0
                            )
                        ) {
                            showEditBillDialog = false
                        }
                    },
                    enabled = p > 0 && r > 0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBillDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditBillDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Bill")
                    }
                    IconButton(onClick = { showDeleteBillDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Bill", tint = MaterialTheme.colorScheme.error)
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
            // Requirement 7: ORDER CARD MUST SHOW ALL THREE
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "${billWithPayments.bill.albumType} — ${billWithPayments.bill.customerName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Photographer: ${billWithPayments.bill.photographerName}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Pages: ${billWithPayments.bill.pages}  •  Rate: ${formatRupee(billWithPayments.bill.ratePerPage)}/page",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (billWithPayments.bill.extraCharges > 0 || billWithPayments.bill.discount > 0) {
                                    Text(
                                        text = "Extra: ${formatRupee(billWithPayments.bill.extraCharges)}  •  Discount: ${formatRupee(billWithPayments.bill.discount)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            PaymentStatusBadge(status = billWithPayments.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Three Amounts Row
                        ThreeAmountsRow(
                            totalBill = billWithPayments.totalBill,
                            received = billWithPayments.totalReceived,
                            remaining = billWithPayments.remaining
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Payment Progress Bar (Requirement 13)
                        PaymentProgressBar(
                            totalBill = billWithPayments.totalBill,
                            received = billWithPayments.totalReceived,
                            remaining = billWithPayments.remaining
                        )
                    }
                }
            }

            // Quick Actions: Add Payment, WhatsApp, Share SMS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddPaymentDialog = true },
                        modifier = Modifier.weight(1.3f).height(44.dp).testTag("detail_add_payment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("＋ Payment", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val phone = photographer?.phoneNumber ?: ""
                            val msg = CommunicationUtils.buildWhatsAppMessage(
                                photographerName = billWithPayments.bill.photographerName,
                                customerName = billWithPayments.bill.customerName,
                                totalBill = billWithPayments.totalBill,
                                received = billWithPayments.totalReceived,
                                remaining = billWithPayments.remaining
                            )
                            CommunicationUtils.openWhatsApp(context, phone, msg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1.1f).height(44.dp).testTag("detail_whatsapp_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val phone = photographer?.phoneNumber ?: ""
                            val msg = CommunicationUtils.buildWhatsAppMessage(
                                photographerName = billWithPayments.bill.photographerName,
                                customerName = billWithPayments.bill.customerName,
                                totalBill = billWithPayments.totalBill,
                                received = billWithPayments.totalReceived,
                                remaining = billWithPayments.remaining
                            )
                            CommunicationUtils.sendSms(context, phone, msg)
                        },
                        modifier = Modifier.weight(0.9f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS")
                    }
                }
            }

            // Payment History Header (Requirement 7)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAYMENT HISTORY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${billWithPayments.payments.size} records",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (billWithPayments.payments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No payments recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showAddPaymentDialog = true }) {
                                Text("Add First Payment")
                            }
                        }
                    }
                }
            } else {
                items(billWithPayments.payments, key = { it.id }) { payment ->
                    PaymentHistoryItemCard(
                        payment = payment,
                        dateStr = dateFormatter.format(Date(payment.paymentDate)),
                        onEdit = { editingPayment = payment },
                        onDelete = { deletingPayment = payment }
                    )
                }
            }

            // Additional details at bottom
            item {
                if (billWithPayments.bill.notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Order Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(billWithPayments.bill.notes, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Requirement 7: Payment History item with Edit and Delete
 * "28 Sep — Advance — ₹500 — UPI [✏️ Edit] [🗑️ Delete]"
 */
@Composable
private fun PaymentHistoryItemCard(
    payment: PaymentRecord,
    dateStr: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_history_item_${payment.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRupee(payment.amount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = ColorReceived
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${payment.paymentType}  •  ${payment.paymentMethod}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr + if (payment.notes.isNotBlank()) " — ${payment.notes}" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit and Delete actions
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).testTag("edit_payment_${payment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Payment",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_payment_${payment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
