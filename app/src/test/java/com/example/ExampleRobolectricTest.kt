package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BillOrder
import com.example.data.model.BillWithPayments
import com.example.data.model.Company
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
    fun `test case 19 live calculation and progressive payment payments with company`() {
        // Step 1: User selects Company 1, enters Pages: 25, Rate: 100, Advance: 500
        val form = NewBillFormState(
            selectedCompanyId = 1L,
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

        // Step 2: Saved bill with company and advance 500
        val bill = BillOrder(
            id = 1L,
            companyId = 1L,
            companyName = "Akash Photo Studio",
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

        assertEquals("INV-1001", billWithPayments.displayBillNumber)
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
        // No reminder shown for fully paid bill
        assertFalse(billWithPayments.isPendingReminder)
    }

    @Test
    fun `test validation requires company selection`() {
        val formWithoutCompany = NewBillFormState(
            selectedCompanyId = null,
            selectedPhotographerId = 1L,
            customerName = "Rahul & Pooja",
            pagesText = "25",
            rateText = "100"
        )
        assertFalse(formWithoutCompany.canSave)
        assertEquals("Please select a Company / Studio.", formWithoutCompany.validationError)
    }

    @Test
    fun `test validation prevents negative amounts`() {
        val formWithNegative = NewBillFormState(
            selectedCompanyId = 1L,
            selectedPhotographerId = 1L,
            customerName = "Rahul & Pooja",
            pagesText = "-5",
            rateText = "100"
        )
        assertFalse(formWithNegative.canSave)
        assertEquals("Negative amounts are not allowed.", formWithNegative.validationError)
    }

    @Test
    fun `test validation prevents advance greater than total bill`() {
        val form = NewBillFormState(
            selectedCompanyId = 1L,
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
    fun `test multi company creation and custom company details`() {
        val company1 = Company(
            id = 1L,
            name = "Akash Photo Studio",
            ownerName = "Akash",
            mobileNumber = "84 46 46 0312"
        )
        val company2 = Company(
            id = 2L,
            name = "ABC Digital Studio",
            ownerName = "Amit Sharma",
            mobileNumber = "9876543210"
        )

        assertEquals("Akash Photo Studio", company1.name)
        assertEquals("84 46 46 0312", company1.mobileNumber)
        assertEquals("ABC Digital Studio", company2.name)
        assertEquals("Amit Sharma", company2.ownerName)
    }

    @Test
    fun `test whatsapp message format conforms to requirement 15 and uses company details`() {
        val message = CommunicationUtils.buildWhatsAppMessage(
            photographerName = "ABC Photography",
            customerName = "Rahul & Pooja",
            totalBill = 2500.0,
            received = 500.0,
            remaining = 2000.0,
            companyName = "ABC Digital Studio",
            companyPhone = "9876543210"
        )
        assertTrue(message.contains("Hello ABC Photography"))
        assertTrue(message.contains("Customer: Rahul & Pooja"))
        assertTrue(message.contains("Total Bill: ₹2,500"))
        assertTrue(message.contains("Received: ₹500"))
        assertTrue(message.contains("Remaining: ₹2,000"))
        assertTrue(message.contains("ABC Digital Studio"))
        assertTrue(message.contains("9876543210"))
    }

    @Test
    fun `test invoice share text contains full invoice layout`() {
        val bill = BillOrder(
            id = 101L,
            companyId = 1L,
            companyName = "Akash Photo Studio",
            photographerId = 1L,
            photographerName = "ABC Photography",
            customerName = "Rahul & Pooja",
            customerPhone = "9822012345",
            customerAddress = "Pune",
            albumType = "Wedding Album",
            pages = 25,
            ratePerPage = 100.0,
            totalBill = 2500.0
        )
        val billWithPayments = BillWithPayments(
            bill = bill,
            payments = listOf(PaymentRecord(id = 1L, billId = 101L, photographerId = 1L, amount = 500.0))
        )
        val company = Company(
            id = 1L,
            name = "Akash Photo Studio",
            ownerName = "Akash",
            mobileNumber = "84 46 46 0312",
            gstNumber = "27AAAAA0000A1Z5"
        )

        val invoiceText = CommunicationUtils.buildInvoiceShareText(billWithPayments, company)
        assertTrue(invoiceText.contains("TAX INVOICE / BILL"))
        assertTrue(invoiceText.contains("Akash Photo Studio"))
        assertTrue(invoiceText.contains("27AAAAA0000A1Z5"))
        assertTrue(invoiceText.contains("Rahul & Pooja"))
        assertTrue(invoiceText.contains("TOTAL BILL    : ₹2,500"))
        assertTrue(invoiceText.contains("RECEIVED      : ₹500"))
        assertTrue(invoiceText.contains("REMAINING     : ₹2,000"))
    }
}
