package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorTotalBill
import com.example.ui.theme.StudioPrimary

fun getCompanyLogoVector(tag: String): ImageVector {
    return when (tag.lowercase()) {
        "camera" -> Icons.Default.CameraAlt
        "aperture" -> Icons.Default.Camera
        "film" -> Icons.Default.Movie
        "store" -> Icons.Default.Store
        "brush" -> Icons.Default.Brush
        else -> Icons.Default.AutoAwesome
    }
}

fun getCompanyLogoColor(tag: String): Color {
    return when (tag.lowercase()) {
        "camera" -> Color(0xFF2563EB)
        "aperture" -> Color(0xFF7C3AED)
        "film" -> Color(0xFFEA580C)
        "store" -> Color(0xFF0D9488)
        "brush" -> Color(0xFFDB2777)
        else -> Color(0xFF0284C7)
    }
}

@Composable
fun CompanyLogoBadge(
    company: Company,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val icon = getCompanyLogoVector(company.logoTag)
    val color = getCompanyLogoColor(company.logoTag)

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.14f), CircleShape)
            .border(1.5.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = company.name,
            tint = color,
            modifier = Modifier.size((size * 0.52).dp)
        )
    }
}

@Composable
fun AddEditCompanyDialog(
    initialCompany: Company? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        owner: String,
        address: String,
        mobile: String,
        email: String,
        gst: String,
        logoTag: String,
        details: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialCompany?.name ?: "") }
    var owner by remember { mutableStateOf(initialCompany?.ownerName ?: "") }
    var address by remember { mutableStateOf(initialCompany?.address ?: "") }
    var mobile by remember { mutableStateOf(initialCompany?.mobileNumber ?: "") }
    var email by remember { mutableStateOf(initialCompany?.email ?: "") }
    var gst by remember { mutableStateOf(initialCompany?.gstNumber ?: "") }
    var logoTag by remember { mutableStateOf(initialCompany?.logoTag ?: "camera") }
    var details by remember { mutableStateOf(initialCompany?.otherDetails ?: "") }

    val iconOptions = listOf(
        "camera" to "Camera",
        "aperture" to "Studio",
        "film" to "Cinematic",
        "store" to "Shop",
        "brush" to "Artistic",
        "star" to "Premium"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCompany == null) "Add Company / Studio" else "Edit Company / Studio",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company / Studio Name *") },
                    placeholder = { Text("e.g. Akash Photo Studio") },
                    modifier = Modifier.fillMaxWidth().testTag("company_name_input"),
                    singleLine = true,
                    isError = name.isBlank()
                )

                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner Name") },
                    placeholder = { Text("e.g. Akash") },
                    modifier = Modifier.fillMaxWidth().testTag("company_owner_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number (for Bills & WhatsApp) *") },
                    placeholder = { Text("e.g. 84 46 46 0312") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("company_mobile_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("company_email_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Studio Address") },
                    placeholder = { Text("e.g. Main Road, Market Area") },
                    modifier = Modifier.fillMaxWidth().testTag("company_address_input"),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = gst,
                    onValueChange = { gst = it },
                    label = { Text("GST Number (Optional)") },
                    placeholder = { Text("e.g. 27AAAAA0000A1Z5") },
                    modifier = Modifier.fillMaxWidth().testTag("company_gst_input"),
                    singleLine = true
                )

                // Logo / Icon Selector
                Text(
                    text = "Select Logo Icon:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { (tag, label) ->
                        val isSelected = logoTag == tag
                        val color = getCompanyLogoColor(tag)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color else color.copy(alpha = 0.12f))
                                .border(
                                    1.5.dp,
                                    if (isSelected) color else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { logoTag = tag },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCompanyLogoVector(tag),
                                contentDescription = label,
                                tint = if (isSelected) Color.White else color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Other Details / Tagline") },
                    placeholder = { Text("e.g. Wedding Photography Specialists") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, owner, address, mobile, email, gst, logoTag, details)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_company_btn")
            ) {
                Text(if (initialCompany == null) "Create Studio" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
