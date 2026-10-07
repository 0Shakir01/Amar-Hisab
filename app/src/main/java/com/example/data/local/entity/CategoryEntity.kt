package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val nameEn: String,
    val nameBn: String,
    val type: String, // EXPENSE or INCOME
    val iconName: String = "category",
    val colorHex: String = "#10B981",
    val isHidden: Boolean = false,
    val isDefault: Boolean = false
)
