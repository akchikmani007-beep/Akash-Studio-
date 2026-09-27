package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BillOrder
import com.example.data.model.BillWithPayments
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.data.model.Photographer
import com.example.data.model.PhotographerSummary
import com.example.data.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NewBillFormState(
    val selectedPhotographerId: Long? = null,
    val customerName: String = "",
    val albumType: String = "Wedding Album",
    val pagesText: String = "",
    val rateText: String = "",
    val extraChargesText: String = "0",
    val discountText: String = "0",
    val costExpenseText: String = "",
    val advanceAmountText: String = "",
    val paymentMethod: String = "UPI",
    val notes: String = "",
    val errorMessage: String? = null
) {
    val pages: Int get() = pagesText.toIntOrNull() ?: 0
    val rate: Double get() = rateText.toDoubleOrNull() ?: 0.0
    val extraCharges: Double get() = extraChargesText.toDoubleOrNull() ?: 0.0
    val discount: Double get() = discountText.toDoubleOrNull() ?: 0.0
    val costExpense: Double get() = costExpenseText.toDoubleOrNull() ?: 0.0
    val advanceAmount: Double get() = advanceAmountText.toDoubleOrNull() ?: 0.0

    val calculatedTotalBill: Double
        get() = ((pages * rate) + extraCharges - discount).coerceAtLeast(0.0)

    val calculatedReceived: Double
        get() = advanceAmount

    val calculatedRemaining: Double
        get() = (calculatedTotalBill - calculatedReceived).coerceAtLeast(0.0)

    val paymentPercentage: Double
        get() = if (calculatedTotalBill > 0.0) {
            ((calculatedReceived / calculatedTotalBill) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

    val calculatedStatus: PaymentStatus
        get() = when {
            calculatedReceived <= 0.001 -> PaymentStatus.UNPAID
            calculatedReceived >= calculatedTotalBill - 0.001 && calculatedTotalBill > 0.0 -> PaymentStatus.FULLY_PAID
            else -> PaymentStatus.PARTIALLY_PAID
        }

    val isAdvanceGreaterThanTotal: Boolean
        get() = calculatedTotalBill > 0.0 && advanceAmount > calculatedTotalBill

    val validationError: String?
        get() = when {
            isAdvanceGreaterThanTotal -> "Received amount cannot be greater than the total bill."
            selectedPhotographerId == null -> "Please select a photographer."
            customerName.isBlank() -> "Please enter customer name."
            pages <= 0 -> "Pages must be greater than 0."
            rate <= 0.0 -> "Rate must be greater than 0."
            else -> null
        }

    val canSave: Boolean
        get() = validationError == null
}

data class DashboardMetrics(
    val totalBilling: Double = 0.0,
    val totalReceived: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val netProfit: Double = 0.0
)

data class MonthlyReport(
    val monthYearKey: String, // "Sep 2026"
    val totalBilled: Double,
    val totalReceived: Double,
    val totalPending: Double,
    val totalCost: Double,
    val netProfit: Double,
    val orderCount: Int
)

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillingRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BillingRepository(db.studioDao())
    }

    val allBills: StateFlow<List<BillWithPayments>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhotographers: StateFlow<List<Photographer>> = repository.allPhotographers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form state for creating a new bill
    val newBillForm = MutableStateFlow(NewBillFormState())

    // Dashboard metrics automatically computed from bills
    val dashboardMetrics: StateFlow<DashboardMetrics> = allBills.combine(allBills) { bills, _ ->
        val totalBilling = bills.sumOf { it.totalBill }
        val totalReceived = bills.sumOf { it.totalReceived }
        val totalRemaining = bills.sumOf { it.remaining }
        val netProfit = bills.sumOf { it.netProfit }
        DashboardMetrics(
            totalBilling = totalBilling,
            totalReceived = totalReceived,
            totalRemaining = totalRemaining,
            netProfit = netProfit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Photographer summaries
    val photographerSummaries: StateFlow<List<PhotographerSummary>> =
        combine(allPhotographers, allBills) { photographers, bills ->
            photographers.map { photographer ->
                val photoBills = bills.filter { it.bill.photographerId == photographer.id }
                val billed = photoBills.sumOf { it.totalBill }
                val received = photoBills.sumOf { it.totalReceived }
                val remaining = (billed - received).coerceAtLeast(0.0)
                val latest = photoBills.firstOrNull()?.bill?.customerName ?: "No orders yet"
                val status = when {
                    photoBills.isEmpty() || received <= 0.001 -> PaymentStatus.UNPAID
                    received >= billed - 0.001 -> PaymentStatus.FULLY_PAID
                    else -> PaymentStatus.PARTIALLY_PAID
                }
                PhotographerSummary(
                    photographer = photographer,
                    totalBilled = billed,
                    totalReceived = received,
                    totalRemaining = remaining,
                    orderCount = photoBills.size,
                    latestCustomer = latest,
                    status = status
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly reports
    val monthlyReports: StateFlow<List<MonthlyReport>> = allBills.combine(allBills) { bills, _ ->
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val grouped = bills.groupBy { formatter.format(Date(it.bill.createdDate)) }
        grouped.map { (monthYear, groupBills) ->
            val billed = groupBills.sumOf { it.totalBill }
            val received = groupBills.sumOf { it.totalReceived }
            val pending = groupBills.sumOf { it.remaining }
            val cost = groupBills.sumOf { it.bill.costExpense }
            val profit = groupBills.sumOf { it.netProfit }
            MonthlyReport(
                monthYearKey = monthYear,
                totalBilled = billed,
                totalReceived = received,
                totalPending = pending,
                totalCost = cost,
                netProfit = profit,
                orderCount = groupBills.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders (Bills that are not fully paid and remaining > 0)
    val pendingReminders: StateFlow<List<BillWithPayments>> = allBills.combine(allBills) { bills, _ ->
        bills.filter { it.isPendingReminder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // New Bill Form updates
    fun updateSelectedPhotographer(id: Long) {
        newBillForm.value = newBillForm.value.copy(selectedPhotographerId = id)
    }

    fun updateCustomerName(name: String) {
        newBillForm.value = newBillForm.value.copy(customerName = name)
    }

    fun updateAlbumType(albumType: String) {
        newBillForm.value = newBillForm.value.copy(albumType = albumType)
    }

    fun updatePages(pages: String) {
        newBillForm.value = newBillForm.value.copy(pagesText = pages)
    }

    fun updateRate(rate: String) {
        newBillForm.value = newBillForm.value.copy(rateText = rate)
    }

    fun updateExtraCharges(charges: String) {
        newBillForm.value = newBillForm.value.copy(extraChargesText = charges)
    }

    fun updateDiscount(discount: String) {
        newBillForm.value = newBillForm.value.copy(discountText = discount)
    }

    fun updateCostExpense(cost: String) {
        newBillForm.value = newBillForm.value.copy(costExpenseText = cost)
    }

    fun updateAdvanceAmount(advance: String) {
        newBillForm.value = newBillForm.value.copy(advanceAmountText = advance)
    }

    fun updatePaymentMethod(method: String) {
        newBillForm.value = newBillForm.value.copy(paymentMethod = method)
    }

    fun updateNotes(notes: String) {
        newBillForm.value = newBillForm.value.copy(notes = notes)
    }

    fun resetNewBillForm() {
        newBillForm.value = NewBillFormState()
    }

    fun saveNewBill(onSuccess: (Long) -> Unit) {
        val state = newBillForm.value
        val photogId = state.selectedPhotographerId ?: return
        val photographer = allPhotographers.value.firstOrNull { it.id == photogId }
        val photogName = photographer?.name ?: "Unknown Photographer"

        val bill = BillOrder(
            photographerId = photogId,
            photographerName = photogName,
            customerName = state.customerName.trim(),
            albumType = state.albumType.trim().ifEmpty { "Photo Album" },
            pages = state.pages,
            ratePerPage = state.rate,
            extraCharges = state.extraCharges,
            discount = state.discount,
            totalBill = state.calculatedTotalBill,
            costExpense = state.costExpense,
            notes = state.notes.trim()
        )

        viewModelScope.launch {
            val billId = repository.createBillWithAdvance(
                bill = bill,
                advanceAmount = state.advanceAmount,
                paymentMethod = state.paymentMethod,
                paymentDate = System.currentTimeMillis()
            )
            resetNewBillForm()
            onSuccess(billId)
        }
    }

    // Add Payment to an existing bill
    fun addPayment(
        billId: Long,
        photographerId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addPayment(
                PaymentRecord(
                    billId = billId,
                    photographerId = photographerId,
                    amount = amount,
                    paymentDate = System.currentTimeMillis(),
                    paymentMethod = paymentMethod,
                    paymentType = "Payment",
                    notes = notes
                )
            )
            onComplete()
        }
    }

    // Edit an existing payment
    fun updatePayment(
        payment: PaymentRecord,
        newAmount: Double,
        newMethod: String,
        newNotes: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updatePayment(
                payment.copy(
                    amount = newAmount,
                    paymentMethod = newMethod,
                    notes = newNotes
                )
            )
            onComplete()
        }
    }

    // Delete a payment
    fun deletePayment(payment: PaymentRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            onComplete()
        }
    }

    // Edit a bill
    fun updateBill(bill: BillOrder, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateBill(bill)
            onComplete()
        }
    }

    // Delete a bill
    fun deleteBill(bill: BillOrder, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            onComplete()
        }
    }

    // Add Photographer
    fun addPhotographer(
        name: String,
        phone: String,
        studioName: String,
        notes: String,
        onCreated: (Long) -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.insertPhotographer(
                Photographer(
                    name = name.trim(),
                    phoneNumber = phone.trim(),
                    studioName = studioName.trim(),
                    notes = notes.trim()
                )
            )
            onCreated(id)
        }
    }

    fun updatePhotographer(photographer: Photographer, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePhotographer(photographer)
            onComplete()
        }
    }

    fun deletePhotographer(photographer: Photographer, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePhotographer(photographer)
            onComplete()
        }
    }
}
