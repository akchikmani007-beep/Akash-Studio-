package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ownerName: String = "",
    val address: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val logoTag: String = "camera", // icon identifier (camera, aperture, store, film, brush, star)
    val otherDetails: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
