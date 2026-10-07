package com.example.data.model

enum class AppLanguage(val code: String, val label: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English")
}

object AppStrings {
    fun get(key: String, lang: AppLanguage): String {
        val isBn = lang == AppLanguage.BANGLA
        return when (key) {
            "app_name" -> if (isBn) "আমার হিসাব" else "Amar Hisab"
            "app_subtitle" -> if (isBn) "ব্যক্তিগত আয়-ব্যয় ট্র্যাকার" else "Personal Finance & Expense Tracker"

            // Tabs / Navigation
            "nav_dashboard" -> if (isBn) "ড্যাশবোর্ড" else "Dashboard"
            "nav_transactions" -> if (isBn) "লেনদেন" else "Transactions"
            "nav_accounts" -> if (isBn) "অ্যাকাউন্ট" else "Accounts"
            "nav_budgets" -> if (isBn) "বাজেট" else "Budgets"
            "nav_reports" -> if (isBn) "রিপোর্ট" else "Reports"
            "nav_categories" -> if (isBn) "ক্যাটাগরি" else "Categories"
            "nav_backup" -> if (isBn) "ডেটা ও ব্যাকআপ" else "Backup & Data"

            // Dashboard
            "total_income" -> if (isBn) "মোট আয়" else "Total Income"
            "total_expense" -> if (isBn) "মোট ব্যয়" else "Total Expense"
            "net_balance" -> if (isBn) "বর্তমান ব্যালেন্স" else "Current Balance"
            "savings" -> if (isBn) "মাসিক সঞ্চয়" else "Monthly Savings"
            "savings_rate" -> if (isBn) "সঞ্চয়ের হার" else "Savings Rate"
            "transaction_count" -> if (isBn) "লেনদেনের সংখ্যা" else "Transactions"
            "add_income" -> if (isBn) "আয় যোগ করুন" else "Add Income"
            "add_expense" -> if (isBn) "ব্যয় যোগ করুন" else "Add Expense"
            "quick_transfer" -> if (isBn) "স্থানান্তর" else "Transfer"
            "recent_transactions" -> if (isBn) "সাম্প্রতিক লেনদেন" else "Recent Transactions"
            "view_all" -> if (isBn) "সব দেখুন" else "View All"
            "no_recent_transactions" -> if (isBn) "এই মাসে এখনও কোন লেনদেন নেই" else "No transactions for this month yet"
            "expense_breakdown" -> if (isBn) "ক্যাটাগরিভিত্তিক ব্যয়" else "Expense Breakdown"
            "income_vs_expense" -> if (isBn) "আয় বনাম ব্যয় তুলনা" else "Income vs Expense"

            // Transaction Types
            "type_income" -> if (isBn) "আয়" else "Income"
            "type_expense" -> if (isBn) "ব্যয়" else "Expense"
            "type_transfer" -> if (isBn) "স্থানান্তর" else "Transfer"

            // Transaction Form
            "new_transaction" -> if (isBn) "নতুন লেনদেন" else "New Transaction"
            "edit_transaction" -> if (isBn) "লেনদেন সম্পাদন" else "Edit Transaction"
            "amount" -> if (isBn) "টাকার পরিমাণ" else "Amount"
            "amount_hint" -> if (isBn) "০.০০" else "0.00"
            "select_category" -> if (isBn) "ক্যাটাগরি নির্বাচন করুন" else "Select Category"
            "select_account" -> if (isBn) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Account"
            "from_account" -> if (isBn) "প্রেরক অ্যাকাউন্ট (উৎস)" else "Source Account"
            "to_account" -> if (isBn) "প্রাপক অ্যাকাউন্ট (গন্তব্য)" else "Destination Account"
            "date" -> if (isBn) "তারিখ" else "Date"
            "note" -> if (isBn) "মন্তব্য / বিবরণ (ঐচ্ছিক)" else "Note (Optional)"
            "tags" -> if (isBn) "ট্যাগ (যেমন: বাজার, উপহার)" else "Tags (e.g. groceries, gift)"
            "recurring" -> if (isBn) "পুনরাবৃত্তিমূলক লেনদেন (মাসিক)" else "Recurring Transaction"
            "save" -> if (isBn) "সংরক্ষণ করুন" else "Save"
            "cancel" -> if (isBn) "বাতিল" else "Cancel"
            "delete" -> if (isBn) "মুছে ফেলুন" else "Delete"
            "duplicate" -> if (isBn) "অনুরূপ তৈরি করুন" else "Duplicate"

            // Validation & Errors
            "error_amount_zero" -> if (isBn) "টাকার পরিমাণ শূন্যের চেয়ে বেশি হতে হবে" else "Amount must be greater than zero"
            "error_category_required" -> if (isBn) "ক্যাটাগরি নির্বাচন করা আবশ্যক" else "Category is required"
            "error_account_required" -> if (isBn) "অ্যাকাউন্ট নির্বাচন করা আবশ্যক" else "Account is required"
            "error_same_account_transfer" -> if (isBn) "উৎস ও গন্তব্য অ্যাকাউন্ট ভিন্ন হতে হবে" else "Source and destination accounts must be different"
            "success_transaction_saved" -> if (isBn) "লেনদেন সফলভাবে সংরক্ষিত হয়েছে!" else "Transaction saved successfully!"
            "success_transaction_deleted" -> if (isBn) "লেনদেন মুছে ফেলা হয়েছে" else "Transaction deleted"
            "confirm_delete_title" -> if (isBn) "মুছে ফেলার নিশ্চয়তা" else "Confirm Deletion"
            "confirm_delete_message" -> if (isBn) "আপনি কি নিশ্চিত যে এই লেনদেনটি মুছে ফেলতে চান?" else "Are you sure you want to delete this transaction?"

            // History & Filters
            "search_placeholder" -> if (isBn) "মন্তব্য, ক্যাটাগরি বা অ্যাকাউন্ট খুঁজুন..." else "Search note, category or account..."
            "filter_all" -> if (isBn) "সব" else "All"
            "sort_newest" -> if (isBn) "নতুনতম আগে" else "Newest First"
            "sort_oldest" -> if (isBn) "পুরাতনতম আগে" else "Oldest First"
            "sort_highest" -> if (isBn) "সর্বোচ্চ পরিমাণ" else "Highest Amount"
            "sort_lowest" -> if (isBn) "সর্বনিম্ন পরিমাণ" else "Lowest Amount"
            "no_transactions_found" -> if (isBn) "কোন লেনদেন খুঁজে পাওয়া যায়নি" else "No transactions match your search"

            // Accounts
            "accounts_title" -> if (isBn) "অ্যাকাউন্টসমূহ" else "Accounts"
            "add_account" -> if (isBn) "নতুন অ্যাকাউন্ট যোগ করুন" else "Add New Account"
            "edit_account" -> if (isBn) "অ্যাকাউন্ট পরিবর্তন করুন" else "Edit Account"
            "account_name" -> if (isBn) "অ্যাকাউন্টের নাম" else "Account Name"
            "account_type" -> if (isBn) "অ্যাকাউন্টের ধরন" else "Account Type"
            "opening_balance" -> if (isBn) "প্রারম্ভিক ব্যালেন্স (৳)" else "Opening Balance (৳)"
            "calculated_balance" -> if (isBn) "বর্তমান মোট ব্যালেন্স" else "Current Balance"
            "is_active" -> if (isBn) "অ্যাকাউন্ট সক্রিয় আছে" else "Account is Active"
            "account_cash" -> if (isBn) "নগদ টাকা (Cash)" else "Cash"
            "account_bank" -> if (isBn) "ব্যাংক হিসাব (Bank)" else "Bank Account"
            "account_bkash" -> if (isBn) "বিকাশ (bKash)" else "bKash"
            "account_nagad" -> if (isBn) "নগদ (Nagad)" else "Nagad"
            "account_credit_card" -> if (isBn) "ক্রেডিট কার্ড (Credit Card)" else "Credit Card"
            "account_other" -> if (isBn) "অন্যান্য অ্যাকাউন্ট" else "Other"

            // Categories
            "categories_title" -> if (isBn) "ক্যাটাগরি ব্যবস্থাপনা" else "Category Management"
            "add_category" -> if (isBn) "নতুন ক্যাটাগরি যোগ করুন" else "Add New Category"
            "category_name_bn" -> if (isBn) "নাম (বাংলা)" else "Name (Bangla)"
            "category_name_en" -> if (isBn) "নাম (English)" else "Name (English)"
            "category_type" -> if (isBn) "ক্যাটাগরির ধরন" else "Category Type"
            "expense_categories" -> if (isBn) "ব্যয়ের ক্যাটাগরি" else "Expense Categories"
            "income_categories" -> if (isBn) "আয়ের ক্যাটাগরি" else "Income Categories"

            // Budgets
            "budgets_title" -> if (isBn) "মাসিক বাজেট" else "Monthly Budgets"
            "set_monthly_budget" -> if (isBn) "মাসিক মোট বাজেট নির্ধারণ" else "Set Overall Monthly Budget"
            "set_category_budget" -> if (isBn) "ক্যাটাগরি বাজেট যোগ করুন" else "Set Category Budget"
            "used" -> if (isBn) "খরচ হয়েছে" else "Used"
            "remaining" -> if (isBn) "অবশিষ্ট আছে" else "Remaining"
            "overspent" -> if (isBn) "অতিরিক্ত খরচ" else "Overspent"
            "warning_80" -> if (isBn) "সতর্কতা: বাজেটের ৮০% এর বেশি শেষ!" else "Warning: Over 80% of budget used!"
            "warning_100" -> if (isBn) "বিপজ্জনক: সম্পূর্ণ বাজেট অতিক্রম করেছে!" else "Alert: 100% Budget exceeded!"
            "budget_limit" -> if (isBn) "বাজেট সীমা (৳)" else "Budget Limit (৳)"
            "no_budget_set" -> if (isBn) "এই মাসের জন্য কোন বাজেট নির্ধারণ করা হয়নি" else "No budgets set for this month yet"

            // Reports
            "reports_title" -> if (isBn) "আর্থিক প্রতিবেদন ও বিশ্লেষণ" else "Financial Reports & Analytics"
            "top_expenses" -> if (isBn) "সর্বোচ্চ খরচের খাত" else "Top Spending Categories"
            "daily_trend" -> if (isBn) "দৈনিক ব্যয়ের ধারা" else "Daily Spending Trend"
            "balance_overview" -> if (isBn) "অ্যাকাউন্ট ব্যালেন্স পরিসংখ্যান" else "Account Balances Overview"
            "export_csv" -> if (isBn) "সিএসভি ফাইল এক্সপোর্ট" else "Export CSV Report"

            // Backup & Data Management
            "backup_title" -> if (isBn) "ডেটা ব্যাকআপ ও ব্যবস্থাপনা" else "Backup & Data Management"
            "export_all_csv" -> if (isBn) "সম্পূর্ণ ডেটা CSV এক্সপোর্ট" else "Export All Data (CSV)"
            "export_all_json" -> if (isBn) "সম্পূর্ণ ডেটা JSON ব্যাকআপ" else "Export All Data (JSON)"
            "import_csv" -> if (isBn) "CSV থেকে লেনদেন ইমপোর্ট" else "Import Transactions from CSV"
            "demo_data_button" -> if (isBn) "ঐচ্ছিক ডেমো ডেটা যোগ করুন" else "Load Realistic Demo Data"
            "demo_data_desc" -> if (isBn) "পরীক্ষার জন্য বাস্তবসম্মত বাংলাদেশী আয়-ব্যয়ের লেনদেন যুক্ত করুন" else "Populate realistic sample Bangladeshi transactions for testing"
            "delete_all_data" -> if (isBn) "সমস্ত ডেটা মুছে ফেলুন" else "Delete All Local Data"
            "delete_all_warning" -> if (isBn) "সাবধান! এই ক্রিয়াটি অপরিবর্তনীয়। সমস্ত অ্যাকাউন্ট, লেনদেন ও বাজেট স্থায়ীভাবে মুছে যাবে।" else "Warning: This action is irreversible. All local transactions, accounts, and budgets will be permanently deleted."
            "confirm_clear_all" -> if (isBn) "হ্যাঁ, সমস্ত ডেটা মুছুন" else "Yes, Delete Everything"
            "firebase_guide_title" -> if (isBn) "ফায়ারবেস ও ফায়ারস্টোর ক্লাউড সেটআপ" else "Firebase & Cloud Firestore Setup"
            "firestore_rules_title" -> if (isBn) "ফায়ারস্টোর সিকিউরিটি রুলস (Security Rules)" else "Firestore Security Rules"

            // Auth
            "auth_status" -> if (isBn) "ব্যবহারকারী প্রোফাইল ও ক্লাউড সিঙ্ক" else "User Profile & Cloud Sync"
            "local_vault" -> if (isBn) "অফলাইন সিকিউর ভল্ট (রুম ডেটাবেস)" else "Offline Secure Vault (Room DB)"
            "google_signin" -> if (isBn) "গুগল দিয়ে সাইন-ইন করুন" else "Sign in with Google"
            "signed_in_as" -> if (isBn) "লগইন করা আছেন:" else "Signed in as:"
            "logout" -> if (isBn) "লগআউট" else "Logout"

            else -> key
        }
    }
}
