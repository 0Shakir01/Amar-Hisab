package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    indices = [
        Index("userId"),
        Index("debtType"),
        Index("area"),
        Index("status"),
        Index("dateMillis"),
        Index("isOpening"),
        Index("isDeleted")
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val personName: String,
    val phone: String = "",
    val debtType: String, // "I_OWE_OTHERS" or "OTHERS_OWE_ME"
    val area: String = "PERSONAL", // "PERSONAL" or "BUSINESS"
    val amountPaisa: Long,
    val dateMillis: Long,
    val dueDateMillis: Long? = null,
    val accountId: Long,
    val settledAmountPaisa: Long = 0L,
    val status: String = "PENDING", // "PENDING", "PARTIALLY_PAID", "FULLY_SETTLED"
    val note: String = "",
    val isOpening: Boolean = false,
    val originalDateMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
) {
    @get:Ignore
    val remainingAmountPaisa: Long
        get() = (amountPaisa - settledAmountPaisa).coerceAtLeast(0L)
}
