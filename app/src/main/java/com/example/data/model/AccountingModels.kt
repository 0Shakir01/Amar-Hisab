package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class FinanceArea(val key: String, val titleBn: String, val titleEn: String) {
    ALL("ALL", "সকল (উভয়)", "All"),
    PERSONAL("PERSONAL", "ব্যক্তিগত", "Personal"),
    BUSINESS("BUSINESS", "ব্যবসা", "Business");

    companion object {
        fun fromKey(key: String?): FinanceArea {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: ALL
        }
    }
}

// Quick suggestions for Income (User can type anything freely)
object IncomeSuggestions {
    val common = listOf(
        "বেতন (Salary)",
        "শাড়ি বিক্রি (Saree sale)",
        "রয়্যাল কোম্পানি কমিশন (Royal Company commission)",
        "হোটেল কমিশন (Hotel commission)",
        "প্লট কমিশন (Plot commission)",
        "ফ্রিল্যান্সিং (Freelance work)",
        "উপহার (Gift)",
        "মুনাফা (Profit)",
        "অন্যান্য আয় (Other income)"
    )
}

// Quick suggestions for Expense (User can type anything freely)
object ExpenseSuggestions {
    val common = listOf(
        "বাড়ি ভাড়া (House rent)",
        "বাজার ও মুদি খরচ (Grocery)",
        "শাড়ির কাপড় ক্রয় (Saree fabric purchase)",
        "কারখানা খরচ (Factory expense)",
        "ফেসবুক বিজ্ঞাপন (Facebook advertising)",
        "যাতায়াত ও পরিবহন (Transport)",
        "চিকিৎসা ও ওষুধ (Medical cost)",
        "পারিবারিক খরচ (Family cost)",
        "বিদ্যুৎ ও ইউটিলিটি (Utility bill)",
        "অন্যান্য খরচ (Other expense)"
    )
}

// Quick suggestions for Asset Types
object AssetTypeSuggestions {
    val common = listOf(
        "মোবাইল ফোন (Mobile phone)",
        "ল্যাপটপ ও কম্পিউটার (Laptop)",
        "সেলাই মেশিন (Sewing machine)",
        "কারখানার সরঞ্জাম (Factory equipment)",
        "আসবাবপত্র (Furniture)",
        "প্লট ও জমি (Plot)",
        "হোটেল শেয়ার (Hotel share)",
        "জামানত / ডিপোজিট (Security deposit)",
        "ব্যাংক ডিপোজিট (Bank deposit)",
        "অন্যান্য বিনিয়োগ ও সম্পদ (Other asset)"
    )
}

enum class DebtType(val key: String, val titleBn: String, val titleEn: String) {
    I_OWE_OTHERS("I_OWE_OTHERS", "আমি দেনাদার (অন্যকে দিতে হবে)", "I Owe Others"),
    OTHERS_OWE_ME("OTHERS_OWE_ME", "অন্যরা আমার কাছে দেনাদার (পাওনা)", "Others Owe Me");

    companion object {
        fun fromKey(key: String): DebtType {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: OTHERS_OWE_ME
        }
    }
}

enum class DebtStatus(val key: String, val titleBn: String, val titleEn: String) {
    PENDING("PENDING", "বাকি", "Pending"),
    PARTIALLY_PAID("PARTIALLY_PAID", "আংশিক পরিশোধ", "Partially Paid"),
    FULLY_SETTLED("FULLY_SETTLED", "সম্পূর্ণ পরিশোধ", "Fully Settled");

    companion object {
        fun fromKey(key: String): DebtStatus {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class InvestmentType(val key: String, val titleBn: String, val titleEn: String) {
    PLOT_INSTALLMENT("PLOT_INSTALLMENT", "প্লট কিস্তি", "Plot Installment"),
    HOTEL_SHARE_PURCHASE("HOTEL_SHARE_PURCHASE", "হোটেল শেয়ার ক্রয়", "Hotel Share Purchase"),
    OTHER_INVESTMENT("OTHER_INVESTMENT", "অন্যান্য বিনিয়োগ", "Other Investment");

    companion object {
        fun fromKey(key: String): InvestmentType {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: OTHER_INVESTMENT
        }
    }
}

// Retained for backward-compatibility with existing legacy records
enum class IncomeCategory(val key: String, val titleBn: String, val titleEn: String) {
    JOB("Job", "চাকরি", "Job"),
    BUSINESS("Business", "ব্যবসা", "Business"),
    FREELANCING("Freelancing", "ফ্রিল্যান্সিং", "Freelancing"),
    OTHERS("Others", "অন্যান্য", "Others");

    companion object {
        fun fromKey(key: String): IncomeCategory {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: OTHERS
        }
    }
}

enum class ExpenseCategory(val key: String, val titleBn: String, val titleEn: String, val color: Color) {
    PERSONAL("Personal", "ব্যক্তিগত", "Personal", Color(0xFF3B82F6)),
    BUSINESS("Business", "ব্যবসা", "Business", Color(0xFF10B981)),
    OTHERS("Others", "অন্যান্য", "Others", Color(0xFF64748B)),
    FAMILY("Family", "পরিবার", "Family", Color(0xFF8B5CF6)),
    HEALTH("Health", "স্বাস্থ্য", "Health", Color(0xFFEF4444)),
    ENTERTAINMENT("Entertainment", "বিনোদন", "Entertainment", Color(0xFFF59E0B));

    companion object {
        fun fromKey(key: String): ExpenseCategory {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: OTHERS
        }
    }
}

data class DashboardMetrics(
    val availableBalancePaisa: Long = 0L,
    val cashBalancePaisa: Long = 0L,
    val bankBalancePaisa: Long = 0L,
    val personalCashPaisa: Long = 0L,
    val personalCash1Paisa: Long = 0L,
    val personalCash2Paisa: Long = 0L,
    val personalCash1Name: String = "ক্যাশ ১ (Cash 1)",
    val personalCash2Name: String = "ক্যাশ ২ (Cash 2)",
    val personalBankPaisa: Long = 0L,
    val personalBank1Paisa: Long = 0L,
    val personalBank2Paisa: Long = 0L,
    val personalBank1Name: String = "ব্যাংক ১ (Bank 1)",
    val personalBank2Name: String = "ব্যাংক ২ (Bank 2)",
    val businessCashPaisa: Long = 0L,
    val businessBankPaisa: Long = 0L,
    val personalBalancePaisa: Long = 0L,
    val businessBalancePaisa: Long = 0L,
    val thisMonthIncomePaisa: Long = 0L,
    val thisMonthExpensePaisa: Long = 0L,
    val monthlySurplusPaisa: Long = 0L,
    val othersOweMePaisa: Long = 0L,       // Total Receivable
    val iOweOthersPaisa: Long = 0L,        // Total Payable
    val businessStockValuePaisa: Long = 0L,
    val fixedAssetsValuePaisa: Long = 0L,
    val investmentsValuePaisa: Long = 0L,
    val upcomingInstallmentsPaisa: Long = 0L,
    val overdueDebtsPaisa: Long = 0L,
    val totalAssetsPaisa: Long = 0L,
    val totalLiabilitiesPaisa: Long = 0L,
    val netWorthPaisa: Long = 0L,
    // Owner capital & withdrawals for proprietor reporting
    val totalOwnerCapitalPaisa: Long = 0L,
    val totalOwnerWithdrawalPaisa: Long = 0L
)

enum class AppNavTab(val id: String) {
    DASHBOARD("dashboard"),         // Home (হোম)
    LEDGER("ledger"),               // Ledger (লেজার / লেনদেন)
    DEBTS("debts"),                 // Debt/Pawna (দেনা-পাওনা)
    ASSETS_STOCK("assets_stock"),   // Assets/Stock (সম্পদ ও স্টক)
    MORE("more"),                   // More (আরও)
    // Sub-screens under More
    OPENING_BALANCE("opening_balance"),
    ACCOUNTS("accounts"),
    REPORTS("reports"),
    TRASH("trash"),
    SETTINGS("settings")
}
