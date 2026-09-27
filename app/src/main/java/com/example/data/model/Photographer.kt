package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photographers")
data class Photographer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val studioName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
