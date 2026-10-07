package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assets",
    indices = [
        Index("userId"),
        Index("area"),
        Index("assetType"),
        Index("status"),
        Index("isOpening"),
        Index("isDeleted")
    ]
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val name: String,
    val assetType: String, // Free text or suggestion (Mobile, Laptop, Sewing Machine, Plot, Hotel Share, Furniture, etc.)
    val area: String = "PERSONAL", // PERSONAL or BUSINESS
    val totalPurchaseValuePaisa: Long = 0L,
    val paidAmountPaisa: Long = 0L,
    val currentValuePaisa: Long = 0L,
    val purchaseDateMillis: Long = System.currentTimeMillis(),
    val accountId: Long = 1L,
    val installmentPaisa: Long = 0L,
    val nextPaymentDateMillis: Long? = null,
    val status: String = "ACTIVE", // ACTIVE, FULLY_PAID, DISPOSED
    val note: String = "",
    val isOpening: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
) {
    @get:Ignore
    val remainingPaisa: Long
        get() = (totalPurchaseValuePaisa - paidAmountPaisa).coerceAtLeast(0L)
}
