package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BillOrder
import com.example.data.model.Company
import com.example.data.model.PaymentRecord
import com.example.data.model.Photographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Company::class,
        Photographer::class,
        BillOrder::class,
        PaymentRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `companies` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `ownerName` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `mobileNumber` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `gstNumber` TEXT NOT NULL,
                        `logoTag` TEXT NOT NULL,
                        `otherDetails` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                try {
                    db.execSQL("ALTER TABLE `bill_orders` ADD COLUMN `companyId` INTEGER")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `bill_orders` ADD COLUMN `companyName` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `bill_orders` ADD COLUMN `customerPhone` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `bill_orders` ADD COLUMN `customerAddress` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `bill_orders` ADD COLUMN `billNumber` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akash_studio_billing.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val dao = database.studioDao()
                                    // Seed initial sample companies
                                    val company1Id = dao.insertCompany(
                                        Company(
                                            name = "Akash Photo Studio",
                                            ownerName = "Akash",
                                            address = "Main Road, Market Area, Pune",
                                            mobileNumber = "84 46 46 0312",
                                            email = "info@akashphotostudio.com",
                                            gstNumber = "27AAAAA0000A1Z5",
                                            logoTag = "camera",
                                            otherDetails = "Specialist in Wedding Albums & Gift Frames"
                                        )
                                    )
                                    dao.insertCompany(
                                        Company(
                                            name = "ABC Digital Studio",
                                            ownerName = "Amit Sharma",
                                            address = "Station Road, City Plaza, Shop #12",
                                            mobileNumber = "9876543210",
                                            email = "abc@digitalstudio.com",
                                            gstNumber = "",
                                            logoTag = "aperture",
                                            otherDetails = "Commercial & Studio Portfolios"
                                        )
                                    )
                                    dao.insertCompany(
                                        Company(
                                            name = "XYZ Wedding Photography",
                                            ownerName = "Rahul Verma",
                                            address = "MG Road, Creative Hub",
                                            mobileNumber = "9123456780",
                                            email = "xyz@weddingphoto.com",
                                            gstNumber = "",
                                            logoTag = "film",
                                            otherDetails = "Cinematic Wedding & Pre-wedding Shoots"
                                        )
                                    )

                                    // Seed photographer
                                    val photographerId = dao.insertPhotographer(
                                        Photographer(
                                            name = "ABC Photography",
                                            phoneNumber = "8446460312",
                                            studioName = "ABC Digital Studio",
                                            notes = "Premium client"
                                        )
                                    )

                                    // Seed bill linked to company 1
                                    val billId = dao.insertBill(
                                        BillOrder(
                                            photographerId = photographerId,
                                            photographerName = "ABC Photography",
                                            companyId = company1Id,
                                            companyName = "Akash Photo Studio",
                                            billNumber = "INV-1001",
                                            customerName = "Rahul & Pooja",
                                            customerPhone = "9822012345",
                                            customerAddress = "Station Road, Pune",
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

                                    // Seed advance payment
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
