package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BillOrder
import com.example.data.model.BillWithPayments
import com.example.data.model.PaymentRecord
import com.example.data.model.Photographer
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {

    // Photographer operations
    @Query("SELECT * FROM photographers ORDER BY name ASC")
    fun getAllPhotographers(): Flow<List<Photographer>>

    @Query("SELECT * FROM photographers WHERE id = :id")
    fun getPhotographerById(id: Long): Flow<Photographer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotographer(photographer: Photographer): Long

    @Update
    suspend fun updatePhotographer(photographer: Photographer)

    @Delete
    suspend fun deletePhotographer(photographer: Photographer)

    // Bill operations
    @Transaction
    @Query("SELECT * FROM bill_orders ORDER BY createdDate DESC")
    fun getAllBillsWithPayments(): Flow<List<BillWithPayments>>

    @Transaction
    @Query("SELECT * FROM bill_orders WHERE id = :billId")
    fun getBillWithPaymentsById(billId: Long): Flow<BillWithPayments?>

    @Transaction
    @Query("SELECT * FROM bill_orders WHERE photographerId = :photographerId ORDER BY createdDate DESC")
    fun getBillsForPhotographer(photographerId: Long): Flow<List<BillWithPayments>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillOrder): Long

    @Update
    suspend fun updateBill(bill: BillOrder)

    @Delete
    suspend fun deleteBill(bill: BillOrder)

    // Payment operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Update
    suspend fun updatePayment(payment: PaymentRecord)

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)

    @Query("DELETE FROM payment_records WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: Long)

    @Query("SELECT * FROM payment_records WHERE billId = :billId ORDER BY paymentDate DESC")
    fun getPaymentsForBill(billId: Long): Flow<List<PaymentRecord>>
}
