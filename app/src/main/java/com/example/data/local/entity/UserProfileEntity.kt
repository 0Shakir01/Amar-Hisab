package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "local_user",
    val email: String = "user@amarhisab.app",
    val displayName: String = "আমার হিসাব ব্যবহারকারী",
    val photoUrl: String = "",
    val preferredLanguage: String = "bn",
    val currency: String = "BDT",
    val isFirebaseLinked: Boolean = false,
    val accountsStartDateMillis: Long? = null
)
