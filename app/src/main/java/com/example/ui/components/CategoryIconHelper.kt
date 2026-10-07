package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.AccountType
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBkash
import com.example.ui.theme.ColorCash
import com.example.ui.theme.ColorCreditCard
import com.example.ui.theme.ColorNagad
import com.example.ui.theme.ColorOtherAccount

object CategoryIconHelper {
    fun getCategoryIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "directions_bus", "transport" -> Icons.Default.DirectionsBus
            "home", "rent" -> Icons.Default.Home
            "receipt_long", "bills" -> Icons.AutoMirrored.Filled.ReceiptLong
            "school", "education" -> Icons.Default.School
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "medical_services", "health" -> Icons.Default.MedicalServices
            "diversity_1", "family" -> Icons.Default.Diversity1
            "movie", "entertainment" -> Icons.Default.Movie
            "flight", "travel" -> Icons.Default.Flight
            "account_balance_wallet", "salary" -> Icons.Default.AccountBalanceWallet
            "laptop_mac", "freelance" -> Icons.Default.LaptopMac
            "store", "business" -> Icons.Default.Store
            "featured_seasonal_and_gifts", "gift" -> Icons.Default.Redo
            "replay", "refund" -> Icons.Default.Redo
            "attach_money", "money" -> Icons.Default.AttachMoney
            else -> Icons.Default.Category
        }
    }

    fun getAccountIcon(type: String): ImageVector {
        return when (type) {
            AccountType.CASH.name -> Icons.Default.LocalAtm
            AccountType.BANK.name -> Icons.Default.AccountBalance
            AccountType.BKASH.name, AccountType.NAGAD.name -> Icons.Default.PhoneAndroid
            AccountType.CREDIT_CARD.name -> Icons.Default.CreditCard
            else -> Icons.Default.AccountBalanceWallet
        }
    }

    fun getAccountColor(type: String): Color {
        return when (type) {
            AccountType.CASH.name -> ColorCash
            AccountType.BANK.name -> ColorBank
            AccountType.BKASH.name -> ColorBkash
            AccountType.NAGAD.name -> ColorNagad
            AccountType.CREDIT_CARD.name -> ColorCreditCard
            else -> ColorOtherAccount
        }
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF10B981)): Color {
        return try {
            val clean = hex.removePrefix("#")
            val fullHex = if (clean.length == 6) "FF$clean" else clean
            Color(fullHex.toLong(16))
        } catch (e: Exception) {
            defaultColor
        }
    }
}
