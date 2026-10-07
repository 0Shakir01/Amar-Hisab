package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AmarHisabDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.InstallmentEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.AppLanguage
import com.example.data.model.AppNavTab
import com.example.data.model.DashboardMetrics
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinanceArea
import com.example.data.model.IncomeCategory
import com.example.data.model.InvestmentType
import com.example.data.model.TransactionType
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinanceRepository
import com.example.data.repository.ImportPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class DateRangeFilter(val labelBn: String, val labelEn: String) {
    ALL("সব সময়", "All Time"),
    THIS_MONTH("এই মাস", "This Month"),
    LAST_MONTH("গত মাস", "Last Month"),
    THIS_YEAR("এই বছর", "This Year")
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AmarHisabDatabase.getInstance(application)
    val repository = FinanceRepository(database, application)

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.BANGLA)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Navigation tab
    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Area filter for Dashboard and Ledger: ALL, PERSONAL, BUSINESS
    val selectedAreaFilter = MutableStateFlow(FinanceArea.ALL)

    // Toast / Feedback message
    private val _feedbackMessage = MutableSharedFlow<String>()
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    // Primary Data Streams
    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = repository.getAccountsWithBalances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardMetrics: StateFlow<DashboardMetrics> = repository.getDashboardMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.getDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<AssetEntity>> = repository.getAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockItems: StateFlow<List<StockItemEntity>> = repository.getStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installments: StateFlow<List<InstallmentEntity>> = repository.getInstallments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investments: StateFlow<List<InvestmentEntity>> = repository.getInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trash Streams
    val deletedTransactions: StateFlow<List<TransactionEntity>> = repository.getDeletedTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedDebts: StateFlow<List<DebtEntity>> = repository.getDeletedDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedAssets: StateFlow<List<AssetEntity>> = repository.getDeletedAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedStockItems: StateFlow<List<StockItemEntity>> = repository.getDeletedStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedAccounts: StateFlow<List<AccountEntity>> = repository.getDeletedAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedInvestments: StateFlow<List<InvestmentEntity>> = repository.getDeletedInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters for Ledger & Reports
    val selectedDateRange = MutableStateFlow(DateRangeFilter.ALL)
    val selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedLedgerType = MutableStateFlow<String?>(null) // ALL, INCOME, EXPENSE, DEBT, ASSET, STOCK, TRANSFER, CAPITAL, WITHDRAWAL
    val selectedDebtStatus = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    // Auth & Sync State
    val currentUser = repository.authService.currentUser
    val syncState = repository.syncManager.syncState
    val lastSyncTimestamp = repository.syncManager.lastSyncTimestamp
    val showAuthDialog = MutableStateFlow(false)
    val migrationSummary = MutableStateFlow<com.example.data.repository.MigrationSummary?>(null)
    val firebaseTestResult = MutableStateFlow<com.example.data.firebase.FirebaseTestResult?>(null)

    // Action dialog states
    val showAddIncomeDialog = MutableStateFlow(false)
    val showAddExpenseDialog = MutableStateFlow(false)
    val showAddDebtDialog = MutableStateFlow(false)
    val showAddAssetDialog = MutableStateFlow(false)
    val showAddStockDialog = MutableStateFlow(false)
    val showStockSaleDialog = MutableStateFlow(false)
    val showTransferDialog = MutableStateFlow(false)
    val showOwnerCapitalDialog = MutableStateFlow(false)
    val showOwnerWithdrawalDialog = MutableStateFlow(false)
    val showOpeningBalanceSetup = MutableStateFlow(false)
    val showAddAccountDialog = MutableStateFlow(false)
    val showAddOldDebtDialog = MutableStateFlow(false)
    val showAddNewLendingDialog = MutableStateFlow(false)
    val showAddOldPayableDialog = MutableStateFlow(false)
    val showAddNewBorrowingDialog = MutableStateFlow(false)
    val selectedDebtTab = MutableStateFlow(DebtType.OTHERS_OWE_ME)

    // Modals for repayments / asset installment / stock sale
    val selectedDebtForRepayment = MutableStateFlow<DebtEntity?>(null)
    val selectedAssetForInstallment = MutableStateFlow<AssetEntity?>(null)
    val selectedStockForSale = MutableStateFlow<StockItemEntity?>(null)
    val selectedInvestmentForPayment = MutableStateFlow<InvestmentEntity?>(null)

    // Deletion confirmation dialogs
    val itemToMoveToTrash = MutableStateFlow<Any?>(null)
    val permanentDeleteStep = MutableStateFlow<Pair<Any, Int>?>(null) // Pair of item and step (1 or 2)
    val showEmptyTrashConfirmation = MutableStateFlow<Int?>(null) // step 1 or 2

    // Backup & Import
    val importPreview = MutableStateFlow<ImportPreview?>(null)
    val rawImportJson = MutableStateFlow<String?>(null)

    // ==========================================
    // FILTERED LEDGER STREAM
    // ==========================================
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        selectedAreaFilter,
        selectedDateRange,
        selectedAccountId,
        combine(selectedLedgerType, searchQuery) { typeFilter, query -> Pair(typeFilter, query) }
    ) { txList, areaFilter, dateRange, accId, (typeFilter, query) ->
        val zoneId = ZoneId.of("Asia/Dhaka")
        val now = LocalDate.now(zoneId)

        val (startMillis, endMillis) = when (dateRange) {
            DateRangeFilter.ALL -> Pair(0L, Long.MAX_VALUE)
            DateRangeFilter.THIS_MONTH -> {
                val start = now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateRangeFilter.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateRangeFilter.THIS_YEAR -> {
                val start = now.withDayOfYear(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.plusYears(1).withDayOfYear(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
        }

        txList.filter { tx ->
            val matchDate = tx.dateMillis in startMillis until endMillis
            val matchAccount = accId == null || tx.accountId == accId || tx.destinationAccountId == accId
            val matchArea = areaFilter == FinanceArea.ALL || tx.area.equals(areaFilter.key, ignoreCase = true)
            val matchType = typeFilter == null || typeFilter == "ALL" || tx.type.equals(typeFilter, ignoreCase = true)

            val matchQuery = query.isBlank() ||
                tx.sourceDetails.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                tx.paidTo.contains(query, ignoreCase = true) ||
                tx.note.contains(query, ignoreCase = true)

            matchDate && matchAccount && matchArea && matchType && matchQuery && !tx.isDeleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDebts: StateFlow<List<DebtEntity>> = combine(
        debts,
        selectedAreaFilter,
        selectedDebtStatus,
        searchQuery
    ) { debtList, areaFilter, status, query ->
        debtList.filter { debt ->
            val matchArea = areaFilter == FinanceArea.ALL || debt.area.equals(areaFilter.key, ignoreCase = true)
            val matchStatus = status == null || debt.status.equals(status, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                debt.personName.contains(query, ignoreCase = true) ||
                debt.phone.contains(query, ignoreCase = true) ||
                debt.note.contains(query, ignoreCase = true)
            matchArea && matchStatus && matchQuery && !debt.isDeleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAssets: StateFlow<List<AssetEntity>> = combine(
        assets,
        selectedAreaFilter,
        searchQuery
    ) { assetList, areaFilter, query ->
        assetList.filter { asset ->
            val matchArea = areaFilter == FinanceArea.ALL || asset.area.equals(areaFilter.key, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                asset.name.contains(query, ignoreCase = true) ||
                asset.assetType.contains(query, ignoreCase = true) ||
                asset.note.contains(query, ignoreCase = true)
            matchArea && matchQuery && !asset.isDeleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStock: StateFlow<List<StockItemEntity>> = combine(
        stockItems,
        searchQuery
    ) { stockList, query ->
        stockList.filter { item ->
            val matchQuery = query.isBlank() ||
                item.productName.contains(query, ignoreCase = true) ||
                item.note.contains(query, ignoreCase = true)
            matchQuery && !item.isDeleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // UI NAVIGATION & CONTROLS
    // ==========================================

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
    }

    fun setAreaFilter(area: FinanceArea) {
        selectedAreaFilter.value = area
    }

    // ==========================================
    // OPERATIONS & CRUD
    // ==========================================

    fun setOpeningBalances(area: String, cashPaisa: Long, bankPaisa: Long, dateMillis: Long, note: String) {
        viewModelScope.launch {
            repository.setOpeningBalances(area, cashPaisa, bankPaisa, dateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA)
                    "প্রারম্ভিক ব্যালেন্স সফলভাবে আপডেট করা হয়েছে"
                else
                    "Opening balance updated successfully"
            )
            showOpeningBalanceSetup.value = false
        }
    }

    fun addIncome(
        amountPaisa: Long,
        dateMillis: Long,
        accountId: Long,
        incomeDetails: String,
        area: String = "PERSONAL",
        category: String = "Income",
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addIncome(amountPaisa, dateMillis, accountId, incomeDetails, area, category, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "আয় রেকর্ড সম্পন্ন হয়েছে" else "Income added successfully"
            )
            showAddIncomeDialog.value = false
        }
    }

    fun addExpense(
        amountPaisa: Long,
        dateMillis: Long,
        accountId: Long,
        expenseDetails: String,
        area: String = "PERSONAL",
        paidTo: String = "",
        category: String = "Expense",
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addExpense(amountPaisa, dateMillis, accountId, expenseDetails, area, paidTo, category, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "ব্যয় রেকর্ড সম্পন্ন হয়েছে" else "Expense added successfully"
            )
            showAddExpenseDialog.value = false
        }
    }

    fun transferMoney(
        sourceAccountId: Long,
        destinationAccountId: Long,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.transferMoney(sourceAccountId, destinationAccountId, amountPaisa, dateMillis, area, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "তহবিল স্থানান্তর সম্পন্ন হয়েছে" else "Transfer completed successfully"
            )
            showTransferDialog.value = false
        }
    }

    fun addOwnerCapital(amountPaisa: Long, businessAccountId: Long, dateMillis: Long, note: String = "") {
        viewModelScope.launch {
            repository.addOwnerCapital(amountPaisa, businessAccountId, dateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "মালিকের মূলধন যোগ করা হয়েছে" else "Owner Capital added"
            )
            showOwnerCapitalDialog.value = false
        }
    }

    fun addOwnerWithdrawal(amountPaisa: Long, businessAccountId: Long, dateMillis: Long, note: String = "") {
        viewModelScope.launch {
            repository.addOwnerWithdrawal(amountPaisa, businessAccountId, dateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "মালিকের ব্যক্তিগত উত্তোলন রেকর্ড করা হয়েছে" else "Owner Withdrawal recorded"
            )
            showOwnerWithdrawalDialog.value = false
        }
    }

    fun addDebt(
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
    ) {
        viewModelScope.launch {
            repository.addDebt(personName, debtType, amountPaisa, dateMillis, area, phone, dueDateMillis, accountId, note, isOpening, originalDateMillis)
            selectedDebtTab.value = debtType
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "দেনা/পাওনা সফলভাবে রেকর্ড করা হয়েছে" else "Debt entry added"
            )
            showAddDebtDialog.value = false
            showAddOldDebtDialog.value = false
            showAddNewLendingDialog.value = false
            showAddOldPayableDialog.value = false
            showAddNewBorrowingDialog.value = false
        }
    }

    fun addOldDebt(
        personName: String,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        note: String = "",
        originalDateMillis: Long? = null,
        dueDateMillis: Long? = null
    ) {
        addDebt(
            personName = personName,
            debtType = DebtType.OTHERS_OWE_ME,
            amountPaisa = amountPaisa,
            dateMillis = dateMillis,
            area = area,
            dueDateMillis = dueDateMillis,
            accountId = 1L,
            note = note,
            isOpening = true,
            originalDateMillis = originalDateMillis
        )
    }

    fun addNewLending(
        personName: String,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        accountId: Long,
        phone: String = "",
        note: String = "",
        dueDateMillis: Long? = null
    ) {
        addDebt(
            personName = personName,
            debtType = DebtType.OTHERS_OWE_ME,
            amountPaisa = amountPaisa,
            dateMillis = dateMillis,
            area = area,
            phone = phone,
            dueDateMillis = dueDateMillis,
            accountId = accountId,
            note = note,
            isOpening = false
        )
    }

    fun addOldPayable(
        personName: String,
        amountPaisa: Long,
        dateMillis: Long,
        area: String = "PERSONAL",
        note: String = "",
        originalDateMillis: Long? = null,
        dueDateMillis: Long? = null
    ) {
        addDebt(
            personName = personName,
            debtType = DebtType.I_OWE_OTHERS,
            amountPaisa = amountPaisa,
            dateMillis = dateMillis,
            area = area,
            dueDateMillis = dueDateMillis,
            accountId = 1L,
            note = note,
            isOpening = true,
            originalDateMillis = originalDateMillis
        )
    }

    fun updateAccount(accountId: Long, name: String, openingBalancePaisa: Long) {
        viewModelScope.launch {
            repository.updateAccount(accountId, name, openingBalancePaisa)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "অ্যাকাউন্ট সফলভাবে সংরক্ষিত হয়েছে" else "Account updated"
            )
        }
    }

    fun setOpeningBalancesPersonal(
        cashAmountPaisa: Long,
        bank1AmountPaisa: Long,
        bank2AmountPaisa: Long,
        dateMillis: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.setOpeningBalancesPersonal(cashAmountPaisa, bank1AmountPaisa, bank2AmountPaisa, dateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "প্রারম্ভিক তহবিল সংরক্ষিত হয়েছে" else "Opening balances saved"
            )
            showOpeningBalanceSetup.value = false
        }
    }

    fun updateAccountsStartDate(startDateMillis: Long) {
        viewModelScope.launch {
            repository.updateAccountsStartDate(startDateMillis)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "হিসাব শুরুর তারিখ সংরক্ষিত হয়েছে" else "Accounts start date saved"
            )
        }
    }

    fun recordDebtRepayment(debtId: Long, repaymentAmountPaisa: Long, accountId: Long? = null, note: String = "") {
        viewModelScope.launch {
            repository.recordDebtRepayment(debtId, repaymentAmountPaisa, accountId, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "পরিশোধ রেকর্ড করা হয়েছে" else "Repayment recorded"
            )
            selectedDebtForRepayment.value = null
        }
    }

    fun addAsset(
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
    ) {
        viewModelScope.launch {
            repository.addAsset(name, assetType, area, totalPurchaseValuePaisa, paidAmountPaisa, currentValuePaisa, purchaseDateMillis, accountId, installmentPaisa, nextPaymentDateMillis, note, isOpening)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "সম্পদ সফলভাবে যোগ করা হয়েছে" else "Asset added successfully"
            )
            showAddAssetDialog.value = false
        }
    }

    fun payAssetInstallment(
        assetId: Long,
        amountPaisa: Long,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        nextDueDateMillis: Long? = null,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.payAssetInstallment(assetId, amountPaisa, accountId, dateMillis, nextDueDateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "কিস্তি পরিশোধ সফল হয়েছে" else "Installment paid successfully"
            )
            selectedAssetForInstallment.value = null
        }
    }

    fun addOrPurchaseStock(
        productName: String,
        area: String = "BUSINESS",
        purchaseCostPerUnitPaisa: Long,
        purchasedQuantity: Double,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = "",
        isOpening: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addOrPurchaseStock(productName, area, purchaseCostPerUnitPaisa, purchasedQuantity, accountId, dateMillis, note, isOpening)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "ব্যবসায়িক স্টক যোগ করা হয়েছে" else "Stock purchased successfully"
            )
            showAddStockDialog.value = false
        }
    }

    fun addStock(
        productName: String,
        area: String = "BUSINESS",
        purchaseCostPerUnitPaisa: Long,
        purchasedQuantity: Double,
        accountId: Long,
        note: String = "",
        isOpening: Boolean = false
    ) {
        addOrPurchaseStock(productName, area, purchaseCostPerUnitPaisa, purchasedQuantity, accountId, System.currentTimeMillis(), note, isOpening)
    }

    fun recordStockSale(
        stockId: Long,
        soldQuantity: Double,
        salePricePerUnitPaisa: Long,
        accountId: Long,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.recordStockSale(stockId, soldQuantity, salePricePerUnitPaisa, accountId, dateMillis, note)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "স্টক বিক্রয় রেকর্ড সম্পন্ন হয়েছে" else "Stock sale recorded successfully"
            )
            selectedStockForSale.value = null
        }
    }

    // ==========================================
    // SOFT DELETE & TRASH MANAGEMENT
    // ==========================================

    fun requestMoveToTrash(item: Any) {
        itemToMoveToTrash.value = item
    }

    fun confirmMoveToTrash() {
        val item = itemToMoveToTrash.value ?: return
        viewModelScope.launch {
            when (item) {
                is TransactionEntity -> repository.softDeleteTransaction(item.id)
                is DebtEntity -> repository.softDeleteDebt(item.id)
                is AssetEntity -> repository.softDeleteAsset(item.id)
                is StockItemEntity -> repository.softDeleteStockItem(item.id)
                is AccountEntity -> repository.softDeleteAccount(item.id)
                is InvestmentEntity -> repository.softDeleteInvestment(item.id)
            }
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "আইটেম ট্র্যাশে পাঠানো হয়েছে" else "Item moved to trash"
            )
            itemToMoveToTrash.value = null
        }
    }

    fun restoreItem(item: Any) {
        viewModelScope.launch {
            when (item) {
                is TransactionEntity -> repository.restoreTransaction(item.id)
                is DebtEntity -> repository.restoreDebt(item.id)
                is AssetEntity -> repository.restoreAsset(item.id)
                is StockItemEntity -> repository.restoreStockItem(item.id)
                is AccountEntity -> repository.restoreAccount(item.id)
                is InvestmentEntity -> repository.restoreInvestment(item.id)
            }
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "আইটেম পুনরুদ্ধার করা হয়েছে" else "Item restored"
            )
        }
    }

    fun startPermanentDelete(item: Any) {
        permanentDeleteStep.value = item to 1
    }

    fun confirmPermanentDeleteStep1() {
        val current = permanentDeleteStep.value ?: return
        permanentDeleteStep.value = current.first to 2
    }

    fun confirmPermanentDeleteStep2() {
        val current = permanentDeleteStep.value ?: return
        viewModelScope.launch {
            when (val item = current.first) {
                is TransactionEntity -> repository.permanentlyDeleteTransaction(item.id)
                is DebtEntity -> repository.permanentlyDeleteDebt(item.id)
                is AssetEntity -> repository.permanentlyDeleteAsset(item.id)
                is StockItemEntity -> repository.permanentlyDeleteStockItem(item.id)
                is AccountEntity -> repository.permanentlyDeleteAccount(item.id)
                is InvestmentEntity -> repository.permanentlyDeleteInvestment(item.id)
            }
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "স্থায়ীভাবে মুছে ফেলা হয়েছে" else "Permanently deleted"
            )
            permanentDeleteStep.value = null
        }
    }

    fun cancelPermanentDelete() {
        permanentDeleteStep.value = null
    }

    fun startEmptyTrash() {
        showEmptyTrashConfirmation.value = 1
    }

    fun confirmEmptyTrashStep1() {
        showEmptyTrashConfirmation.value = 2
    }

    fun confirmEmptyTrashStep2() {
        viewModelScope.launch {
            repository.emptyTrash()
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "ট্র্যাশ সম্পূর্ণ খালি করা হয়েছে" else "Trash emptied"
            )
            showEmptyTrashConfirmation.value = null
        }
    }

    fun cancelEmptyTrash() {
        showEmptyTrashConfirmation.value = null
    }

    // ==========================================
    // BACKUP & RESTORE
    // ==========================================

    fun onImportJsonSelected(json: String) {
        rawImportJson.value = json
        importPreview.value = repository.validateBackupJson(json)
    }

    fun confirmRestore() {
        val json = rawImportJson.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.importBackupJson(json)
            _feedbackMessage.emit(msg)
            if (success) {
                importPreview.value = null
                rawImportJson.value = null
            }
        }
    }

    fun dismissImportPreview() {
        importPreview.value = null
        rawImportJson.value = null
    }

    fun cancelRestoreBackup() {
        dismissImportPreview()
    }

    fun confirmRestoreBackup() {
        confirmRestore()
    }

    suspend fun getExportJson(): String = repository.exportBackupJson()

    suspend fun getExportCsv(): String = repository.exportTransactionsCsv()

    fun previewBackupJson(json: String) {
        onImportJsonSelected(json)
    }

    fun advancePermanentDelete() {
        val current = permanentDeleteStep.value ?: return
        if (current.second == 1) {
            confirmPermanentDeleteStep1()
        } else {
            confirmPermanentDeleteStep2()
        }
    }

    fun advanceEmptyTrash() {
        val current = showEmptyTrashConfirmation.value ?: return
        if (current == 1) {
            confirmEmptyTrashStep1()
        } else {
            confirmEmptyTrashStep2()
        }
    }

    fun addAccount(name: String, type: String, openingBalancePaisa: Long, colorHex: String = "#10B981", area: String = "PERSONAL") {
        viewModelScope.launch {
            repository.addAccount(name, type, openingBalancePaisa, colorHex, area)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "অ্যাকাউন্ট যোগ করা হয়েছে" else "Account added"
            )
        }
    }

    fun updateAccountOpeningBalance(accountId: Long, openingBalancePaisa: Long) {
        viewModelScope.launch {
            repository.updateAccountOpeningBalance(accountId, openingBalancePaisa)
            _feedbackMessage.emit(
                if (_currentLanguage.value == AppLanguage.BANGLA) "ব্যালেন্স আপডেট করা হয়েছে" else "Balance updated"
            )
        }
    }

    // ==========================================
    // FIREBASE AUTH & REAL DIAGNOSTICS
    // ==========================================

    fun getCurrentUserId(): String = repository.getCurrentUserId()

    fun runRealFirebaseTest() {
        viewModelScope.launch {
            val result = repository.verifyRealFirestoreConnection()
            firebaseTestResult.value = result
            if (result.isSuccess) {
                _feedbackMessage.emit("✓ ${result.message}")
            } else {
                _feedbackMessage.emit("✕ ${result.message}: ${result.details ?: ""}")
            }
        }
    }

    fun signInWithGoogle(idToken: String = "") {
        if (idToken.isNotBlank()) {
            viewModelScope.launch {
                val result = repository.authService.signInWithGoogleCredential(idToken)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    _feedbackMessage.emit("স্বাগতম, ${user.displayName ?: user.email}")
                    showAuthDialog.value = false
                    checkMigrationOnSignIn(user.uid)
                } else {
                    _feedbackMessage.emit("Google সাইন-ইন ব্যর্থ হয়েছে: ${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            signInAnonymously()
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            val result = repository.authService.signInWithEmail(email, pass)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _feedbackMessage.emit("স্বাগতম, ${user.email}")
                showAuthDialog.value = false
                checkMigrationOnSignIn(user.uid)
            } else {
                _feedbackMessage.emit("সাইন-ইন ব্যর্থ হয়েছে: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            val result = repository.authService.signInAnonymously()
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _feedbackMessage.emit("সাইন-ইন সম্পন্ন হয়েছে")
                showAuthDialog.value = false
                checkMigrationOnSignIn(user.uid)
            } else {
                _feedbackMessage.emit("সাইন-ইন ব্যর্থ হয়েছে")
            }
        }
    }

    fun signOut() {
        repository.authService.signOut()
        viewModelScope.launch {
            _feedbackMessage.emit("সাইন-আউট সম্পন্ন হয়েছে")
        }
    }

    private fun checkMigrationOnSignIn(uid: String) {
        viewModelScope.launch {
            val summary = repository.checkMigrationNeeded(uid)
            if (summary != null) {
                migrationSummary.value = summary
            }
        }
    }

    fun executeUploadLocal(uid: String) {
        viewModelScope.launch {
            repository.executeUploadLocal(uid)
            _feedbackMessage.emit("লোকাল ডেটা ক্লাউডে আপলোড সম্পন্ন হয়েছে")
            migrationSummary.value = null
        }
    }

    fun executeDownloadCloud(uid: String) {
        viewModelScope.launch {
            repository.executeDownloadCloud(uid)
            _feedbackMessage.emit("ক্লাউড ডেটা ডিভাইসে ডাউনলোড সম্পন্ন হয়েছে")
            migrationSummary.value = null
        }
    }

    fun executeMerge(uid: String) {
        viewModelScope.launch {
            repository.executeMerge(uid)
            _feedbackMessage.emit("ক্লাউড ও লোকাল ডেটা মার্জ সম্পন্ন হয়েছে")
            migrationSummary.value = null
        }
    }

    fun dismissMigration() {
        migrationSummary.value = null
    }

    fun triggerSync() {
        val uid = getCurrentUserId()
        if (uid != "local_user") {
            viewModelScope.launch {
                executeUploadLocal(uid)
            }
        } else {
            showAuthDialog.value = true
        }
    }
}
