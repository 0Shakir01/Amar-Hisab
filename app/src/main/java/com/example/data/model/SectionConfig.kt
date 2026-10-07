package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AppSection(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val color: Color,
    val colorHex: String
) {
    PERSONAL(
        id = "PERSONAL",
        titleBn = "ব্যক্তিগত ও চাকরি",
        titleEn = "Personal & Job",
        color = Color(0xFF2563EB), // Blue
        colorHex = "#2563EB"
    ),
    SAREE(
        id = "SAREE",
        titleBn = "শাড়ি ব্যবসা",
        titleEn = "Saree Business",
        color = Color(0xFF059669), // Green/Gold
        colorHex = "#059669"
    ),
    ROYAL(
        id = "ROYAL",
        titleBn = "রয়্যাল কোম্পানি",
        titleEn = "Royal Company",
        color = Color(0xFF7C3AED), // Purple
        colorHex = "#7C3AED"
    ),
    DHAR_DEWA(
        id = "DHAR_DEWA",
        titleBn = "ধার দেওয়া",
        titleEn = "Dhar Dewa",
        color = Color(0xFFEA580C), // Orange
        colorHex = "#EA580C"
    );

    companion object {
        fun fromId(id: String?): AppSection {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PERSONAL
        }
    }
}

object SectionOptions {

    // PERSONAL & JOB
    val PERSONAL_INCOME = listOf(
        OptionItem("Job Salary", "চাকরির বেতন", "Job Salary"),
        OptionItem("Other Income", "অন্যান্য আয়", "Other Income")
    )

    val PERSONAL_EXPENSE = listOf(
        OptionItem("Daily Expense", "দৈনন্দিন খরচ", "Daily Expense"),
        OptionItem("Home and Family", "বাড়ি ও পরিবার", "Home and Family"),
        OptionItem("Health", "স্বাস্থ্য ও চিকিৎসা", "Health"),
        OptionItem("Education", "শিক্ষা", "Education"),
        OptionItem("Travel", "যাতায়াত ও ভ্রমণ", "Travel"),
        OptionItem("Other", "অন্যান্য খরচ", "Other")
    )

    // SAREE BUSINESS
    val SAREE_INCOME = listOf(
        OptionItem("Website Sales", "ওয়েবসাইট বিক্রয়", "Website Sales"),
        OptionItem("Facebook Page Sales", "ফেসবুক পেজ বিক্রয়", "Facebook Page Sales"),
        OptionItem("Direct Sales", "সরাসরি বিক্রয়", "Direct Sales"),
        OptionItem("Other Saree Income", "অন্যান্য শাড়ি আয়", "Other Saree Income")
    )

    val SAREE_EXPENSE = listOf(
        OptionItem("Factory and Production", "কারখানা ও উৎপাদন", "Factory and Production"),
        OptionItem("Raw Materials", "কাঁচামাল", "Raw Materials"),
        OptionItem("Worker Salary", "শ্রমিকদের বেতন", "Worker Salary"),
        OptionItem("Packaging", "প্যাকেজিং", "Packaging"),
        OptionItem("Delivery", "ডেলিভারি", "Delivery"),
        OptionItem("Facebook Marketing", "ফেসবুক বিজ্ঞাপন", "Facebook Marketing"),
        OptionItem("Website and Hosting", "ওয়েবসাইট ও হোস্টিং", "Website and Hosting"),
        OptionItem("Other Business Expense", "অন্যান্য ব্যবসায়িক খরচ", "Other Business Expense")
    )

    // ROYAL COMPANY (Income only)
    val ROYAL_INCOME = listOf(
        OptionItem("Fixed Salary", "নির্দিষ্ট বেতন", "Fixed Salary"),
        OptionItem("Target-Based Salary", "টার্গেট ভিত্তিক বেতন", "Target-Based Salary"),
        OptionItem("Hotel Share Commission", "হোটেল শেয়ার কমিশন", "Hotel Share Commission"),
        OptionItem("Plot Commission", "প্লট কমিশন", "Plot Commission"),
        OptionItem("Other Royal Income", "অন্যান্য রয়্যাল আয়", "Other Royal Income")
    )

    fun getLabel(key: String, isBangla: Boolean): String {
        val all = PERSONAL_INCOME + PERSONAL_EXPENSE + SAREE_INCOME + SAREE_EXPENSE + ROYAL_INCOME
        val match = all.find { it.id.equals(key, ignoreCase = true) }
        return if (match != null) {
            if (isBangla) match.nameBn else match.nameEn
        } else {
            key
        }
    }
}

data class OptionItem(
    val id: String,
    val nameBn: String,
    val nameEn: String
)

enum class DharDewaStatus(val id: String, val titleBn: String, val titleEn: String) {
    PENDING("PENDING", "বকেয়া", "Pending"),
    PARTIALLY_PAID("PARTIALLY_PAID", "আংশিক পরিশোধ", "Partially Paid"),
    FULLY_PAID("FULLY_PAID", "সম্পূর্ণ পরিশোধ", "Fully Paid");

    companion object {
        fun fromId(id: String?): DharDewaStatus {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PENDING
        }
    }
}
