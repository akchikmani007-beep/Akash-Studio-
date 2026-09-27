package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.ui.components.formatRupee

object CommunicationUtils {

    const val STUDIO_NAME = "Akash Photo Studio & Gift Shop"
    const val STUDIO_PHONE = "84 46 46 0312"

    fun buildWhatsAppMessage(
        photographerName: String,
        customerName: String,
        totalBill: Double,
        received: Double,
        remaining: Double
    ): String {
        return """
Hello $photographerName,

Album Billing Details

Customer: $customerName

Total Bill: ${formatRupee(totalBill)}
Received: ${formatRupee(received)}
Remaining: ${formatRupee(remaining)}

Thank you.
$STUDIO_NAME
$STUDIO_PHONE
        """.trimIndent()
    }

    fun buildReminderMessage(
        photographerName: String,
        customerName: String,
        remaining: Double
    ): String {
        return """
Hello $photographerName,

Friendly Reminder:
Your bill for customer "$customerName" has a pending balance.

Pending Amount: ${formatRupee(remaining)}

Please arrange for payment at your earliest convenience.

Thank you.
$STUDIO_NAME
$STUDIO_PHONE
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
}
