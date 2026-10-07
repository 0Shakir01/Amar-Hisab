package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_items",
    indices = [
        Index("userId"),
        Index("area"),
        Index("isOpening"),
        Index("isDeleted")
    ]
)
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val productName: String,
    val area: String = "BUSINESS", // Normally BUSINESS
    val purchaseCostPerUnitPaisa: Long = 0L,
    val purchasedQuantity: Double = 0.0,
    val soldQuantity: Double = 0.0,
    val accountId: Long = 1L, // Account used for purchase
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val isOpening: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
) {
    @get:Ignore
    val remainingQuantity: Double
        get() = (purchasedQuantity - soldQuantity).coerceAtLeast(0.0)

    @get:Ignore
    val totalStockCostPaisa: Long
        get() = (purchasedQuantity * purchaseCostPerUnitPaisa).toLong()

    @get:Ignore
    val currentStockValuePaisa: Long
        get() = (remainingQuantity * purchaseCostPerUnitPaisa).toLong()
}
