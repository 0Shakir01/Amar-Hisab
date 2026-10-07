package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.AppNavTab
import com.example.data.model.DebtType
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddAssetDialog
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddIncomeDialog
import com.example.ui.components.AddStockDialog
import com.example.ui.components.CloudSyncMigrationDialog
import com.example.ui.components.DebtRepaymentDialog
import com.example.ui.components.FirebaseAuthDialog
import com.example.ui.components.ImportBackupPreviewDialog
import com.example.ui.components.MoveToTrashConfirmDialog
import com.example.ui.components.OpeningBalanceSetupDialog
import com.example.ui.components.OwnerCapitalDialog
import com.example.ui.components.OwnerWithdrawalDialog
import com.example.ui.components.PayAssetInstallmentDialog
import com.example.ui.components.RecordStockSaleDialog
import com.example.ui.components.TransferMoneyDialog
import com.example.ui.components.TwoStepPermanentDeleteDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val currentTab by viewModel.currentTab.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()

    // Modals & Dialog states
    val showAddIncome by viewModel.showAddIncomeDialog.collectAsState()
    val showAddExpense by viewModel.showAddExpenseDialog.collectAsState()
    val showAddDebt by viewModel.showAddDebtDialog.collectAsState()
    val showAddOldDebt by viewModel.showAddOldDebtDialog.collectAsState()
    val showAddNewLending by viewModel.showAddNewLendingDialog.collectAsState()
    val showAddOldPayable by viewModel.showAddOldPayableDialog.collectAsState()
    val showAddNewBorrowing by viewModel.showAddNewBorrowingDialog.collectAsState()
    val showAddAsset by viewModel.showAddAssetDialog.collectAsState()
    val showAddStock by viewModel.showAddStockDialog.collectAsState()
    val showTransfer by viewModel.showTransferDialog.collectAsState()
    val showOwnerCapital by viewModel.showOwnerCapitalDialog.collectAsState()
    val showOwnerWithdrawal by viewModel.showOwnerWithdrawalDialog.collectAsState()
    val showOpeningBalanceSetup by viewModel.showOpeningBalanceSetup.collectAsState()
    val selectedDebtForRepayment by viewModel.selectedDebtForRepayment.collectAsState()
    val selectedAssetForInstallment by viewModel.selectedAssetForInstallment.collectAsState()
    val selectedStockForSale by viewModel.selectedStockForSale.collectAsState()
    val itemToMoveToTrash by viewModel.itemToMoveToTrash.collectAsState()
    val permanentDeleteStep by viewModel.permanentDeleteStep.collectAsState()
    val showEmptyTrashStep by viewModel.showEmptyTrashConfirmation.collectAsState()
    val importPreview by viewModel.importPreview.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val migrationSummary by viewModel.migrationSummary.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val testResult by viewModel.firebaseTestResult.collectAsState()

    // Trash badge count
    val deletedTx by viewModel.deletedTransactions.collectAsState()
    val deletedDebts by viewModel.deletedDebts.collectAsState()
    val deletedInv by viewModel.deletedInvestments.collectAsState()
    val deletedAcc by viewModel.deletedAccounts.collectAsState()
    val totalTrashCount = deletedTx.size + deletedDebts.size + deletedInv.size + deletedAcc.size

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.feedbackMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // BackHandler: If on a sub-screen, return to Dashboard
    BackHandler(enabled = currentTab != AppNavTab.DASHBOARD) {
        viewModel.selectTab(AppNavTab.DASHBOARD)
    }

    var showMoreMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Tablet / Desktop Navigation Rail Layout
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.DASHBOARD,
                        onClick = { viewModel.selectTab(AppNavTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text(if (isBangla) "ড্যাশবোর্ড" else "Dashboard") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.LEDGER,
                        onClick = { viewModel.selectTab(AppNavTab.LEDGER) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text(if (isBangla) "লেজার" else "Ledger") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.DEBTS,
                        onClick = { viewModel.selectTab(AppNavTab.DEBTS) },
                        icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        label = { Text(if (isBangla) "দেনা-পাওনা" else "Debts") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.ASSETS_STOCK,
                        onClick = { viewModel.selectTab(AppNavTab.ASSETS_STOCK) },
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                        label = { Text(if (isBangla) "সম্পদ ও স্টক" else "Assets/Stock") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.ACCOUNTS,
                        onClick = { viewModel.selectTab(AppNavTab.ACCOUNTS) },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        label = { Text(if (isBangla) "অ্যাকাউন্ট" else "Accounts") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.REPORTS,
                        onClick = { viewModel.selectTab(AppNavTab.REPORTS) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                        label = { Text(if (isBangla) "রিপোর্ট" else "Reports") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.TRASH,
                        onClick = { viewModel.selectTab(AppNavTab.TRASH) },
                        icon = {
                            if (totalTrashCount > 0) {
                                BadgedBox(badge = { Badge { Text(totalTrashCount.toString()) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            } else {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        },
                        label = { Text(if (isBangla) "ট্র্যাশ" else "Trash") }
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(if (isBangla) "সেটিংস" else "Settings") }
                    )
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.weight(1f)
                ) { innerPadding ->
                    ScreenContent(
                        tab = currentTab,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            // Mobile Bottom Navigation Layout
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.DASHBOARD,
                            onClick = { viewModel.selectTab(AppNavTab.DASHBOARD) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text(if (isBangla) "হোম" else "Home") }
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.LEDGER,
                            onClick = { viewModel.selectTab(AppNavTab.LEDGER) },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                            label = { Text(if (isBangla) "লেজার" else "Ledger") }
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.DEBTS,
                            onClick = { viewModel.selectTab(AppNavTab.DEBTS) },
                            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            label = { Text(if (isBangla) "দেনা" else "Debt") }
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.ASSETS_STOCK,
                            onClick = { viewModel.selectTab(AppNavTab.ASSETS_STOCK) },
                            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                            label = { Text(if (isBangla) "সম্পদ/স্টক" else "Assets") }
                        )
                        NavigationBarItem(
                            selected = currentTab in listOf(AppNavTab.ACCOUNTS, AppNavTab.REPORTS, AppNavTab.TRASH, AppNavTab.SETTINGS, AppNavTab.OPENING_BALANCE, AppNavTab.MORE),
                            onClick = { showMoreMenu = true },
                            icon = {
                                Box {
                                    if (totalTrashCount > 0) {
                                        BadgedBox(badge = { Badge { Text(totalTrashCount.toString()) } }) {
                                            Icon(Icons.Default.MoreHoriz, contentDescription = null)
                                        }
                                    } else {
                                        Icon(Icons.Default.MoreHoriz, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(if (isBangla) "অ্যাকাউন্ট ও ব্যালেন্স" else "Accounts & Balances") },
                                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                            onClick = {
                                                viewModel.selectTab(AppNavTab.ACCOUNTS)
                                                showMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (isBangla) "রিপোর্ট ও বিশ্লেষণ" else "Financial Reports") },
                                            leadingIcon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                                            onClick = {
                                                viewModel.selectTab(AppNavTab.REPORTS)
                                                showMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${if (isBangla) "রিসাইকেল বিন / ট্র্যাশ" else "Trash"} ($totalTrashCount)") },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                            onClick = {
                                                viewModel.selectTab(AppNavTab.TRASH)
                                                showMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (isBangla) "সেটিংস ও ব্যাকআপ" else "Settings & Backup") },
                                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                            onClick = {
                                                viewModel.selectTab(AppNavTab.SETTINGS)
                                                showMoreMenu = false
                                            }
                                        )
                                    }
                                }
                            },
                            label = { Text(if (isBangla) "মেন্যু" else "More") }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                ScreenContent(
                    tab = currentTab,
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // ==========================================
    // ACTION DIALOGS
    // ==========================================

    if (showAddIncome) {
        AddIncomeDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showAddIncomeDialog.value = false },
            onSave = { amount, date, accountId, incomeDetails, area, note ->
                viewModel.addIncome(amount, date, accountId, incomeDetails, area, "Income", note)
            }
        )
    }

    if (showAddExpense) {
        AddExpenseDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showAddExpenseDialog.value = false },
            onSave = { amount, date, accountId, expenseDetails, area, paidTo, note ->
                viewModel.addExpense(amount, date, accountId, expenseDetails, area, paidTo, "Expense", note)
            }
        )
    }

    if (showAddDebt) {
        AddDebtDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showAddDebtDialog.value = false },
            onSave = { person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate ->
                viewModel.addDebt(person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate)
            }
        )
    }

    if (showAddOldDebt) {
        AddDebtDialog(
            accounts = accounts,
            isBangla = isBangla,
            initialDebtType = DebtType.OTHERS_OWE_ME,
            initialIsOpening = true,
            onDismiss = { viewModel.showAddOldDebtDialog.value = false },
            onSave = { person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate ->
                viewModel.addDebt(person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate)
            }
        )
    }

    if (showAddNewLending) {
        AddDebtDialog(
            accounts = accounts,
            isBangla = isBangla,
            initialDebtType = DebtType.OTHERS_OWE_ME,
            initialIsOpening = false,
            onDismiss = { viewModel.showAddNewLendingDialog.value = false },
            onSave = { person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate ->
                viewModel.addDebt(person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate)
            }
        )
    }

    if (showAddOldPayable) {
        AddDebtDialog(
            accounts = accounts,
            isBangla = isBangla,
            initialDebtType = DebtType.I_OWE_OTHERS,
            initialIsOpening = true,
            onDismiss = { viewModel.showAddOldPayableDialog.value = false },
            onSave = { person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate ->
                viewModel.addDebt(person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate)
            }
        )
    }

    if (showAddNewBorrowing) {
        AddDebtDialog(
            accounts = accounts,
            isBangla = isBangla,
            initialDebtType = DebtType.I_OWE_OTHERS,
            initialIsOpening = false,
            onDismiss = { viewModel.showAddNewBorrowingDialog.value = false },
            onSave = { person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate ->
                viewModel.addDebt(person, type, amount, date, area, phone, dueDate, accountId, note, isOpening, originalDate)
            }
        )
    }

    if (showAddAsset) {
        AddAssetDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showAddAssetDialog.value = false },
            onSave = { name, assetType, area, totalPurchaseValuePaisa, paidAmountPaisa, currentValuePaisa, purchaseDateMillis, accountId, installmentPaisa, nextPaymentDateMillis, note, isOpening ->
                viewModel.addAsset(name, assetType, area, totalPurchaseValuePaisa, paidAmountPaisa, currentValuePaisa, purchaseDateMillis, accountId, installmentPaisa, nextPaymentDateMillis, note, isOpening)
            }
        )
    }

    if (showAddStock) {
        AddStockDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showAddStockDialog.value = false },
            onSave = { productName, area, purchaseCostPerUnitPaisa, purchasedQuantity, accountId, note, isOpening ->
                viewModel.addStock(productName, area, purchaseCostPerUnitPaisa, purchasedQuantity, accountId, note, isOpening)
            }
        )
    }

    if (showOwnerCapital) {
        OwnerCapitalDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showOwnerCapitalDialog.value = false },
            onSave = { amountPaisa, businessAccountId, note ->
                viewModel.addOwnerCapital(amountPaisa, businessAccountId, System.currentTimeMillis(), note)
            }
        )
    }

    if (showOwnerWithdrawal) {
        OwnerWithdrawalDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showOwnerWithdrawalDialog.value = false },
            onSave = { amountPaisa, businessAccountId, note ->
                viewModel.addOwnerWithdrawal(amountPaisa, businessAccountId, System.currentTimeMillis(), note)
            }
        )
    }

    if (showTransfer) {
        TransferMoneyDialog(
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.showTransferDialog.value = false },
            onSave = { from, to, amount, date, area, note ->
                viewModel.transferMoney(from, to, amount, date, area, note)
            }
        )
    }

    if (showOpeningBalanceSetup) {
        OpeningBalanceSetupDialog(
            isBangla = isBangla,
            onDismiss = { viewModel.showOpeningBalanceSetup.value = false },
            onSave = { area, cash, bank, date, note ->
                viewModel.setOpeningBalances(area, cash, bank, date, note)
            }
        )
    }

    selectedDebtForRepayment?.let { debt ->
        DebtRepaymentDialog(
            debt = debt,
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.selectedDebtForRepayment.value = null },
            onSave = { amount, accountId, note ->
                viewModel.recordDebtRepayment(debt.id, amount, accountId, note)
            }
        )
    }

    selectedAssetForInstallment?.let { asset ->
        PayAssetInstallmentDialog(
            asset = asset,
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.selectedAssetForInstallment.value = null },
            onSave = { amount, accountId, note ->
                viewModel.payAssetInstallment(asset.id, amount, accountId, System.currentTimeMillis(), null, note)
            }
        )
    }

    selectedStockForSale?.let { stock ->
        RecordStockSaleDialog(
            stock = stock,
            accounts = accounts,
            isBangla = isBangla,
            onDismiss = { viewModel.selectedStockForSale.value = null },
            onSave = { soldQty, salePrice, accountId, note ->
                viewModel.recordStockSale(stock.id, soldQty, salePrice, accountId, System.currentTimeMillis(), note)
            }
        )
    }

    itemToMoveToTrash?.let { item ->
        MoveToTrashConfirmDialog(
            item = item,
            isBangla = isBangla,
            onDismiss = { viewModel.itemToMoveToTrash.value = null },
            onConfirm = { viewModel.confirmMoveToTrash() }
        )
    }

    permanentDeleteStep?.let { (item, step) ->
        TwoStepPermanentDeleteDialog(
            item = item,
            step = step,
            isBangla = isBangla,
            onDismiss = { viewModel.cancelPermanentDelete() },
            onConfirmStep1 = { viewModel.confirmPermanentDeleteStep1() },
            onConfirmStep2 = { viewModel.confirmPermanentDeleteStep2() }
        )
    }

    showEmptyTrashStep?.let { step ->
        TwoStepPermanentDeleteDialog(
            item = "empty_trash",
            step = step,
            isBangla = isBangla,
            onDismiss = { viewModel.cancelEmptyTrash() },
            onConfirmStep1 = { viewModel.confirmEmptyTrashStep1() },
            onConfirmStep2 = { viewModel.confirmEmptyTrashStep2() }
        )
    }

    importPreview?.let { preview ->
        ImportBackupPreviewDialog(
            preview = preview,
            isBangla = isBangla,
            onDismiss = { viewModel.cancelRestoreBackup() },
            onConfirm = { viewModel.confirmRestoreBackup() }
        )
    }

    if (showAuthDialog) {
        FirebaseAuthDialog(
            currentUser = currentUser,
            testResult = testResult,
            isBangla = isBangla,
            onDismiss = { viewModel.showAuthDialog.value = false },
            onGoogleSignIn = {
                viewModel.showAuthDialog.value = false
                viewModel.signInWithGoogle()
            },
            onEmailSignIn = { email, pass ->
                viewModel.showAuthDialog.value = false
                viewModel.signInWithEmail(email, pass)
            },
            onSignOut = {
                viewModel.signOut()
            },
            onTestConnection = {
                viewModel.runRealFirebaseTest()
            }
        )
    }

    migrationSummary?.let { summary ->
        val uid = viewModel.getCurrentUserId()
        CloudSyncMigrationDialog(
            summary = summary,
            uid = uid,
            isBangla = isBangla,
            onUploadLocal = { viewModel.executeUploadLocal(uid) },
            onDownloadCloud = { viewModel.executeDownloadCloud(uid) },
            onMerge = { viewModel.executeMerge(uid) },
            onCancel = { viewModel.dismissMigration() }
        )
    }
}

@Composable
private fun ScreenContent(
    tab: AppNavTab,
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (tab) {
            AppNavTab.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToTab = { viewModel.selectTab(it) }
            )
            AppNavTab.LEDGER -> TransactionsScreen(viewModel = viewModel)
            AppNavTab.DEBTS -> DebtsScreen(viewModel = viewModel)
            AppNavTab.ASSETS_STOCK -> AssetsAndStockScreen(viewModel = viewModel)
            AppNavTab.ACCOUNTS, AppNavTab.OPENING_BALANCE, AppNavTab.MORE -> AccountsScreen(viewModel = viewModel)
            AppNavTab.REPORTS -> ReportsScreen(viewModel = viewModel)
            AppNavTab.TRASH -> TrashScreen(viewModel = viewModel)
            AppNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}
