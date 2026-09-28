package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.AddEditCompanyDialog
import com.example.ui.components.AddPhotographerDialog
import com.example.ui.components.CompanyLogoBadge
import com.example.ui.components.DashboardFourCards
import com.example.ui.components.PaymentProgressBar
import com.example.ui.components.PaymentStatusBadge
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
    onNavigateToCompanies: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onBillClick: (Long) -> Unit,
    onSeeAllBills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val filteredBills by viewModel.filteredBills.collectAsState()
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val photographers by viewModel.allPhotographers.collectAsState()
    val companies by viewModel.allCompanies.collectAsState()
    val selectedCompanyFilter by viewModel.selectedCompanyFilter.collectAsState()

    var showAddCompanyDialog by remember { mutableStateOf(false) }

    if (showAddCompanyDialog) {
        AddEditCompanyDialog(
            initialCompany = null,
            onDismiss = { showAddCompanyDialog = false },
            onConfirm = { name, owner, address, mobile, email, gst, logoTag, details ->
                viewModel.addCompany(name, owner, address, mobile, email, gst, logoTag, details) {
                    showAddCompanyDialog = false
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
            // Header
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
                                text = "Studio Billing",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Multi-Company Studio Accounts",
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

            // Requirement 5: Company Filter Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPANY FILTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Manage (${companies.size})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToCompanies() }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedCompanyFilter == null,
                            onClick = { viewModel.setCompanyFilter(null) },
                            label = { Text("All Companies", fontWeight = if (selectedCompanyFilter == null) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (selectedCompanyFilter == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_all_companies")
                        )

                        companies.forEach { comp ->
                            val isSelected = selectedCompanyFilter == comp.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCompanyFilter(if (isSelected) null else comp.id) },
                                label = { Text(comp.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    CompanyLogoBadge(company = comp, size = 18)
                                },
                                modifier = Modifier.testTag("filter_company_${comp.id}")
                            )
                        }

                        IconButton(
                            onClick = { showAddCompanyDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Company", tint = MaterialTheme.colorScheme.primary)
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

            // Quick Actions: New Bill, Add Company, Photographers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToNewBill,
                        modifier = Modifier.weight(1.2f).height(48.dp).testTag("quick_new_bill_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Bill", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToCompanies,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_companies_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Studios", fontWeight = FontWeight.SemiBold)
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
                                        text = "Pending: ${formatRupee(metrics.totalRemaining)}",
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

            // Recent Bills Header (Requirement 5)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BILLING HISTORY (${filteredBills.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (filteredBills.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (selectedCompanyFilter != null) "No bills for this company yet" else "No bills created yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = onNavigateToNewBill) {
                                Text("Create First Bill")
                            }
                        }
                    }
                }
            } else {
                items(filteredBills, key = { it.bill.id }) { billWithPayments ->
                    val billCompany = companies.firstOrNull { it.id == billWithPayments.bill.companyId }
                    DashboardBillCard(
                        billWithPayments = billWithPayments,
                        company = billCompany,
                        onClick = { onBillClick(billWithPayments.bill.id) },
                        onWhatsApp = {
                            val photog = photographers.firstOrNull { it.id == billWithPayments.bill.photographerId }
                            val phone = photog?.phoneNumber ?: ""
                            val msg = CommunicationUtils.buildWhatsAppMessage(
                                photographerName = billWithPayments.bill.photographerName,
                                customerName = billWithPayments.bill.customerName,
                                totalBill = billWithPayments.totalBill,
                                received = billWithPayments.totalReceived,
                                remaining = billWithPayments.remaining,
                                companyName = billCompany?.name ?: billWithPayments.bill.companyName,
                                companyPhone = billCompany?.mobileNumber ?: ""
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
 * Requirement 5: Billing History Card showing:
 * - Bill Number
 * - Date
 * - Customer Name
 * - Company / Studio Name
 * - Total Amount
 * - Received / Advance Amount
 * - Remaining Amount
 * - Payment Status
 */
@Composable
private fun DashboardBillCard(
    billWithPayments: BillWithPayments,
    company: com.example.data.model.Company?,
    onClick: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = formatter.format(Date(billWithPayments.bill.createdDate))
    val compName = company?.name ?: billWithPayments.bill.companyName.ifBlank { "Akash Photo Studio" }

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
            // Header with Bill Number, Company Name, and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (company != null) {
                        CompanyLogoBadge(company = company, size = 32)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = billWithPayments.displayBillNumber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • $dateStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = compName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                PaymentStatusBadge(status = billWithPayments.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Customer & Album line
            Text(
                text = "${billWithPayments.bill.customerName} — ${billWithPayments.bill.albumType} (Photog: ${billWithPayments.bill.photographerName})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Three amounts row (Requirement 2 & 6 & 17)
            ThreeAmountsRow(
                totalBill = billWithPayments.totalBill,
                received = billWithPayments.totalReceived,
                remaining = billWithPayments.remaining
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            PaymentProgressBar(
                totalBill = billWithPayments.totalBill,
                received = billWithPayments.totalReceived,
                remaining = billWithPayments.remaining
            )
        }
    }
}
