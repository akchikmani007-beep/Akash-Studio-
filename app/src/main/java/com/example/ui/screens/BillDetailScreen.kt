package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.material3.Surface
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
import com.example.data.model.Company
import com.example.data.model.PaymentRecord
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.CompanyLogoBadge
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
    val companies by viewModel.allCompanies.collectAsState()

    val billWithPayments = allBills.firstOrNull { it.bill.id == billId }
    val photographer = photographers.firstOrNull { it.id == billWithPayments?.bill?.photographerId }
    val company: Company? = companies.firstOrNull { it.id == billWithPayments?.bill?.companyId }
        ?: companies.firstOrNull { it.name.equals(billWithPayments?.bill?.companyName, ignoreCase = true) }

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

    // Dialog: Add Payment
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

    // Dialog: Edit Payment
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

    // Dialog: Delete Payment Confirmation
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
        var editPhone by remember { mutableStateOf(billWithPayments.bill.customerPhone) }
        var editAddress by remember { mutableStateOf(billWithPayments.bill.customerAddress) }
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
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Customer Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Customer Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAlbum,
                        onValueChange = { editAlbum = it },
                        label = { Text("Album Type / Service") },
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
                                customerPhone = editPhone,
                                customerAddress = editAddress,
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

    val companyDisplayName = company?.name ?: billWithPayments.bill.companyName.ifBlank { "Akash Photo Studio" }
    val companyMobile = company?.mobileNumber ?: "84 46 46 0312"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(billWithPayments.displayBillNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(companyDisplayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val invoiceText = CommunicationUtils.buildInvoiceShareText(billWithPayments, company)
                            CommunicationUtils.shareText(context, "Invoice ${billWithPayments.displayBillNumber}", invoiceText)
                        },
                        modifier = Modifier.testTag("share_invoice_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Invoice")
                    }
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
            // Requirement 4 & 8: Professional Bill Header Card (Company Details)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_header_company_card"),
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                if (company != null) {
                                    CompanyLogoBadge(company = company, size = 46)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📸", fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = companyDisplayName,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (company?.ownerName?.isNotBlank() == true) {
                                        Text(
                                            text = "Prop: ${company.ownerName}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            PaymentStatusBadge(status = billWithPayments.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Company Contact details
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(companyMobile, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (company?.email?.isNotBlank() == true) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(company.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (company?.address?.isNotBlank() == true) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(company.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (company?.gstNumber?.isNotBlank() == true) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("GST No: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(company.gstNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Customer & Invoice Details (Requirement 7 & 8)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "BILL TO (CUSTOMER)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = billWithPayments.bill.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (billWithPayments.bill.customerPhone.isNotBlank()) {
                                    Text(
                                        text = "Phone: ${billWithPayments.bill.customerPhone}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (billWithPayments.bill.customerAddress.isNotBlank()) {
                                    Text(
                                        text = billWithPayments.bill.customerAddress,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "INVOICE DETAILS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = billWithPayments.displayBillNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = dateFormatter.format(Date(billWithPayments.bill.createdDate)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Photog: ${billWithPayments.bill.photographerName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Line items breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service / Album", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Qty × Rate", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Amount", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(billWithPayments.bill.albumType, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${billWithPayments.bill.pages} × ${formatRupee(billWithPayments.bill.ratePerPage)}", fontSize = 13.sp)
                            Text(formatRupee(billWithPayments.bill.pages * billWithPayments.bill.ratePerPage), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (billWithPayments.bill.extraCharges > 0 || billWithPayments.bill.discount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            if (billWithPayments.bill.extraCharges > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Extra Charges", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+ ${formatRupee(billWithPayments.bill.extraCharges)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (billWithPayments.bill.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- ${formatRupee(billWithPayments.bill.discount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Three Amounts Row (Requirement 2 & 17)
                        ThreeAmountsRow(
                            totalBill = billWithPayments.totalBill,
                            received = billWithPayments.totalReceived,
                            remaining = billWithPayments.remaining
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Payment Progress Bar (Requirement 13)
                        PaymentProgressBar(
                            totalBill = billWithPayments.totalBill,
                            received = billWithPayments.totalReceived,
                            remaining = billWithPayments.remaining
                        )
                    }
                }
            }

            // Quick Actions: Add Payment, WhatsApp, Share Invoice
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
                                remaining = billWithPayments.remaining,
                                companyName = companyDisplayName,
                                companyPhone = companyMobile
                            )
                            CommunicationUtils.openWhatsApp(context, phone, msg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1.2f).height(44.dp).testTag("detail_whatsapp_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val invoiceText = CommunicationUtils.buildInvoiceShareText(billWithPayments, company)
                            CommunicationUtils.shareText(context, "Invoice ${billWithPayments.displayBillNumber}", invoiceText)
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("detail_print_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invoice")
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

            // Requirement 8: Company Footer
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Thank you for your business!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$companyDisplayName • $companyMobile",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

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
