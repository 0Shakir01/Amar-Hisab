package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    indices = [
        Index("userId"),
        Index("investmentType"),
        Index("isDeleted")
    ]
)
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val projectName: String,
    val investmentType: String, // "PLOT_INSTALLMENT", "HOTEL_SHARE_PURCHASE", "OTHER_INVESTMENT"
    val totalAgreedPaisa: Long = 0L,
    val paidAmountPaisa: Long = 0L,
    val installmentPaisa: Long = 0L,
    val nextPaymentDateMillis: Long? = null,
    val accountId: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
) {
    @get:Ignore
    val remainingPaisa: Long
        get() = (totalAgreedPaisa - paidAmountPaisa).coerceAtLeast(0L)
}
