package com.example.data.repository

import com.example.data.local.StudioDao
import com.example.data.model.BillOrder
import com.example.data.model.BillWithPayments
import com.example.data.model.Company
import com.example.data.model.PaymentRecord
import com.example.data.model.Photographer
import kotlinx.coroutines.flow.Flow

class BillingRepository(private val dao: StudioDao) {

    // Companies
    val allCompanies: Flow<List<Company>> = dao.getAllCompanies()

    fun getCompanyById(id: Long): Flow<Company?> = dao.getCompanyById(id)

    suspend fun getCompanyByIdSync(id: Long): Company? = dao.getCompanyByIdSync(id)

    suspend fun insertCompany(company: Company): Long = dao.insertCompany(company)

    suspend fun updateCompany(company: Company) = dao.updateCompany(company)

    suspend fun deleteCompany(company: Company) = dao.deleteCompany(company)

    fun getBillsForCompany(companyId: Long): Flow<List<BillWithPayments>> =
        dao.getBillsForCompany(companyId)

    // Photographers
    val allPhotographers: Flow<List<Photographer>> = dao.getAllPhotographers()
    val allBills: Flow<List<BillWithPayments>> = dao.getAllBillsWithPayments()

    fun getPhotographerById(id: Long): Flow<Photographer?> = dao.getPhotographerById(id)

    fun getBillById(id: Long): Flow<BillWithPayments?> = dao.getBillWithPaymentsById(id)

    fun getBillsForPhotographer(photographerId: Long): Flow<List<BillWithPayments>> =
        dao.getBillsForPhotographer(photographerId)

    suspend fun insertPhotographer(photographer: Photographer): Long =
        dao.insertPhotographer(photographer)

    suspend fun updatePhotographer(photographer: Photographer) =
        dao.updatePhotographer(photographer)

    suspend fun deletePhotographer(photographer: Photographer) =
        dao.deletePhotographer(photographer)

    suspend fun createBillWithAdvance(
        bill: BillOrder,
        advanceAmount: Double,
        paymentMethod: String,
        paymentDate: Long
    ): Long {
        val billId = dao.insertBill(bill)
        if (advanceAmount > 0.0) {
            dao.insertPayment(
                PaymentRecord(
                    billId = billId,
                    photographerId = bill.photographerId,
                    amount = advanceAmount,
                    paymentDate = paymentDate,
                    paymentMethod = paymentMethod,
                    paymentType = "Advance",
                    notes = "Advance received while creating bill"
                )
            )
        }
        return billId
    }

    suspend fun updateBill(bill: BillOrder) = dao.updateBill(bill)

    suspend fun deleteBill(bill: BillOrder) = dao.deleteBill(bill)

    suspend fun addPayment(payment: PaymentRecord): Long = dao.insertPayment(payment)

    suspend fun updatePayment(payment: PaymentRecord) = dao.updatePayment(payment)

    suspend fun deletePayment(payment: PaymentRecord) = dao.deletePayment(payment)

    suspend fun deletePaymentById(paymentId: Long) = dao.deletePaymentById(paymentId)
}
