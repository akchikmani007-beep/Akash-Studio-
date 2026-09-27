package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BillOrder
import com.example.data.model.BillWithPayments
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.ui.viewmodel.NewBillFormState
import com.example.util.CommunicationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `app name is configured correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Akash Studio Billing", appName)
    }

    @Test
    fun `test case 19 live calculation and progressive payment payments`() {
        // Step 1: User enters Pages: 25, Rate: 100, Advance: 500
        val form = NewBillFormState(
            selectedPhotographerId = 1L,
            customerName = "Rahul & Pooja",
            albumType = "Wedding Album",
            pagesText = "25",
            rateText = "100",
            advanceAmountText = "500"
        )

        // Live calculation asserts
        assertEquals(2500.0, form.calculatedTotalBill, 0.001)
        assertEquals(500.0, form.calculatedReceived, 0.001)
        assertEquals(2000.0, form.calculatedRemaining, 0.001)
        assertEquals(PaymentStatus.PARTIALLY_PAID, form.calculatedStatus)
        assertEquals(20.0, form.paymentPercentage, 0.001)
        assertTrue(form.canSave)

        // Step 2: Saved bill with advance 500
        val bill = BillOrder(
            id = 1L,
            photographerId = 1L,
            photographerName = "ABC Photography",
            customerName = "Rahul & Pooja",
            albumType = "Wedding Album",
            pages = 25,
            ratePerPage = 100.0,
            totalBill = 2500.0
        )
        val initialPayments = listOf(
            PaymentRecord(id = 1L, billId = 1L, photographerId = 1L, amount = 500.0, paymentType = "Advance")
        )
        var billWithPayments = BillWithPayments(bill = bill, payments = initialPayments)

        assertEquals(2500.0, billWithPayments.totalBill, 0.001)
        assertEquals(500.0, billWithPayments.totalReceived, 0.001)
        assertEquals(2000.0, billWithPayments.remaining, 0.001)
        assertEquals(PaymentStatus.PARTIALLY_PAID, billWithPayments.status)
        assertTrue(billWithPayments.isPendingReminder)

        // Step 3: Add payment of ₹1,000 -> Total: 2500, Received: 1500, Remaining: 1000
        val payment2 = PaymentRecord(id = 2L, billId = 1L, photographerId = 1L, amount = 1000.0, paymentType = "Payment")
        billWithPayments = billWithPayments.copy(payments = initialPayments + payment2)

        assertEquals(2500.0, billWithPayments.totalBill, 0.001)
        assertEquals(1500.0, billWithPayments.totalReceived, 0.001)
        assertEquals(1000.0, billWithPayments.remaining, 0.001)
        assertEquals(PaymentStatus.PARTIALLY_PAID, billWithPayments.status)
        assertTrue(billWithPayments.isPendingReminder)

        // Step 4: Add another payment of ₹1,000 -> Total: 2500, Received: 2500, Remaining: 0 -> FULLY PAID
        val payment3 = PaymentRecord(id = 3L, billId = 1L, photographerId = 1L, amount = 1000.0, paymentType = "Payment")
        billWithPayments = billWithPayments.copy(payments = initialPayments + payment2 + payment3)

        assertEquals(2500.0, billWithPayments.totalBill, 0.001)
        assertEquals(2500.0, billWithPayments.totalReceived, 0.001)
        assertEquals(0.0, billWithPayments.remaining, 0.001)
        assertEquals(PaymentStatus.FULLY_PAID, billWithPayments.status)
        // No reminder shown for fully paid bill (Requirement 16 & 19)
        assertFalse(billWithPayments.isPendingReminder)
    }

    @Test
    fun `test validation prevents advance greater than total bill`() {
        val form = NewBillFormState(
            selectedPhotographerId = 1L,
            customerName = "Rahul & Pooja",
            pagesText = "25",
            rateText = "100",
            advanceAmountText = "3000" // greater than 2500
        )
        assertTrue(form.isAdvanceGreaterThanTotal)
        assertEquals("Received amount cannot be greater than the total bill.", form.validationError)
        assertFalse(form.canSave)
    }

    @Test
    fun `test whatsapp message format conforms to requirement 15`() {
        val message = CommunicationUtils.buildWhatsAppMessage(
            photographerName = "ABC Photography",
            customerName = "Rahul & Pooja",
            totalBill = 2500.0,
            received = 500.0,
            remaining = 2000.0
        )
        assertTrue(message.contains("Hello ABC Photography"))
        assertTrue(message.contains("Customer: Rahul & Pooja"))
        assertTrue(message.contains("Total Bill: ₹2,500"))
        assertTrue(message.contains("Received: ₹500"))
        assertTrue(message.contains("Remaining: ₹2,000"))
        assertTrue(message.contains("Akash Photo Studio & Gift Shop"))
        assertTrue(message.contains("84 46 46 0312"))
    }

    @Test
    fun `test reminder message uses remaining amount not original bill`() {
        val reminder = CommunicationUtils.buildReminderMessage(
            photographerName = "ABC Photography",
            customerName = "Rahul & Pooja",
            remaining = 2000.0
        )
        assertTrue(reminder.contains("Pending Amount: ₹2,000"))
        assertFalse(reminder.contains("2,500"))
    }
}
