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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.components.AddPhotographerDialog
import com.example.ui.components.DashboardFourCards
import com.example.ui.components.PaymentProgressBar
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.QuickFinancialStatusBar
import com.example.ui.components.ThreeAmountsRow
import com.example.ui.components.formatRupee
import com.example.ui.viewmodel.BillingViewModel
import com.example.util.CommunicationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BillingViewModel,
    onNavigateToNewBill: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onBillClick: (Long) -> Unit,
    onSeeAllBills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val allBills by viewModel.allBills.collectAsState()
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val photographers by viewModel.allPhotographers.collectAsState()

    var showAddPhotogDialog by remember { mutableStateOf(false) }

    if (showAddPhotogDialog) {
        AddPhotographerDialog(
            onDismiss = { showAddPhotogDialog = false },
            onConfirm = { name, phone, studio, notes ->
                viewModel.addPhotographer(name, phone, studio, notes) {
                    showAddPhotogDialog = false
                }
            }
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Studio Branding
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📸", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Akash Photo Studio",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "& Gift Shop • 84 46 46 0312",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Pending reminders alert button
                    if (pendingReminders.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .clickable { onNavigateToReminders() }
                                .testTag("reminders_badge_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Reminders",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${pendingReminders.size} Pending",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Requirement 8: Four Prominent Financial Cards
            item {
                DashboardFourCards(
                    totalBilling = metrics.totalBilling,
                    totalReceived = metrics.totalReceived,
                    totalRemaining = metrics.totalRemaining,
                    netProfit = metrics.netProfit
                )
            }

            // Quick Actions: New Bill, Add Photographer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToNewBill,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_new_bill_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Bill", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showAddPhotogDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_add_photog_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photographer", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Pending Reminders Notice Card if any
            if (pendingReminders.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReminders() }
                            .testTag("dashboard_reminder_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${pendingReminders.size} Bills with Pending Balances",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "Total Pending: ${formatRupee(metrics.totalRemaining)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View reminders",
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Recent Bills Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT BILLS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Total: ${allBills.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allBills.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No bills created yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = onNavigateToNewBill) {
                                Text("Create First Bill")
                            }
                        }
                    }
                }
            } else {
                items(allBills.take(10), key = { it.bill.id }) { billWithPayments ->
                    DashboardBillCard(
                        billWithPayments = billWithPayments,
                        onClick = { onBillClick(billWithPayments.bill.id) },
                        onWhatsApp = {
                            val photog = photographers.firstOrNull { it.id == billWithPayments.bill.photographerId }
                            val phone = photog?.phoneNumber ?: ""
                            val msg = CommunicationUtils.buildWhatsAppMessage(
                                photographerName = billWithPayments.bill.photographerName,
                                customerName = billWithPayments.bill.customerName,
                                totalBill = billWithPayments.totalBill,
                                received = billWithPayments.totalReceived,
                                remaining = billWithPayments.remaining
                            )
                            CommunicationUtils.openWhatsApp(context, phone, msg)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Requirement 18: Dashboard Recent Bill item with prominent three amounts & quick financial status
 */
@Composable
private fun DashboardBillCard(
    billWithPayments: BillWithPayments,
    onClick: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    val dateStr = formatter.format(Date(billWithPayments.bill.createdDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dashboard_bill_${billWithPayments.bill.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = billWithPayments.bill.photographerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${billWithPayments.bill.customerName} • ${billWithPayments.bill.albumType} ($dateStr)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(status = billWithPayments.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Three amounts row (Requirement 2 & 17)
            ThreeAmountsRow(
                totalBill = billWithPayments.totalBill,
                received = billWithPayments.totalReceived,
                remaining = billWithPayments.remaining
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Compact progress bar
            PaymentProgressBar(
                totalBill = billWithPayments.totalBill,
                received = billWithPayments.totalReceived,
                remaining = billWithPayments.remaining
            )
        }
    }
}
