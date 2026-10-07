package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index("userId"),
        Index("area"),
        Index("isDeleted")
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val name: String,
    val type: String, // CASH, BANK, OTHER
    val area: String = "PERSONAL", // PERSONAL, BUSINESS
    val openingBalancePaisa: Long = 0L,
    val isActive: Boolean = true,
    val colorHex: String = "#059669",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
)
