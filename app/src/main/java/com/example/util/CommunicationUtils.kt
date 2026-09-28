package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.BillWithPayments
import com.example.data.model.Company
import com.example.ui.components.formatRupee
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommunicationUtils {

    const val STUDIO_NAME = "Akash Photo Studio & Gift Shop"
    const val STUDIO_PHONE = "84 46 46 0312"

    fun buildWhatsAppMessage(
        photographerName: String,
        customerName: String,
        totalBill: Double,
        received: Double,
        remaining: Double,
        companyName: String = STUDIO_NAME,
        companyPhone: String = STUDIO_PHONE
    ): String {
        val name = if (companyName.isNotBlank()) companyName else STUDIO_NAME
        val phone = if (companyPhone.isNotBlank()) companyPhone else STUDIO_PHONE
        return """
Hello $photographerName,

Album Billing Details

Customer: $customerName

Total Bill: ${formatRupee(totalBill)}
Received: ${formatRupee(received)}
Remaining: ${formatRupee(remaining)}

Thank you.
$name
$phone
        """.trimIndent()
    }

    fun buildReminderMessage(
        photographerName: String,
        customerName: String,
        remaining: Double,
        companyName: String = STUDIO_NAME,
        companyPhone: String = STUDIO_PHONE
    ): String {
        val name = if (companyName.isNotBlank()) companyName else STUDIO_NAME
        val phone = if (companyPhone.isNotBlank()) companyPhone else STUDIO_PHONE
        return """
Hello $photographerName,

Friendly Reminder:
Your bill for customer "$customerName" has a pending balance.

Pending Amount: ${formatRupee(remaining)}

Please arrange for payment at your earliest convenience.

Thank you.
$name
$phone
        """.trimIndent()
    }

    fun buildInvoiceShareText(
        billWithPayments: BillWithPayments,
        company: Company?
    ): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateStr = formatter.format(Date(billWithPayments.bill.createdDate))
        val compName = company?.name ?: billWithPayments.bill.companyName.ifBlank { STUDIO_NAME }
        val compPhone = company?.mobileNumber ?: STUDIO_PHONE
        val compAddress = company?.address ?: ""
        val compGst = if (!company?.gstNumber.isNullOrBlank()) "GST: ${company?.gstNumber}\n" else ""

        val billNumber = billWithPayments.displayBillNumber

        return """
═════════════════════════════════
         TAX INVOICE / BILL      
═════════════════════════════════
$compName
$compAddress
Phone: $compPhone
$compGst
Invoice No : $billNumber
Bill Date  : $dateStr

BILL TO (CUSTOMER):
Customer Name : ${billWithPayments.bill.customerName}
Phone         : ${billWithPayments.bill.customerPhone.ifBlank { "N/A" }}
Address       : ${billWithPayments.bill.customerAddress.ifBlank { "N/A" }}
Photographer  : ${billWithPayments.bill.photographerName}

ORDER PARTICULARS:
Item / Service: ${billWithPayments.bill.albumType}
Pages / Qty   : ${billWithPayments.bill.pages}
Rate / Page   : ${formatRupee(billWithPayments.bill.ratePerPage)}
Subtotal      : ${formatRupee(billWithPayments.bill.pages * billWithPayments.bill.ratePerPage)}
Extra Charges : ${formatRupee(billWithPayments.bill.extraCharges)}
Discount      : ${formatRupee(billWithPayments.bill.discount)}
─────────────────────────────────
TOTAL BILL    : ${formatRupee(billWithPayments.totalBill)}
RECEIVED      : ${formatRupee(billWithPayments.totalReceived)}
REMAINING     : ${formatRupee(billWithPayments.remaining)}
PAYMENT STATUS: ${billWithPayments.status.emoji} ${billWithPayments.status.label}
═════════════════════════════════
Thank you for your business!
$compName
═════════════════════════════════
        """.trimIndent()
    }

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=" + Uri.encode(message))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Bill Details"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber")).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhone(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not dial phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(intent, subject))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share invoice", Toast.LENGTH_SHORT).show()
        }
    }
}
