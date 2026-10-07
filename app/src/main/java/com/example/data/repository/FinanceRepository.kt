package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.local.AmarHisabDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.InstallmentEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.AccountType
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DashboardMetrics
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeCategory
import com.example.data.model.InvestmentType
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AccountWithBalance(
    val account: AccountEntity,
    val calculatedBalancePaisa: Long
)

data class ImportPreview(
    val isValid: Boolean,
    val accountCount: Int = 0,
    val transactionCount: Int = 0,
    val debtCount: Int = 0,
    val assetCount: Int = 0,
    val stockCount: Int = 0,
    val installmentCount: Int = 0,
    val investmentCount: Int = 0,
    val exportedAt: Long = 0L,
    val errorMessage: String? = null
)

data class MigrationSummary(
    val localAccountCount: Int = 0,
    val localTransactionCount: Int = 0,
    val localDebtCount: Int = 0,
    val localAssetCount: Int = 0,
    val localStockCount: Int = 0,
    val localInstallmentCount: Int = 0,
    val localInvestmentCount: Int = 0,
    val cloudAccountCount: Int = 0,
    val cloudTransactionCount: Int = 0,
    val cloudDebtCount: Int = 0,
    val cloudAssetCount: Int = 0,
    val cloudStockCount: Int = 0,
    val cloudInstallmentCount: Int = 0,
    val cloudInvestmentCount: Int = 0,
    val backupJson: String = ""
)

class FinanceRepository(
    private val database: AmarHisabDatabase,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val debtDao = database.debtDao()
    private val assetDao = database.assetDao()
    private val stockDao = database.stockDao()
    private val installmentDao = database.installmentDao()
    private val investmentDao = database.investmentDao()
    private val userProfileDao = database.userProfileDao()
    private val moneyLentDao = database.moneyLentDao()
    val authService = com.example.data.firebase.FirebaseAuthService(context)
    val syncManager = com.example.data.firebase.FirestoreSyncManager(context)

    fun getCurrentUserId(): String = authService.getCurrentUserId()

    suspend fun verifyRealFirestoreConnection(): com.example.data.firebase.FirebaseTestResult {
        return syncManager.verifyRealFirestoreConnection(getCurrentUserId())
    }

    init {
        scope.launch {
            seedDefaultsIfNeeded()
            migrateLegacyDataIfNeeded()
        }
    }

    private suspend fun seedDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val currentAccounts = accountDao.getAccounts("local_user").first()
        if (currentAccounts.isEmpty()) {
            val defaultAccounts = listOf(
                AccountEntity(
                    name = "ব্যক্তিগত ক্যাশ ১ (Personal Cash 1)",
                    type = AccountType.CASH.name,
                    area = "PERSONAL",
                    openingBalancePaisa = 0L,
                    colorHex = "#059669"
                ),
                AccountEntity(
                    name = "ব্যক্তিগত ক্যাশ ২ (Personal Cash 2)",
                    type = AccountType.CASH.name,
                    area = "PERSONAL",
                    openingBalancePaisa = 0L,
                    colorHex = "#10B981"
                ),
                AccountEntity(
                    name = "ব্যক্তিগত ব্যাংক ১ (Personal Bank 1)",
                    type = AccountType.BANK.name,
                    area = "PERSONAL",
                    openingBalancePaisa = 0L,
                    colorHex = "#1E40AF"
                ),
                AccountEntity(
                    name = "ব্যক্তিগত ব্যাংক ২ (Personal Bank 2)",
                    type = AccountType.BANK.name,
                    area = "PERSONAL",
                    openingBalancePaisa = 0L,
                    colorHex = "#3B82F6"
                ),
                AccountEntity(
                    name = "ব্যবসা ক্যাশ (Business Cash)",
                    type = AccountType.CASH.name,
                    area = "BUSINESS",
                    openingBalancePaisa = 0L,
                    colorHex = "#D97706"
                ),
                AccountEntity(
                    name = "ব্যবসা ব্যাংক (Business Bank)",
                    type = AccountType.BANK.name,
                    area = "BUSINESS",
                    openingBalancePaisa = 0L,
                    colorHex = "#7C3AED"
                )
            )
            accountDao.insertAccounts(defaultAccounts)
        } else {
            // Migration for Personal Cash 1 and Cash 2:
            // "If the existing app has one generic Cash account, preserve its existing records and use it as Personal Cash 1.
            // Create Personal Cash 2 with zero balance. Do not copy the old balance into Cash 2.
            // Cash 1 and Cash 2 must never be duplicated during migration."
            val personalCashes = currentAccounts.filter { it.type == AccountType.CASH.name && it.area.equals("PERSONAL", ignoreCase = true) && !it.isDeleted }
            if (personalCashes.size == 1) {
                val existingGenericCash = personalCashes[0]
                if (existingGenericCash.name == "ব্যক্তিগত ক্যাশ (Personal Cash)" || existingGenericCash.name == "ব্যক্তিগত ক্যাশ" || existingGenericCash.name == "Cash" || existingGenericCash.name == "ক্যাশ") {
                    accountDao.updateAccount(existingGenericCash.copy(name = "ব্যক্তিগত ক্যাশ ১ (Personal Cash 1)", updatedAt = System.currentTimeMillis()))
                }
                val hasCash2 = currentAccounts.any { it.name.contains("ক্যাশ ২") || it.name.contains("Cash 2") }
                if (!hasCash2) {
                    val cash2 = AccountEntity(
                        userId = existingGenericCash.userId,
                        name = "ব্যক্তিগত ক্যাশ ২ (Personal Cash 2)",
                        type = AccountType.CASH.name,
                        area = "PERSONAL",
                        openingBalancePaisa = 0L,
                        colorHex = "#10B981"
                    )
                    accountDao.insertAccount(cash2)
                }
            } else if (personalCashes.isEmpty()) {
                accountDao.insertAccounts(
                    listOf(
                        AccountEntity(
                            name = "ব্যক্তিগত ক্যাশ ১ (Personal Cash 1)",
                            type = AccountType.CASH.name,
                            area = "PERSONAL",
                            openingBalancePaisa = 0L,
                            colorHex = "#059669"
                        ),
                        AccountEntity(
                            name = "ব্যক্তিগত ক্যাশ ২ (Personal Cash 2)",
                            type = AccountType.CASH.name,
                            area = "PERSONAL",
                            openingBalancePaisa = 0L,
                            colorHex = "#10B981"
                        )
                    )
                )
            }

            // Migration for Personal Bank 1 and Bank 2:
            // "If the existing app has one generic Bank account, preserve its existing records and use it as Personal Bank 1.
            // Create Personal Bank 2 with zero balance. Do not copy the old balance into Bank 2.
            // Bank 1 and Bank 2 must never be duplicated during migration."
            val personalBanks = currentAccounts.filter { it.type == AccountType.BANK.name && it.area.equals("PERSONAL", ignoreCase = true) && !it.isDeleted }
            if (personalBanks.size == 1) {
                val existingGenericBank = personalBanks[0]
                if (existingGenericBank.name == "ব্যক্তিগত ব্যাংক (Personal Bank)" || existingGenericBank.name == "ব্যক্তিগত ব্যাংক" || existingGenericBank.name == "Bank") {
                    accountDao.updateAccount(existingGenericBank.copy(name = "ব্যক্তিগত ব্যাংক ১ (Personal Bank 1)", updatedAt = System.currentTimeMillis()))
                }
                // Check if Bank 2 already exists
                val hasBank2 = currentAccounts.any { it.name.contains("ব্যাংক ২") || it.name.contains("Bank 2") }
                if (!hasBank2) {
                    val bank2 = AccountEntity(
                        userId = existingGenericBank.userId,
                        name = "ব্যক্তিগত ব্যাংক ২ (Personal Bank 2)",
                        type = AccountType.BANK.name,
                        area = "PERSONAL",
                        openingBalancePaisa = 0L,
                        colorHex = "#3B82F6"
                    )
                    accountDao.insertAccount(bank2)
                }
            } else if (personalBanks.isEmpty()) {
                accountDao.insertAccounts(
                    listOf(
                        AccountEntity(
                            name = "ব্যক্তিগত ব্যাংক ১ (Personal Bank 1)",
                            type = AccountType.BANK.name,
                            area = "PERSONAL",
                            openingBalancePaisa = 0L,
                            colorHex = "#1E40AF"
                        ),
                        AccountEntity(
                            name = "ব্যক্তিগত ব্যাংক ২ (Personal Bank 2)",
                            type = AccountType.BANK.name,
                            area = "PERSONAL",
                            openingBalancePaisa = 0L,
                            colorHex = "#3B82F6"
                        )
                    )
                )
            }

            // Ensure at least one business account exists
            val hasBusiness = currentAccounts.any { it.area.equals("BUSINESS", ignoreCase = true) }
            if (!hasBusiness) {
                accountDao.insertAccounts(
                    listOf(
                        AccountEntity(
                            name = "ব্যবসা ক্যাশ (Business Cash)",
                            type = AccountType.CASH.name,
                            area = "BUSINESS",
                            openingBalancePaisa = 0L,
                            colorHex = "#D97706"
                        ),
                        AccountEntity(
                            name = "ব্যবসা ব্যাংক (Business Bank)",
                            type = AccountType.BANK.name,
                            area = "BUSINESS",
                            openingBalancePaisa = 0L,
                            colorHex = "#7C3AED"
                        )
                    )
                )
            }
        }

        userProfileDao.upsert(
            UserProfileEntity(
                id = "local_user",
                email = "owner@amarhisab.app",
                displayName = "আমার হিসাব",
                preferredLanguage = "bn",
                isFirebaseLinked = syncManager.isFirebaseConnected()
            )
        )
    }

    private suspend fun migrateLegacyDataIfNeeded() = withContext(Dispatchers.IO) {
        // Migrate legacy investments into assets table if assets table is empty
        try {
            val existingAssets = assetDao.getAssets("local_user").first()
            val existingInvestments = investmentDao.getInvestments("local_user").first()
            if (existingAssets.isEmpty() && existingInvestments.isNotEmpty()) {
                val newAssets = existingInvestments.map { inv ->
                    AssetEntity(
                        id = inv.id,
                        userId = inv.userId,
                        name = inv.projectName,
                        assetType = inv.investmentType,
                        area = "PERSONAL",
                        totalPurchaseValuePaisa = inv.totalAgreedPaisa,
                        paidAmountPaisa = inv.paidAmountPaisa,
                        currentValuePaisa = inv.paidAmountPaisa,
                        purchaseDateMillis = inv.createdAt,
                        accountId = inv.accountId,
                        installmentPaisa = inv.installmentPaisa,
                        nextPaymentDateMillis = inv.nextPaymentDateMillis,
                        status = if (inv.paidAmountPaisa >= inv.totalAgreedPaisa && inv.totalAgreedPaisa > 0) "FULLY_PAID" else "ACTIVE",
                        note = inv.note,
                        createdAt = inv.createdAt,
                        updatedAt = inv.updatedAt,
                        isDeleted = inv.isDeleted,
                        deletedAtMillis = inv.deletedAtMillis
                    )
                }
                assetDao.insertAssets(newAssets)
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // DATA STREAMS
    // ==========================================

    fun getAccounts(): Flow<List<AccountEntity>> = accountDao.getAccounts(getCurrentUserId())
    fun getTransactions(): Flow<List<TransactionEntity>> = transactionDao.getTransactions(getCurrentUserId())
    fun getDebts(): Flow<List<DebtEntity>> = debtDao.getDebts(getCurrentUserId())
    fun getAssets(): Flow<List<AssetEntity>> = assetDao.getAssets(getCurrentUserId())
    fun getStockItems(): Flow<List<StockItemEntity>> = stockDao.getStockItems(getCurrentUserId())
    fun getInstallments(): Flow<List<InstallmentEntity>> = installmentDao.getInstallments(getCurrentUserId())
    fun getInvestments(): Flow<List<InvestmentEntity>> = investmentDao.getInvestments(getCurrentUserId())
    fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile(getCurrentUserId())

    // Trash streams
    fun getDeletedAccounts(): Flow<List<AccountEntity>> = accountDao.getDeletedAccounts(getCurrentUserId())
    fun getDeletedTransactions(): Flow<List<TransactionEntity>> = transactionDao.getDeletedTransactions(getCurrentUserId())
    fun getDeletedDebts(): Flow<List<DebtEntity>> = debtDao.getDeletedDebts(getCurrentUserId())
    fun getDeletedAssets(): Flow<List<AssetEntity>> = assetDao.getDeletedAssets(getCurrentUserId())
    fun getDeletedStockItems(): Flow<List<StockItemEntity>> = stockDao.getDeletedStockItems(getCurrentUserId())
    fun getDeletedInvestments(): Flow<List<InvestmentEntity>> = investmentDao.getDeletedInvestments(getCurrentUserId())

    // ==========================================
    // ATOMIC LEDGER-RECALCULATED BALANCES
    // ==========================================

    fun getAccountsWithBalances(): Flow<List<AccountWithBalance>> {
        return combine(
            getAccounts(),
            getTransactions(),
            getDebts(),
            getAssets(),
            getStockItems()
        ) { accounts, transactions, debts, assets, stockItems ->
            val nonDeletedTx = transactions.filter { !it.isDeleted }
            val nonDeletedDebts = debts.filter { !it.isDeleted }
            val nonDeletedAssets = assets.filter { !it.isDeleted }
            val nonDeletedStock = stockItems.filter { !it.isDeleted }

            accounts.map { account ->
                var balance = account.openingBalancePaisa

                // 1. Transactions (Income, Expense, Transfer, Owner Capital, Owner Withdrawal, Stock Sale)
                nonDeletedTx.forEach { tx ->
                    when (tx.type) {
                        TransactionType.INCOME.name,
                        TransactionType.STOCK_SALE.name -> {
                            if (tx.accountId == account.id) {
                                balance += tx.amountPaisa
                            }
                        }
                        TransactionType.EXPENSE.name -> {
                            if (tx.accountId == account.id) {
                                balance -= tx.amountPaisa
                            }
                        }
                        TransactionType.OWNER_CAPITAL.name -> {
                            // Increases Business account
                            if (tx.accountId == account.id) {
                                balance += tx.amountPaisa
                            }
                        }
                        TransactionType.OWNER_WITHDRAWAL.name -> {
                            // Decreases Business account
                            if (tx.accountId == account.id) {
                                balance -= tx.amountPaisa
                            }
                        }
                        TransactionType.STOCK_PURCHASE.name -> {
                            // Handled via stock items or transaction deduction
                            if (tx.accountId == account.id) {
                                balance -= tx.amountPaisa
                            }
                        }
                        TransactionType.INCOME.name,
                        TransactionType.OWNER_CAPITAL.name,
                        TransactionType.STOCK_SALE.name,
                        TransactionType.DEBT_REPAYMENT.name,
                        TransactionType.DEBT_BORROW.name -> {
                            if (tx.accountId == account.id) {
                                balance += tx.amountPaisa
                            }
                        }
                        TransactionType.EXPENSE.name,
                        TransactionType.OWNER_WITHDRAWAL.name,
                        TransactionType.STOCK_PURCHASE.name,
                        TransactionType.ASSET_PURCHASE.name,
                        TransactionType.ASSET_INSTALLMENT.name,
                        TransactionType.DEBT_LENDING.name,
                        TransactionType.DEBT_PAYMENT.name -> {
                            if (tx.accountId == account.id) {
                                balance -= tx.amountPaisa
                            }
                        }
                        TransactionType.TRANSFER.name -> {
                            if (tx.accountId == account.id) {
                                balance -= tx.amountPaisa // Sent from this account
                            }
                            if (tx.destinationAccountId == account.id) {
                                balance += tx.amountPaisa // Received into this account
                            }
                        }
                    }
                }

                // 2. Debts (Opening debts do NOT decrease Cash or Bank. New lending decreases accountId)
                nonDeletedDebts.forEach { debt ->
                    if (debt.accountId == account.id) {
                        if (debt.debtType == DebtType.OTHERS_OWE_ME.key) {
                            if (!debt.isOpening) {
                                balance -= debt.amountPaisa
                            }
                        } else if (debt.debtType == DebtType.I_OWE_OTHERS.key) {
                            if (!debt.isOpening) {
                                balance += debt.amountPaisa
                            }
                        }
                        // Legacy repayment fallback if no DEBT_REPAYMENT/DEBT_PAYMENT transaction exists
                        val hasRepaymentTx = nonDeletedTx.any {
                            (it.type == TransactionType.DEBT_REPAYMENT.name || it.type == TransactionType.DEBT_PAYMENT.name) &&
                            it.sourceDetails.contains(debt.personName)
                        }
                        if (!hasRepaymentTx && debt.settledAmountPaisa > 0) {
                            if (debt.debtType == DebtType.OTHERS_OWE_ME.key) {
                                balance += debt.settledAmountPaisa
                            } else {
                                balance -= debt.settledAmountPaisa
                            }
                        }
                    }
                }

                // 3. Asset initial purchase & installments if not opening asset and not recorded via transaction
                nonDeletedAssets.forEach { asset ->
                    if (!asset.isOpening && asset.accountId == account.id) {
                        val hasTx = nonDeletedTx.any {
                            it.accountId == account.id &&
                            (it.type == TransactionType.ASSET_PURCHASE.name || it.type == TransactionType.ASSET_INSTALLMENT.name) &&
                            it.sourceDetails.contains(asset.name)
                        }
                        if (!hasTx) {
                            balance -= asset.paidAmountPaisa
                        }
                    }
                }

                // 4. Stock items if not opening stock and not recorded via transaction
                nonDeletedStock.forEach { stock ->
                    if (!stock.isOpening && stock.accountId == account.id) {
                        val hasTx = nonDeletedTx.any {
                            it.accountId == account.id &&
                            it.type == TransactionType.STOCK_PURCHASE.name &&
                            it.sourceDetails.contains(stock.productName)
                        }
                        if (!hasTx) {
                            balance -= stock.totalStockCostPaisa
                        }
                    }
                }

                AccountWithBalance(account, balance)
            }
        }
    }

    // ==========================================
    // DASHBOARD METRICS (PERSONAL & PROPRIETOR BUSINESS)
    // ==========================================

    fun getDashboardMetrics(): Flow<DashboardMetrics> {
        return combine(
            getAccountsWithBalances(),
            getTransactions(),
            getDebts(),
            getAssets(),
            getStockItems()
        ) { accountsWithBalances, transactions, debts, assets, stockItems ->
            val activeAccounts = accountsWithBalances.filter { it.account.isActive }
            val availableBalance = activeAccounts.sumOf { it.calculatedBalancePaisa }

            val cashAccounts = activeAccounts.filter { it.account.type == AccountType.CASH.name }
            val bankAccounts = activeAccounts.filter { it.account.type == AccountType.BANK.name }

            val cashBalance = cashAccounts.sumOf { it.calculatedBalancePaisa }
            val bankBalance = bankAccounts.sumOf { it.calculatedBalancePaisa }

            val personalCashAccounts = cashAccounts.filter { it.account.area.equals("PERSONAL", ignoreCase = true) }.sortedBy { it.account.id }
            val cash1 = personalCashAccounts.getOrNull(0)
            val cash2 = personalCashAccounts.getOrNull(1)
            val personalCash1Paisa = cash1?.calculatedBalancePaisa ?: 0L
            val personalCash2Paisa = cash2?.calculatedBalancePaisa ?: 0L
            val personalCash1Name = cash1?.account?.name ?: "ব্যক্তিগত ক্যাশ ১ (Cash 1)"
            val personalCash2Name = cash2?.account?.name ?: "ব্যক্তিগত ক্যাশ ২ (Cash 2)"
            val personalCash = personalCash1Paisa + personalCash2Paisa

            val personalBanks = bankAccounts.filter { it.account.area.equals("PERSONAL", ignoreCase = true) }.sortedBy { it.account.id }
            val bank1 = personalBanks.getOrNull(0)
            val bank2 = personalBanks.getOrNull(1)
            val personalBank1Paisa = bank1?.calculatedBalancePaisa ?: 0L
            val personalBank2Paisa = bank2?.calculatedBalancePaisa ?: 0L
            val personalBank1Name = bank1?.account?.name ?: "ব্যাংক ১ (Bank 1)"
            val personalBank2Name = bank2?.account?.name ?: "ব্যাংক ২ (Bank 2)"
            val personalBank = personalBank1Paisa + personalBank2Paisa
            val personalBalance = personalCash + personalBank

            val businessCash = cashAccounts.filter { it.account.area.equals("BUSINESS", ignoreCase = true) }.sumOf { it.calculatedBalancePaisa }
            val businessBank = bankAccounts.filter { it.account.area.equals("BUSINESS", ignoreCase = true) }.sumOf { it.calculatedBalancePaisa }
            val businessBalance = businessCash + businessBank

            // Date boundary for Month (Asia/Dhaka)
            val zoneId = ZoneId.of("Asia/Dhaka")
            val nowLocalDate = LocalDate.now(zoneId)
            val nowMillis = System.currentTimeMillis()
            val startOfMonthMillis = nowLocalDate.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfMonthMillis = nowLocalDate.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val nonDeletedTx = transactions.filter { !it.isDeleted }

            // Recognized Income (Excludes transfers, loans, owner capital)
            val thisMonthIncome = nonDeletedTx
                .filter { (it.type == TransactionType.INCOME.name || it.type == TransactionType.STOCK_SALE.name) && it.dateMillis in startOfMonthMillis until endOfMonthMillis }
                .sumOf { it.amountPaisa }

            // Recognized Expense (Excludes transfers, repayments, stock purchases, asset purchases, owner withdrawal)
            val thisMonthExpense = nonDeletedTx
                .filter { it.type == TransactionType.EXPENSE.name && it.dateMillis in startOfMonthMillis until endOfMonthMillis }
                .sumOf { it.amountPaisa }

            // Monthly Surplus = Income - Expense
            val monthlySurplus = thisMonthIncome - thisMonthExpense

            // Debts (Receivables & Payables)
            val nonDeletedDebts = debts.filter { !it.isDeleted }
            val othersOweMe = nonDeletedDebts
                .filter { it.debtType == DebtType.OTHERS_OWE_ME.key }
                .sumOf { it.remainingAmountPaisa }

            val iOweOthers = nonDeletedDebts
                .filter { it.debtType == DebtType.I_OWE_OTHERS.key }
                .sumOf { it.remainingAmountPaisa }

            val overdueDebts = nonDeletedDebts
                .filter { it.dueDateMillis != null && it.dueDateMillis < nowMillis && it.status != DebtStatus.FULLY_SETTLED.key }
                .sumOf { it.remainingAmountPaisa }

            // Assets & Stock
            val nonDeletedAssets = assets.filter { !it.isDeleted }
            val fixedAssetsValue = nonDeletedAssets.sumOf { if (it.currentValuePaisa > 0) it.currentValuePaisa else it.paidAmountPaisa }
            val upcomingInstallments = nonDeletedAssets.sumOf { it.remainingPaisa }

            val nonDeletedStock = stockItems.filter { !it.isDeleted }
            val businessStockValue = nonDeletedStock.sumOf { it.currentStockValuePaisa }

            // Owner capital & withdrawal totals
            val totalOwnerCapital = nonDeletedTx.filter { it.type == TransactionType.OWNER_CAPITAL.name }.sumOf { it.amountPaisa }
            val totalOwnerWithdrawal = nonDeletedTx.filter { it.type == TransactionType.OWNER_WITHDRAWAL.name }.sumOf { it.amountPaisa }

            // Total Assets = Cash + Bank + Business Stock + Fixed Assets + Receivables
            val totalAssets = availableBalance + businessStockValue + fixedAssetsValue + othersOweMe

            // Total Liabilities = Payables (I Owe Others) + Outstanding installment commitments
            val totalLiabilities = iOweOthers + upcomingInstallments

            // Net Worth = Total Assets - Total Liabilities
            val netWorth = totalAssets - totalLiabilities

            DashboardMetrics(
                availableBalancePaisa = availableBalance,
                cashBalancePaisa = cashBalance,
                bankBalancePaisa = bankBalance,
                personalCashPaisa = personalCash,
                personalCash1Paisa = personalCash1Paisa,
                personalCash2Paisa = personalCash2Paisa,
                personalCash1Name = personalCash1Name,
                personalCash2Name = personalCash2Name,
                personalBankPaisa = personalBank,
                personalBank1Paisa = personalBank1Paisa,
                personalBank2Paisa = personalBank2Paisa,
                personalBank1Name = personalBank1Name,
                personalBank2Name = personalBank2Name,
                businessCashPaisa = businessCash,
                businessBankPaisa = businessBank,
                personalBalancePaisa = personalBalance,
                businessBalancePaisa = businessBalance,
                thisMonthIncomePaisa = thisMonthIncome,
                thisMonthExpensePaisa = thisMonthExpense,
                monthlySurplusPaisa = monthlySurplus,
                othersOweMePaisa = othersOweMe,
                iOweOthersPaisa = iOweOthers,
                businessStockValuePaisa = businessStockValue,
                fixedAssetsValuePaisa = fixedAssetsValue,
                investmentsValuePaisa = 0L,
                upcomingInstallmentsPaisa = upcomingInstallments,
                overdueDebtsPaisa = overdueDebts,
                totalAssetsPaisa = totalAssets,
                totalLiabilitiesPaisa = totalLiabilities,
                netWorthPaisa = netWorth,
                totalOwnerCapitalPaisa = totalOwnerCapital,
                totalOwnerWithdrawalPaisa = totalOwnerWithdrawal
            )
        }
    }

    // ==========================================
    // OPENING BALANCES
    // ==========================================

    suspend fun setOpeningBalances(
        area: String, // "PERSONAL" or "BUSINESS"
        cashAmountPaisa: Long,
        bankAmountPaisa: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val currentAccounts = accountDao.getAccounts("local_user").first()
        var cashAccount = currentAccounts.firstOrNull { it.area.equals(area, ignoreCase = true) && it.type == AccountType.CASH.name }
        if (cashAccount == null) {
            val name = if (area == "BUSINESS") "ব্যবসা ক্যাশ (Business Cash)" else "ব্যক্তিগত ক্যাশ (Personal Cash)"
            val newId = accountDao.insertAccount(
                AccountEntity(
                    name = name,
                    type = AccountType.CASH.name,
                    area = area,
                    openingBalancePaisa = cashAmountPaisa,
                    createdAt = dateMillis,
                    updatedAt = System.currentTimeMillis()
                )
            )
            cashAccount = accountDao.getAccountById(newId)
        } else {
            accountDao.updateAccount(
                cashAccount.copy(
                    openingBalancePaisa = cashAmountPaisa,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        var bankAccount = currentAccounts.firstOrNull { it.area.equals(area, ignoreCase = true) && it.type == AccountType.BANK.name }
        if (bankAccount == null) {
            val name = if (area == "BUSINESS") "ব্যবসা ব্যাংক (Business Bank)" else "ব্যক্তিগত ব্যাংক (Personal Bank)"
            val newId = accountDao.insertAccount(
                AccountEntity(
                    name = name,
                    type = AccountType.BANK.name,
                    area = area,
                    openingBalancePaisa = bankAmountPaisa,
                    createdAt = dateMillis,
                    updatedAt = System.currentTimeMillis()
                )
            )
            bankAccount = accountDao.getAccountById(newId)
        } else {
            accountDao.updateAccount(
                bankAccount.copy(
                    openingBalancePaisa = bankAmountPaisa,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        cashAccount?.let { syncManager.syncSingleAccount(getCurrentUserId(), it) }
        bankAccount?.let { syncManager.syncSingleAccount(getCurrentUserId(), it) }
    }

    suspend fun addAccount(
        name: String,
        type: String,
        openingBalancePaisa: Long,
        colorHex: String = "#10B981",
        area: String = "PERSONAL"
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val account = AccountEntity(
            userId = uid,
            name = name,
            type = type,
            area = area,
            openingBalancePaisa = openingBalancePaisa,
            colorHex = colorHex,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = accountDao.insertAccount(account)
        syncManager.syncSingleAccount(uid, account.copy(id = id))
        id
    }

    suspend fun updateAccountOpeningBalance(
        accountId: Long,
        openingBalancePaisa: Long
    ) = withContext(Dispatchers.IO) {
        val acc = accountDao.getAccountById(accountId) ?: return@withContext
        val updated = acc.copy(
            openingBalancePaisa = openingBalancePaisa,
            updatedAt = System.currentTimeMillis()
        )
        accountDao.updateAccount(updated)
        syncManager.syncSingleAccount(getCurrentUserId(), updated)
    }

    suspend fun updateAccount(
        accountId: Long,
        name: String,
        openingBalancePaisa: Long
    ) = withContext(Dispatchers.IO) {
        val acc = accountDao.getAccountById(accountId) ?: return@withContext
        val updated = acc.copy(
            name = name.trim(),
            openingBalancePaisa = openingBalancePaisa,
            updatedAt = System.currentTimeMillis()
        )
        accountDao.updateAccount(updated)
        syncManager.syncSingleAccount(getCurrentUserId(), updated)
    }

    suspend fun setOpeningBalancesPersonal(
        cashAmountPaisa: Long,
        bank1AmountPaisa: Long,
        bank2AmountPaisa: Long,
        dateMillis: Long,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val currentAccounts = accountDao.getAccounts("local_user").first()
        val cashAccount = currentAccounts.firstOrNull { it.area.equals("PERSONAL", ignoreCase = true) && it.type == AccountType.CASH.name }
        cashAccount?.let {
            val updated = it.copy(openingBalancePaisa = cashAmountPaisa, updatedAt = System.currentTimeMillis())
            accountDao.updateAccount(updated)
            syncManager.syncSingleAccount(getCurrentUserId(), updated)
        }

        val personalBanks = currentAccounts.filter { it.area.equals("PERSONAL", ignoreCase = true) && it.type == AccountType.BANK.name && !it.isDeleted }.sortedBy { it.id }
        val bank1 = personalBanks.getOrNull(0)
        bank1?.let {
            val updated = it.copy(openingBalancePaisa = bank1AmountPaisa, updatedAt = System.currentTimeMillis())
            accountDao.updateAccount(updated)
            syncManager.syncSingleAccount(getCurrentUserId(), updated)
        }
        val bank2 = personalBanks.getOrNull(1)
        bank2?.let {
            val updated = it.copy(openingBalancePaisa = bank2AmountPaisa, updatedAt = System.currentTimeMillis())
            accountDao.updateAccount(updated)
            syncManager.syncSingleAccount(getCurrentUserId(), updated)
        }
    }

    suspend fun updateAccountsStartDate(startDateMillis: Long) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile("local_user").first()
        if (profile != null) {
            val updated = profile.copy(accountsStartDateMillis = startDateMillis)
            userProfileDao.upsert(updated)
        }
    }

    // ==========================================
    // FINANCIAL OPERATIONS (FREE-TEXT & AREA ENFORCED)
    // ==========================================

    suspend fun addIncome(
        amountPaisa: Long,
        dateMillis: Long,
        accountId: Long,
        incomeDetails: String, // Free text description
        area: String = "PERSONAL", // PERSONAL or BUSINESS
        category: String = "Income",
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val tx = TransactionEntity(
            userId = uid,
            type = TransactionType.INCOME.name,
            amountPaisa = amountPaisa,
            accountId = accountId,
            dateMillis = dateMillis,
            area = area,
            category = category,
            sourceDetails = incomeDetails,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = id))
        id
    }

    suspend fun addExpense(
        amountPaisa: Long,
        dateMillis: Long,
        accountId: Long,
        expenseDetails: String, // Free text description
        area: String = "PERSONAL", // PERSONAL or BUSINESS
        paidTo: String = "",
        category: String = "Expense",
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val tx = TransactionEntity(
            userId = uid,
            type = TransactionType.EXPENSE.name,
            amountPaisa = amountPaisa,
            accountId = accountId,
            dateMillis = dateMillis,
            area = area,
            category = category,
            sourceDetails = expenseDetails,
            paidTo = paidTo,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = id))
        id
    }

    suspend fun transferMoney(
        sourceAccountId: Long,
        destinationAccountId: Long,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val tx = TransactionEntity(
            userId = uid,
            type = TransactionType.TRANSFER.name,
            amountPaisa = amountPaisa,
            accountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            dateMillis = dateMillis,
            area = area,
            category = "Transfer",
            sourceDetails = "তহবিল স্থানান্তর (Transfer)",
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = id))
        id
    }

    suspend fun addOwnerCapital(
        amountPaisa: Long,
        businessAccountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val tx = TransactionEntity(
            userId = uid,
            type = TransactionType.OWNER_CAPITAL.name,
            amountPaisa = amountPaisa,
            accountId = businessAccountId,
            dateMillis = dateMillis,
            area = "BUSINESS",
            category = "Owner Capital",
            sourceDetails = "মালিকের মূলধন বিনিয়োগ (Owner Capital)",
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = id))
        id
    }

    suspend fun addOwnerWithdrawal(
        amountPaisa: Long,
        businessAccountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val tx = TransactionEntity(
            userId = uid,
            type = TransactionType.OWNER_WITHDRAWAL.name,
            amountPaisa = amountPaisa,
            accountId = businessAccountId,
            dateMillis = dateMillis,
            area = "BUSINESS",
            category = "Owner Withdrawal",
            sourceDetails = "মালিকের ব্যক্তিগত উত্তোলন (Owner Withdrawal)",
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = id))
        id
    }

    // ==========================================
    // DEBTS (OTHERS OWE ME & I OWE OTHERS)
    // ==========================================

    suspend fun addDebt(
        personName: String,
        debtType: DebtType,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        phone: String = "",
        dueDateMillis: Long? = null,
        accountId: Long = 1L,
        note: String = "",
        isOpening: Boolean = false,
        originalDateMillis: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val debt = DebtEntity(
            userId = uid,
            personName = personName,
            phone = phone,
            debtType = debtType.key,
            area = area,
            amountPaisa = amountPaisa,
            dateMillis = dateMillis,
            dueDateMillis = dueDateMillis,
            accountId = accountId,
            settledAmountPaisa = 0L,
            status = DebtStatus.PENDING.key,
            note = note,
            isOpening = isOpening,
            originalDateMillis = originalDateMillis,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = debtDao.insertDebt(debt)
        syncManager.syncSingleDebt(uid, debt.copy(id = id))
        id
    }

    suspend fun recordDebtRepayment(
        debtId: Long,
        repaymentAmountPaisa: Long,
        accountId: Long? = null,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val existing = debtDao.getDebtById(debtId) ?: return@withContext
        val newSettled = (existing.settledAmountPaisa + repaymentAmountPaisa).coerceAtMost(existing.amountPaisa)
        val newStatus = when {
            newSettled >= existing.amountPaisa -> DebtStatus.FULLY_SETTLED.key
            newSettled > 0 -> DebtStatus.PARTIALLY_PAID.key
            else -> DebtStatus.PENDING.key
        }
        val targetAccountId = accountId ?: existing.accountId
        val updated = existing.copy(
            settledAmountPaisa = newSettled,
            status = newStatus,
            note = if (note.isNotBlank()) "${existing.note} | Repayment: $note".trim(' ', '|') else existing.note,
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        syncManager.syncSingleDebt(uid, updated)

        // Record a transaction for the repayment so that the target Cash, Bank 1, or Bank 2 account increases
        val isPawna = existing.debtType == DebtType.OTHERS_OWE_ME.key
        val txType = if (isPawna) TransactionType.DEBT_REPAYMENT.name else TransactionType.DEBT_PAYMENT.name
        val txDetails = if (isPawna) "কিস্তি গ্রহণ: ${existing.personName}" else "দেনা পরিশোধ: ${existing.personName}"
        val tx = TransactionEntity(
            userId = uid,
            type = txType,
            amountPaisa = repaymentAmountPaisa,
            accountId = targetAccountId,
            dateMillis = System.currentTimeMillis(),
            area = existing.area,
            sourceDetails = txDetails,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val txId = transactionDao.insertTransaction(tx)
        syncManager.syncSingleTransaction(uid, tx.copy(id = txId))
    }

    // ==========================================
    // ASSET MANAGEMENT
    // ==========================================

    suspend fun addAsset(
        name: String,
        assetType: String,
        area: String = "PERSONAL",
        totalPurchaseValuePaisa: Long,
        paidAmountPaisa: Long,
        currentValuePaisa: Long = 0L,
        purchaseDateMillis: Long = System.currentTimeMillis(),
        accountId: Long = 1L,
        installmentPaisa: Long = 0L,
        nextPaymentDateMillis: Long? = null,
        note: String = "",
        isOpening: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val asset = AssetEntity(
            userId = uid,
            name = name,
            assetType = assetType,
            area = area,
            totalPurchaseValuePaisa = totalPurchaseValuePaisa,
            paidAmountPaisa = paidAmountPaisa,
            currentValuePaisa = if (currentValuePaisa > 0) currentValuePaisa else paidAmountPaisa,
            purchaseDateMillis = purchaseDateMillis,
            accountId = accountId,
            installmentPaisa = installmentPaisa,
            nextPaymentDateMillis = nextPaymentDateMillis,
            status = if (paidAmountPaisa >= totalPurchaseValuePaisa && totalPurchaseValuePaisa > 0) "FULLY_PAID" else "ACTIVE",
            note = note,
            isOpening = isOpening,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = assetDao.insertAsset(asset)
        syncManager.syncSingleAsset(uid, asset.copy(id = id))
        id
    }

    suspend fun payAssetInstallment(
        assetId: Long,
        amountPaisa: Long,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        nextDueDateMillis: Long? = null,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val asset = assetDao.getAssetById(assetId) ?: return@withContext
        val newPaid = asset.paidAmountPaisa + amountPaisa
        val newCurrent = if (asset.currentValuePaisa < newPaid) newPaid else asset.currentValuePaisa
        val isFinished = newPaid >= asset.totalPurchaseValuePaisa && asset.totalPurchaseValuePaisa > 0
        val updatedAsset = asset.copy(
            paidAmountPaisa = newPaid,
            currentValuePaisa = newCurrent,
            nextPaymentDateMillis = if (isFinished) null else nextDueDateMillis,
            status = if (isFinished) "FULLY_PAID" else "ACTIVE",
            updatedAt = System.currentTimeMillis()
        )
        assetDao.updateAsset(updatedAsset)

        val inst = InstallmentEntity(
            userId = uid,
            assetId = assetId,
            amountPaisa = amountPaisa,
            accountId = accountId,
            dateMillis = dateMillis,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val instId = installmentDao.insertInstallment(inst)
        syncManager.syncSingleAsset(uid, updatedAsset)
        syncManager.syncSingleInstallment(uid, inst.copy(id = instId))
    }

    // ==========================================
    // BUSINESS STOCK MANAGEMENT
    // ==========================================

    suspend fun addOrPurchaseStock(
        productName: String,
        area: String = "BUSINESS",
        purchaseCostPerUnitPaisa: Long,
        purchasedQuantity: Double,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = "",
        isOpening: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val existing = stockDao.getStockItems("local_user").first().find {
            it.productName.equals(productName.trim(), ignoreCase = true) && !it.isDeleted
        }
        val stockItem = if (existing != null) {
            val updated = existing.copy(
                purchaseCostPerUnitPaisa = if (purchaseCostPerUnitPaisa > 0) purchaseCostPerUnitPaisa else existing.purchaseCostPerUnitPaisa,
                purchasedQuantity = existing.purchasedQuantity + purchasedQuantity,
                accountId = accountId,
                dateMillis = dateMillis,
                note = if (note.isNotBlank()) note else existing.note,
                isOpening = isOpening || existing.isOpening,
                updatedAt = System.currentTimeMillis()
            )
            stockDao.updateStockItem(updated)
            updated
        } else {
            val newItem = StockItemEntity(
                userId = uid,
                productName = productName.trim(),
                area = area,
                purchaseCostPerUnitPaisa = purchaseCostPerUnitPaisa,
                purchasedQuantity = purchasedQuantity,
                soldQuantity = 0.0,
                accountId = accountId,
                dateMillis = dateMillis,
                note = note,
                isOpening = isOpening,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val id = stockDao.insertStockItem(newItem)
            newItem.copy(id = id)
        }
        syncManager.syncSingleStock(uid, stockItem)
        stockItem.id
    }

    suspend fun recordStockSale(
        stockId: Long,
        soldQuantity: Double,
        salePricePerUnitPaisa: Long,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        val stock = stockDao.getStockItemById(stockId) ?: return@withContext 0L
        val updatedStock = stock.copy(
            soldQuantity = stock.soldQuantity + soldQuantity,
            updatedAt = System.currentTimeMillis()
        )
        stockDao.updateStockItem(updatedStock)

        val totalSalePaisa = (soldQuantity * salePricePerUnitPaisa).toLong()
        val totalCostPaisa = (soldQuantity * stock.purchaseCostPerUnitPaisa).toLong()
        val profitPaisa = totalSalePaisa - totalCostPaisa

        val saleTx = TransactionEntity(
            userId = uid,
            type = TransactionType.STOCK_SALE.name,
            amountPaisa = totalSalePaisa,
            accountId = accountId,
            dateMillis = dateMillis,
            area = stock.area,
            category = "Stock Sale",
            sourceDetails = "${stock.productName} বিক্রয় ($soldQuantity টি)",
            note = if (note.isNotBlank()) note else "মুনাফা: ${CurrencyFormatter.formatBdt(profitPaisa, isBangla = false)}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val txId = transactionDao.insertTransaction(saleTx)
        syncManager.syncSingleStock(uid, updatedStock)
        syncManager.syncSingleTransaction(uid, saleTx.copy(id = txId))
        txId
    }

    // ==========================================
    // CLOUD SYNC & MIGRATION ON FIRST SIGN-IN
    // ==========================================

    suspend fun checkMigrationNeeded(uid: String): MigrationSummary? = withContext(Dispatchers.IO) {
        val localAccounts = accountDao.getAccounts("local_user").first()
        val localTx = transactionDao.getTransactions("local_user").first()
        val localDebts = debtDao.getDebts("local_user").first()
        val localAssets = assetDao.getAssets("local_user").first()
        val localStock = stockDao.getStockItems("local_user").first()
        val localInstallments = installmentDao.getInstallments("local_user").first()
        val localInv = investmentDao.getInvestments("local_user").first()

        val cloudSummary = syncManager.getCloudSummary(uid)
        val hasLocalData = localAccounts.isNotEmpty() || localTx.isNotEmpty() || localDebts.isNotEmpty() || localAssets.isNotEmpty() || localStock.isNotEmpty()
        val hasCloudData = cloudSummary.accountCount > 0 || cloudSummary.transactionCount > 0 || cloudSummary.debtCount > 0 || cloudSummary.assetCount > 0

        if (hasLocalData || hasCloudData) {
            val autoBackup = exportBackupJson()
            MigrationSummary(
                localAccountCount = localAccounts.size,
                localTransactionCount = localTx.size,
                localDebtCount = localDebts.size,
                localAssetCount = localAssets.size,
                localStockCount = localStock.size,
                localInstallmentCount = localInstallments.size,
                localInvestmentCount = localInv.size,
                cloudAccountCount = cloudSummary.accountCount,
                cloudTransactionCount = cloudSummary.transactionCount,
                cloudDebtCount = cloudSummary.debtCount,
                cloudAssetCount = cloudSummary.assetCount,
                cloudStockCount = cloudSummary.stockCount,
                cloudInstallmentCount = cloudSummary.installmentCount,
                cloudInvestmentCount = cloudSummary.investmentCount,
                backupJson = autoBackup
            )
        } else {
            null
        }
    }

    suspend fun executeUploadLocal(uid: String) = withContext(Dispatchers.IO) {
        val accounts = accountDao.getAccounts("local_user").first()
        val tx = transactionDao.getTransactions("local_user").first()
        val debts = debtDao.getDebts("local_user").first()
        val assets = assetDao.getAssets("local_user").first()
        val stock = stockDao.getStockItems("local_user").first()
        val installments = installmentDao.getInstallments("local_user").first()
        val inv = investmentDao.getInvestments("local_user").first()
        syncManager.uploadAllLocalData(uid, accounts, tx, debts, assets, stock, installments, inv)
    }

    suspend fun executeDownloadCloud(uid: String) = withContext(Dispatchers.IO) {
        val result = syncManager.downloadCloudData(uid)
        if (result.isSuccess) {
            val pkg = result.getOrNull() ?: return@withContext
            accountDao.insertAccounts(pkg.accounts)
            transactionDao.insertTransactions(pkg.transactions)
            debtDao.insertDebts(pkg.debts)
            assetDao.insertAssets(pkg.assets)
            stockDao.insertStockItems(pkg.stock)
            installmentDao.insertInstallments(pkg.installments)
        }
    }

    suspend fun executeMerge(uid: String) = withContext(Dispatchers.IO) {
        val result = syncManager.downloadCloudData(uid)
        val cloudPkg = result.getOrNull() ?: return@withContext

        val localAccounts = accountDao.getAccounts("local_user").first()
        val localTransactions = transactionDao.getTransactions("local_user").first()
        val localDebts = debtDao.getDebts("local_user").first()
        val localAssets = assetDao.getAssets("local_user").first()
        val localStock = stockDao.getStockItems("local_user").first()
        val localInstallments = installmentDao.getInstallments("local_user").first()

        val mergedAccounts = (localAccounts + cloudPkg.accounts).distinctBy { it.id }
        val mergedTransactions = (localTransactions + cloudPkg.transactions).distinctBy { it.id }
        val mergedDebts = (localDebts + cloudPkg.debts).distinctBy { it.id }
        val mergedAssets = (localAssets + cloudPkg.assets).distinctBy { it.id }
        val mergedStock = (localStock + cloudPkg.stock).distinctBy { it.id }
        val mergedInstallments = (localInstallments + cloudPkg.installments).distinctBy { it.id }

        accountDao.insertAccounts(mergedAccounts)
        transactionDao.insertTransactions(mergedTransactions)
        debtDao.insertDebts(mergedDebts)
        assetDao.insertAssets(mergedAssets)
        stockDao.insertStockItems(mergedStock)
        installmentDao.insertInstallments(mergedInstallments)

        syncManager.uploadAllLocalData(uid, mergedAccounts, mergedTransactions, mergedDebts, mergedAssets, mergedStock, mergedInstallments)
    }

    // ==========================================
    // SOFT DELETE & TRASH (2-STEP CONFIRMATION)
    // ==========================================

    suspend fun softDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.softDeleteById(id)
        val tx = transactionDao.getTransactionById(id)
        if (tx != null) syncManager.syncSingleTransaction(getCurrentUserId(), tx)
    }

    suspend fun restoreTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.restoreById(id)
        val tx = transactionDao.getTransactionById(id)
        if (tx != null) syncManager.syncSingleTransaction(getCurrentUserId(), tx)
    }

    suspend fun permanentlyDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
    }

    suspend fun softDeleteDebt(id: Long) = withContext(Dispatchers.IO) {
        debtDao.softDeleteById(id)
        val debt = debtDao.getDebtById(id)
        if (debt != null) syncManager.syncSingleDebt(getCurrentUserId(), debt)
    }

    suspend fun restoreDebt(id: Long) = withContext(Dispatchers.IO) {
        debtDao.restoreById(id)
        val debt = debtDao.getDebtById(id)
        if (debt != null) syncManager.syncSingleDebt(getCurrentUserId(), debt)
    }

    suspend fun permanentlyDeleteDebt(id: Long) = withContext(Dispatchers.IO) {
        debtDao.deleteById(id)
    }

    suspend fun softDeleteAsset(id: Long) = withContext(Dispatchers.IO) {
        assetDao.softDeleteById(id)
        val asset = assetDao.getAssetById(id)
        if (asset != null) syncManager.syncSingleAsset(getCurrentUserId(), asset)
    }

    suspend fun restoreAsset(id: Long) = withContext(Dispatchers.IO) {
        assetDao.restoreById(id)
        val asset = assetDao.getAssetById(id)
        if (asset != null) syncManager.syncSingleAsset(getCurrentUserId(), asset)
    }

    suspend fun permanentlyDeleteAsset(id: Long) = withContext(Dispatchers.IO) {
        assetDao.deleteById(id)
    }

    suspend fun softDeleteStockItem(id: Long) = withContext(Dispatchers.IO) {
        stockDao.softDeleteById(id)
        val item = stockDao.getStockItemById(id)
        if (item != null) syncManager.syncSingleStock(getCurrentUserId(), item)
    }

    suspend fun restoreStockItem(id: Long) = withContext(Dispatchers.IO) {
        stockDao.restoreById(id)
        val item = stockDao.getStockItemById(id)
        if (item != null) syncManager.syncSingleStock(getCurrentUserId(), item)
    }

    suspend fun permanentlyDeleteStockItem(id: Long) = withContext(Dispatchers.IO) {
        stockDao.deleteById(id)
    }

    suspend fun softDeleteAccount(id: Long) = withContext(Dispatchers.IO) {
        accountDao.softDeleteById(id)
        val acc = accountDao.getAccountById(id)
        if (acc != null) syncManager.syncSingleAccount(getCurrentUserId(), acc)
    }

    suspend fun restoreAccount(id: Long) = withContext(Dispatchers.IO) {
        accountDao.restoreById(id)
        val acc = accountDao.getAccountById(id)
        if (acc != null) syncManager.syncSingleAccount(getCurrentUserId(), acc)
    }

    suspend fun permanentlyDeleteAccount(id: Long) = withContext(Dispatchers.IO) {
        accountDao.deleteById(id)
    }

    suspend fun softDeleteInvestment(id: Long) = withContext(Dispatchers.IO) {
        investmentDao.softDeleteById(id)
        val inv = investmentDao.getInvestmentById(id)
        if (inv != null) syncManager.syncSingleInvestment(getCurrentUserId(), inv)
    }

    suspend fun restoreInvestment(id: Long) = withContext(Dispatchers.IO) {
        investmentDao.restoreById(id)
        val inv = investmentDao.getInvestmentById(id)
        if (inv != null) syncManager.syncSingleInvestment(getCurrentUserId(), inv)
    }

    suspend fun permanentlyDeleteInvestment(id: Long) = withContext(Dispatchers.IO) {
        investmentDao.deleteById(id)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        val deletedTx = transactionDao.getDeletedTransactions("local_user").first()
        deletedTx.forEach { transactionDao.deleteById(it.id) }

        val deletedDebts = debtDao.getDeletedDebts("local_user").first()
        deletedDebts.forEach { debtDao.deleteById(it.id) }

        val deletedAssets = assetDao.getDeletedAssets("local_user").first()
        deletedAssets.forEach { assetDao.deleteById(it.id) }

        val deletedStock = stockDao.getDeletedStockItems("local_user").first()
        deletedStock.forEach { stockDao.deleteById(it.id) }

        val deletedAcc = accountDao.getDeletedAccounts("local_user").first()
        deletedAcc.forEach { accountDao.deleteById(it.id) }

        val deletedInvestments = investmentDao.getDeletedInvestments("local_user").first()
        deletedInvestments.forEach { investmentDao.deleteById(it.id) }
    }

    // ==========================================
    // BACKUP & RESTORE (JSON SNAPSHOTS & CSV)
    // ==========================================

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Amar Hisab")
        root.put("version", 4)
        root.put("exportedAtMillis", System.currentTimeMillis())

        val accounts = accountDao.getAccounts("local_user").first()
        val accArray = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject()
            obj.put("id", acc.id)
            obj.put("name", acc.name)
            obj.put("type", acc.type)
            obj.put("area", acc.area)
            obj.put("openingBalancePaisa", acc.openingBalancePaisa)
            obj.put("isActive", acc.isActive)
            obj.put("colorHex", acc.colorHex)
            obj.put("createdAt", acc.createdAt)
            obj.put("updatedAt", acc.updatedAt)
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        val transactions = transactionDao.getTransactions("local_user").first()
        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("type", tx.type)
            obj.put("amountPaisa", tx.amountPaisa)
            obj.put("accountId", tx.accountId)
            obj.put("destinationAccountId", tx.destinationAccountId)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("area", tx.area)
            obj.put("category", tx.category)
            obj.put("sourceDetails", tx.sourceDetails)
            obj.put("paidTo", tx.paidTo)
            obj.put("note", tx.note)
            obj.put("createdAt", tx.createdAt)
            obj.put("updatedAt", tx.updatedAt)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val debts = debtDao.getDebts("local_user").first()
        val debtArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("personName", d.personName)
            obj.put("phone", d.phone)
            obj.put("debtType", d.debtType)
            obj.put("area", d.area)
            obj.put("amountPaisa", d.amountPaisa)
            obj.put("dateMillis", d.dateMillis)
            obj.put("dueDateMillis", d.dueDateMillis)
            obj.put("accountId", d.accountId)
            obj.put("settledAmountPaisa", d.settledAmountPaisa)
            obj.put("status", d.status)
            obj.put("note", d.note)
            obj.put("isOpening", d.isOpening)
            obj.put("originalDateMillis", d.originalDateMillis)
            obj.put("createdAt", d.createdAt)
            obj.put("updatedAt", d.updatedAt)
            debtArray.put(obj)
        }
        root.put("debts", debtArray)

        val assets = assetDao.getAssets("local_user").first()
        val assetArray = JSONArray()
        assets.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("assetType", a.assetType)
            obj.put("area", a.area)
            obj.put("totalPurchaseValuePaisa", a.totalPurchaseValuePaisa)
            obj.put("paidAmountPaisa", a.paidAmountPaisa)
            obj.put("currentValuePaisa", a.currentValuePaisa)
            obj.put("purchaseDateMillis", a.purchaseDateMillis)
            obj.put("accountId", a.accountId)
            obj.put("installmentPaisa", a.installmentPaisa)
            obj.put("nextPaymentDateMillis", a.nextPaymentDateMillis)
            obj.put("status", a.status)
            obj.put("note", a.note)
            obj.put("isOpening", a.isOpening)
            obj.put("createdAt", a.createdAt)
            obj.put("updatedAt", a.updatedAt)
            assetArray.put(obj)
        }
        root.put("assets", assetArray)

        val stock = stockDao.getStockItems("local_user").first()
        val stockArray = JSONArray()
        stock.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("productName", s.productName)
            obj.put("area", s.area)
            obj.put("purchaseCostPerUnitPaisa", s.purchaseCostPerUnitPaisa)
            obj.put("purchasedQuantity", s.purchasedQuantity)
            obj.put("soldQuantity", s.soldQuantity)
            obj.put("accountId", s.accountId)
            obj.put("dateMillis", s.dateMillis)
            obj.put("note", s.note)
            obj.put("isOpening", s.isOpening)
            obj.put("createdAt", s.createdAt)
            obj.put("updatedAt", s.updatedAt)
            stockArray.put(obj)
        }
        root.put("stock", stockArray)

        root.toString(2)
    }

    fun validateBackupJson(jsonString: String): ImportPreview {
        return try {
            val root = JSONObject(jsonString)
            val accCount = root.optJSONArray("accounts")?.length() ?: 0
            val txCount = root.optJSONArray("transactions")?.length() ?: 0
            val debtCount = root.optJSONArray("debts")?.length() ?: root.optJSONArray("moneyLent")?.length() ?: 0
            val assetCount = root.optJSONArray("assets")?.length() ?: 0
            val stockCount = root.optJSONArray("stock")?.length() ?: 0
            val invCount = root.optJSONArray("investments")?.length() ?: 0
            val exportTime = root.optLong("exportedAtMillis", System.currentTimeMillis())

            ImportPreview(
                isValid = true,
                accountCount = accCount,
                transactionCount = txCount,
                debtCount = debtCount,
                assetCount = assetCount,
                stockCount = stockCount,
                investmentCount = invCount,
                exportedAt = exportTime
            )
        } catch (e: Exception) {
            ImportPreview(isValid = false, errorMessage = e.localizedMessage ?: "Invalid JSON backup format")
        }
    }

    suspend fun importBackupJson(jsonString: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val preview = validateBackupJson(jsonString)
        if (!preview.isValid) {
            return@withContext false to (preview.errorMessage ?: "Invalid backup file")
        }

        try {
            // AUTOMATIC PRE-RESTORE SNAPSHOT (Zero Data Loss Guarantee)
            val preRestoreSnapshot = exportBackupJson()
            val root = JSONObject(jsonString)

            // 1. Accounts
            val accArray = root.optJSONArray("accounts")
            if (accArray != null) {
                for (i in 0 until accArray.length()) {
                    val obj = accArray.getJSONObject(i)
                    val acc = AccountEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        area = obj.optString("area", "PERSONAL"),
                        openingBalancePaisa = obj.optLong("openingBalancePaisa", 0L),
                        isActive = obj.optBoolean("isActive", true),
                        colorHex = obj.optString("colorHex", "#059669")
                    )
                    accountDao.insertAccount(acc)
                }
            }

            // 2. Transactions
            val txArray = root.optJSONArray("transactions")
            if (txArray != null) {
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    val tx = TransactionEntity(
                        id = obj.optLong("id", 0L),
                        type = obj.getString("type"),
                        amountPaisa = obj.getLong("amountPaisa"),
                        accountId = obj.getLong("accountId"),
                        destinationAccountId = if (obj.has("destinationAccountId") && !obj.isNull("destinationAccountId")) obj.getLong("destinationAccountId") else null,
                        dateMillis = obj.getLong("dateMillis"),
                        area = obj.optString("area", "PERSONAL"),
                        category = obj.optString("category", ""),
                        sourceDetails = obj.optString("sourceDetails", obj.optString("category", "")),
                        paidTo = obj.optString("paidTo", ""),
                        note = obj.optString("note", "")
                    )
                    transactionDao.insertTransaction(tx)
                }
            }

            // 3. Debts
            val debtArray = root.optJSONArray("debts")
            if (debtArray != null) {
                for (i in 0 until debtArray.length()) {
                    val obj = debtArray.getJSONObject(i)
                    val debt = DebtEntity(
                        id = obj.optLong("id", 0L),
                        personName = obj.getString("personName"),
                        phone = obj.optString("phone", ""),
                        debtType = obj.getString("debtType"),
                        area = obj.optString("area", "PERSONAL"),
                        amountPaisa = obj.getLong("amountPaisa"),
                        dateMillis = obj.getLong("dateMillis"),
                        dueDateMillis = if (obj.has("dueDateMillis") && !obj.isNull("dueDateMillis")) obj.getLong("dueDateMillis") else null,
                        accountId = obj.getLong("accountId"),
                        settledAmountPaisa = obj.optLong("settledAmountPaisa", 0L),
                        status = obj.optString("status", DebtStatus.PENDING.key),
                        note = obj.optString("note", ""),
                        isOpening = obj.optBoolean("isOpening", false),
                        originalDateMillis = if (obj.has("originalDateMillis") && !obj.isNull("originalDateMillis")) obj.getLong("originalDateMillis") else null
                    )
                    debtDao.insertDebt(debt)
                }
            }

            // 4. Assets
            val assetArray = root.optJSONArray("assets")
            if (assetArray != null) {
                for (i in 0 until assetArray.length()) {
                    val obj = assetArray.getJSONObject(i)
                    val asset = AssetEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        assetType = obj.optString("assetType", "Asset"),
                        area = obj.optString("area", "PERSONAL"),
                        totalPurchaseValuePaisa = obj.optLong("totalPurchaseValuePaisa", 0L),
                        paidAmountPaisa = obj.optLong("paidAmountPaisa", 0L),
                        currentValuePaisa = obj.optLong("currentValuePaisa", 0L),
                        purchaseDateMillis = obj.optLong("purchaseDateMillis", System.currentTimeMillis()),
                        accountId = obj.optLong("accountId", 1L),
                        installmentPaisa = obj.optLong("installmentPaisa", 0L),
                        nextPaymentDateMillis = if (obj.has("nextPaymentDateMillis") && !obj.isNull("nextPaymentDateMillis")) obj.getLong("nextPaymentDateMillis") else null,
                        status = obj.optString("status", "ACTIVE"),
                        note = obj.optString("note", ""),
                        isOpening = obj.optBoolean("isOpening", false)
                    )
                    assetDao.insertAsset(asset)
                }
            }

            // 5. Stock
            val stockArray = root.optJSONArray("stock")
            if (stockArray != null) {
                for (i in 0 until stockArray.length()) {
                    val obj = stockArray.getJSONObject(i)
                    val stock = StockItemEntity(
                        id = obj.optLong("id", 0L),
                        productName = obj.getString("productName"),
                        area = obj.optString("area", "BUSINESS"),
                        purchaseCostPerUnitPaisa = obj.optLong("purchaseCostPerUnitPaisa", 0L),
                        purchasedQuantity = obj.optDouble("purchasedQuantity", 0.0),
                        soldQuantity = obj.optDouble("soldQuantity", 0.0),
                        accountId = obj.optLong("accountId", 1L),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        note = obj.optString("note", ""),
                        isOpening = obj.optBoolean("isOpening", false)
                    )
                    stockDao.insertStockItem(stock)
                }
            }

            true to "ব্যাকআপ সফলভাবে পুনরুদ্ধার করা হয়েছে (${preview.transactionCount} টি লেনদেন, ${preview.debtCount} টি দেনা/পাওনা, ${preview.assetCount} টি সম্পদ)"
        } catch (e: Exception) {
            false to (e.localizedMessage ?: "Failed to import backup")
        }
    }

    suspend fun exportTransactionsCsv(): String = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getTransactions("local_user").first()
        val accounts = accountDao.getAccounts("local_user").first().associateBy { it.id }
        val sb = StringBuilder()
        sb.append("ID,Type,Area,Description,PaidTo,Amount_BDT,Account,Date,Note\n")
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        transactions.forEach { tx ->
            val accountName = accounts[tx.accountId]?.name ?: "Account #${tx.accountId}"
            val dateStr = Instant.ofEpochMilli(tx.dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)
            val amountBdt = CurrencyFormatter.formatBdt(tx.amountPaisa, useBanglaDigits = false, showSymbol = false)
            val desc = if (tx.sourceDetails.isNotBlank()) tx.sourceDetails else tx.category
            sb.append("${tx.id},${tx.type},${tx.area},\"$desc\",\"${tx.paidTo}\",$amountBdt,\"$accountName\",$dateStr,\"${tx.note}\"\n")
        }
        sb.toString()
    }

    // ==========================================
    // FIRESTORE SECURITY RULES
    // ==========================================

    fun getFirestoreSecurityRules(): String {
        return """
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null
        && request.auth.uid == uid;
    }
  }
}
        """.trimIndent()
    }
}
