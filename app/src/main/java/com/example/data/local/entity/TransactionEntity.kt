package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("userId"),
        Index("dateMillis"),
        Index("accountId"),
        Index("type"),
        Index("area"),
        Index("category"),
        Index("isDeleted")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val type: String, // INCOME, EXPENSE, TRANSFER, OWNER_CAPITAL, OWNER_WITHDRAWAL, STOCK_PURCHASE, STOCK_SALE, ASSET_PURCHASE, ASSET_INSTALLMENT
    val amountPaisa: Long,
    val accountId: Long,
    val destinationAccountId: Long? = null, // for TRANSFER
    val dateMillis: Long,
    val area: String = "PERSONAL", // PERSONAL or BUSINESS
    val category: String = "", // Legacy category or details
    val sourceDetails: String = "", // Free text description/source of income or expense
    val paidTo: String = "", // Paid to or place for expenses
    val note: String = "",
    // Legacy fields preserved for backward compatibility & safe migration
    val categoryId: Long? = null,
    val tags: String = "",
    val isRecurring: Boolean = false,
    val section: String = "PERSONAL",
    val subCategory: String = "",
    val royalTargetAchieved: Boolean? = null,
    val royalClientName: String = "",
    val royalProjectName: String = "",
    val royalPaymentStatus: String = "RECEIVED",
    val royalExpectedDateMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
)
