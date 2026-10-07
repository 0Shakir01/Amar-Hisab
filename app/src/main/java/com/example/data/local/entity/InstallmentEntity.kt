package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "installments",
    indices = [
        Index("userId"),
        Index("assetId"),
        Index("isDeleted")
    ]
)
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val assetId: Long,
    val amountPaisa: Long,
    val accountId: Long,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
)
