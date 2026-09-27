package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BillOrder
import com.example.data.model.PaymentRecord
import com.example.data.model.Photographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Photographer::class,
        BillOrder::class,
        PaymentRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akash_studio_billing.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with initial test scenario
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val dao = database.studioDao()
                                    val photographerId = dao.insertPhotographer(
                                        Photographer(
                                            name = "ABC Photography",
                                            phoneNumber = "8446460312",
                                            studioName = "ABC Digital Studio",
                                            notes = "Premium client"
                                        )
                                    )
                                    val billId = dao.insertBill(
                                        BillOrder(
                                            photographerId = photographerId,
                                            photographerName = "ABC Photography",
                                            customerName = "Rahul & Pooja",
                                            albumType = "Wedding Album",
                                            pages = 25,
                                            ratePerPage = 100.0,
                                            extraCharges = 0.0,
                                            discount = 0.0,
                                            totalBill = 2500.0,
                                            costExpense = 1100.0,
                                            notes = "25 pages silk matte finish"
                                        )
                                    )
                                    dao.insertPayment(
                                        PaymentRecord(
                                            billId = billId,
                                            photographerId = photographerId,
                                            amount = 500.0,
                                            paymentMethod = "UPI",
                                            paymentType = "Advance",
                                            notes = "Initial booking advance"
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
