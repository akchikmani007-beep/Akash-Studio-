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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.ui.components.AddEditCompanyDialog
import com.example.ui.components.CompanyLogoBadge
import com.example.ui.components.formatRupee
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorRemaining
import com.example.ui.theme.ColorTotalBill
import com.example.ui.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesScreen(
    viewModel: BillingViewModel,
    onViewCompanyBills: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val companies by viewModel.allCompanies.collectAsState()
    val allBills by viewModel.allBills.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCompany by remember { mutableStateOf<Company?>(null) }
    var deletingCompany by remember { mutableStateOf<Company?>(null) }

    if (showAddDialog) {
        AddEditCompanyDialog(
            initialCompany = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, owner, address, mobile, email, gst, logoTag, details ->
                viewModel.addCompany(
                    name = name,
                    ownerName = owner,
                    address = address,
                    mobileNumber = mobile,
                    email = email,
                    gstNumber = gst,
                    logoTag = logoTag,
                    otherDetails = details
                ) {
                    showAddDialog = false
                }
            }
        )
    }

    editingCompany?.let { company ->
        AddEditCompanyDialog(
            initialCompany = company,
            onDismiss = { editingCompany = null },
            onConfirm = { name, owner, address, mobile, email, gst, logoTag, details ->
                viewModel.updateCompany(
                    company.copy(
                        name = name,
                        ownerName = owner,
                        address = address,
                        mobileNumber = mobile,
                        email = email,
                        gstNumber = gst,
                        logoTag = logoTag,
                        otherDetails = details
                    )
                ) {
                    editingCompany = null
                }
            }
        )
    }

    deletingCompany?.let { company ->
        AlertDialog(
            onDismissRequest = { deletingCompany = null },
            title = { Text("Delete Company / Studio?") },
            text = {
                Text("Are you sure you want to delete '${company.name}'? Existing bills will retain historical records.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCompany(company) {
                            deletingCompany = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCompany = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Companies / Studios",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_company_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Company")
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
            Text(
                text = "Manage your multiple photography studios and businesses. Select any company when generating bills.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (companies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Company or Studio Added Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your studio details to print professional bills.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("＋ Add Company / Studio")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
                ) {
                    items(companies, key = { it.id }) { company ->
                        val companyBills = allBills.filter { it.bill.companyId == company.id }
                        val totalBilled = companyBills.sumOf { it.totalBill }
                        val totalReceived = companyBills.sumOf { it.totalReceived }
                        val totalRemaining = (totalBilled - totalReceived).coerceAtLeast(0.0)

                        CompanyCard(
                            company = company,
                            billsCount = companyBills.size,
                            totalBilled = totalBilled,
                            totalReceived = totalReceived,
                            totalRemaining = totalRemaining,
                            onViewBills = { onViewCompanyBills(company.id) },
                            onEdit = { editingCompany = company },
                            onDelete = { deletingCompany = company }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyCard(
    company: Company,
    billsCount: Int,
    totalBilled: Double,
    totalReceived: Double,
    totalRemaining: Double,
    onViewBills: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("company_card_${company.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Logo, Name, Edit/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CompanyLogoBadge(company = company, size = 46)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = company.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (company.ownerName.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Owner: ${company.ownerName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp).testTag("edit_company_${company.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_company_${company.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details section (Mobile, Email, Address, GST)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (company.mobileNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(company.mobileNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (company.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(company.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (company.address.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(company.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                if (company.gstNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("GST: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(company.gstNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Mini Financials for this company
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Billed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupee(totalBilled), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorTotalBill)
                }
                Column {
                    Text("Received", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupee(totalReceived), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorReceived)
                }
                Column {
                    Text("Pending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupee(totalRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (totalRemaining > 0) ColorRemaining else ColorReceived)
                }
                OutlinedButton(
                    onClick = onViewBills,
                    modifier = Modifier.height(34.dp).testTag("view_company_bills_${company.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$billsCount Bills", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
