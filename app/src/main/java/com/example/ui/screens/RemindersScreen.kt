package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillWithPayments
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.ThreeAmountsRow
import com.example.ui.components.formatRupee
import com.example.ui.theme.ColorRemaining
import com.example.ui.viewmodel.BillingViewModel
import com.example.util.CommunicationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: BillingViewModel,
    onBillClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val photographers by viewModel.allPhotographers.collectAsState()

    var paymentTargetBill by remember { mutableStateOf<BillWithPayments?>(null) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = ColorRemaining,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Reminders", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                .padding(horizontal = 16.dp)
        ) {
            if (pendingReminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "All Bills Fully Paid! 🎉",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No pending payments or reminders at this time.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "${pendingReminders.size} pending orders require payment settlement",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
                ) {
                    items(pendingReminders, key = { it.bill.id }) { billWithPayments ->
                        val photog = photographers.firstOrNull { it.id == billWithPayments.bill.photographerId }
                        ReminderCard(
                            billWithPayments = billWithPayments,
                            phoneNumber = photog?.phoneNumber ?: "",
                            onCardClick = { onBillClick(billWithPayments.bill.id) },
                            onAddPayment = { paymentTargetBill = billWithPayments },
                            onSendWhatsApp = {
                                val msg = CommunicationUtils.buildReminderMessage(
                                    photographerName = billWithPayments.bill.photographerName,
                                    customerName = billWithPayments.bill.customerName,
                                    remaining = billWithPayments.remaining
                                )
                                CommunicationUtils.openWhatsApp(
                                    context = context,
                                    phoneNumber = photog?.phoneNumber ?: "",
                                    message = msg
                                )
                            },
                            onCall = {
                                CommunicationUtils.dialPhone(
                                    context = context,
                                    phoneNumber = photog?.phoneNumber ?: ""
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
 * Requirement 16: 10-DAY REMINDER MUST USE REMAINING AMOUNT
 * Shows: “Pending Amount: ₹[Remaining]”
 * Never show original bill amount as pending amount!
 */
@Composable
private fun ReminderCard(
    billWithPayments: BillWithPayments,
    phoneNumber: String,
    onCardClick: () -> Unit,
    onAddPayment: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onCall: () -> Unit
) {
    val daysElapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - billWithPayments.bill.createdDate).toInt()
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = formatter.format(Date(billWithPayments.bill.createdDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("reminder_card_${billWithPayments.bill.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = billWithPayments.bill.photographerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Customer: ${billWithPayments.bill.customerName} (${billWithPayments.bill.albumType})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Billed on $dateStr ($daysElapsed days ago)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (daysElapsed >= 10) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (daysElapsed >= 10) "$daysElapsed d Overdue" else "$daysElapsed d ago",
                        color = if (daysElapsed >= 10) Color(0xFFDC2626) else Color(0xFFD97706),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted Pending Amount Box (Requirement 16)
            Surface(
                color = Color(0xFFFFF7ED),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pending Amount:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9A3412)
                        )
                        Text(
                            text = formatRupee(billWithPayments.remaining),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEA580C)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Bill: ${formatRupee(billWithPayments.totalBill)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Received: ${formatRupee(billWithPayments.totalReceived)}",
                            fontSize = 11.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: WhatsApp, Call, Add Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1.3f).height(40.dp).testTag("reminder_whatsapp_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remind WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onAddPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.weight(1.1f).height(40.dp).testTag("reminder_add_payment_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("＋ Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (phoneNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCall,
                        modifier = Modifier.weight(0.7f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
