package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_orders",
    foreignKeys = [
        ForeignKey(
            entity = Photographer::class,
            parentColumns = ["id"],
            childColumns = ["photographerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("photographerId"), Index("companyId")]
)
data class BillOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photographerId: Long,
    val photographerName: String,
    val companyId: Long? = null,
    val companyName: String = "",
    val billNumber: String = "",
    val customerName: String,
    val customerPhone: String = "",
    val customerAddress: String = "",
    val albumType: String,
    val pages: Int,
    val ratePerPage: Double,
    val extraCharges: Double = 0.0,
    val discount: Double = 0.0,
    val totalBill: Double,
    val costExpense: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000),
    val notes: String = ""
)
