package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["userId", "monthKey", "categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val monthKey: String, // "YYYY-MM" e.g. "2026-09"
    val categoryId: Long? = null, // null = overall monthly budget
    val limitPaisa: Long
)
