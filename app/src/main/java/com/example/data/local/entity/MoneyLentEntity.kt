package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "money_lent",
    indices = [
        Index("userId"),
        Index("status"),
        Index("dateGivenMillis")
    ]
)
data class MoneyLentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val borrowerName: String,
    val amountGivenPaisa: Long,
    val dateGivenMillis: Long,
    val dueDateMillis: Long? = null,
    val amountReturnedPaisa: Long = 0L,
    val status: String = "PENDING", // PENDING, PARTIALLY_PAID, FULLY_PAID
    val accountId: Long, // Account from which money was given
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingPaisa: Long
        get() = (amountGivenPaisa - amountReturnedPaisa).coerceAtLeast(0L)
}
