package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

enum class PaymentStatus(val label: String, val emoji: String) {
    UNPAID("UNPAID", "🔴"),
    PARTIALLY_PAID("PARTIALLY PAID", "🟡"),
    FULLY_PAID("FULLY PAID", "🟢")
}

data class BillWithPayments(
    @Embedded val bill: BillOrder,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val payments: List<PaymentRecord> = emptyList()
) {
    val totalBill: Double get() = bill.totalBill
    val totalReceived: Double get() = payments.sumOf { it.amount }
    val remaining: Double get() = (totalBill - totalReceived).coerceAtLeast(0.0)
    val netProfit: Double get() = totalBill - bill.costExpense

    val paymentPercentage: Double
        get() = if (totalBill > 0.0) {
            ((totalReceived / totalBill) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

    val status: PaymentStatus
        get() = when {
            totalReceived <= 0.001 -> PaymentStatus.UNPAID
            totalReceived >= totalBill - 0.001 -> PaymentStatus.FULLY_PAID
            else -> PaymentStatus.PARTIALLY_PAID
        }

    val isPendingReminder: Boolean
        get() = status != PaymentStatus.FULLY_PAID && remaining > 0.0
}

data class PhotographerSummary(
    val photographer: Photographer,
    val totalBilled: Double,
    val totalReceived: Double,
    val totalRemaining: Double,
    val orderCount: Int,
    val latestCustomer: String,
    val status: PaymentStatus
)
