package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AppLanguage
import com.example.data.model.AppNavTab
import com.example.data.model.CurrencyFormatter
import com.example.data.model.FinanceArea
import com.example.data.model.TransactionType
import com.example.ui.FinanceViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTab: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val selectedArea by viewModel.selectedAreaFilter.collectAsState()
    val recentTransactions by viewModel.transactions.collectAsState()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsState()

    var isBalanceRevealed by remember { mutableStateOf(false) }
    var showCashBreakdownDialog by remember { mutableStateOf(false) }
    var showBankBreakdownDialog by remember { mutableStateOf(false) }

    val maskedAmount = "৳ •••••"
    fun formatOrMask(amountPaisa: Long): String {
        return if (isBalanceRevealed) CurrencyFormatter.formatBdt(amountPaisa, isBangla) else maskedAmount
    }

    // Filter recent transactions according to area toggle
    val displayTransactions = recentTransactions.filter { !it.isDeleted && (selectedArea == FinanceArea.ALL || it.area.equals(selectedArea.key, ignoreCase = true)) }.take(10)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Dashboard Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBangla) "আমার হিসাব" else "Amar Hisab",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isBangla) "ব্যক্তিগত ও ব্যবসায়িক আর্থিক নিয়ন্ত্রণ" else "Personal & Business Financial System",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val syncState by viewModel.syncState.collectAsState()
                    val lastSync by viewModel.lastSyncTimestamp.collectAsState()
                    val currentUser by viewModel.currentUser.collectAsState()

                    com.example.ui.components.SyncStatusBadge(
                        syncState = syncState,
                        lastSyncMillis = lastSync,
                        isBangla = isBangla,
                        onClick = {
                            if (currentUser != null) {
                                viewModel.triggerSync()
                            } else {
                                viewModel.showAuthDialog.value = true
                            }
                        }
                    )

                    IconButton(onClick = { viewModel.toggleLanguage() }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isBangla) "EN" else "বাং",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // AREA SELECTOR TOGGLE: ALL | PERSONAL | BUSINESS
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        FinanceArea.ALL to (if (isBangla) "সকল (উভয়)" else "All"),
                        FinanceArea.PERSONAL to (if (isBangla) "ব্যক্তিগত (Personal)" else "Personal"),
                        FinanceArea.BUSINESS to (if (isBangla) "ব্যবসা (Business)" else "Business")
                    ).forEach { (area, label) ->
                        val isSelected = selectedArea == area
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setAreaFilter(area) }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 1: AVAILABLE BALANCE (CASH & BANK)
        // ==========================================
        item {
            val balanceToShow = when (selectedArea) {
                FinanceArea.ALL -> metrics.availableBalancePaisa
                FinanceArea.PERSONAL -> metrics.personalBalancePaisa
                FinanceArea.BUSINESS -> metrics.businessBalancePaisa
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isBalanceRevealed = !isBalanceRevealed }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "মোট উপলব্ধ তহবিল (Available Balance)" else "Total Available Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        IconButton(
                            onClick = { isBalanceRevealed = !isBalanceRevealed },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isBalanceRevealed) "Hide Balance" else "Show Balance",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatOrMask(balanceToShow),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Clickable Cash Balance Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showCashBreakdownDialog = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBangla) "নগদ টাকা (Cash)" else "Cash Balance",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Cash Details",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                val cashToShow = when (selectedArea) {
                                    FinanceArea.ALL -> metrics.cashBalancePaisa
                                    FinanceArea.PERSONAL -> metrics.personalCashPaisa
                                    FinanceArea.BUSINESS -> metrics.businessCashPaisa
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatOrMask(cashToShow),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Clickable Bank Balance Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showBankBreakdownDialog = true }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBangla) "ব্যাংক ব্যালেন্স (Bank)" else "Bank Balance",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Bank Details",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                val bankToShow = when (selectedArea) {
                                    FinanceArea.ALL -> metrics.bankBalancePaisa
                                    FinanceArea.PERSONAL -> metrics.personalBankPaisa
                                    FinanceArea.BUSINESS -> metrics.businessBankPaisa
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatOrMask(bankToShow),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 2: TOTAL ASSETS, STOCK, RECEIVABLE, PAYABLE & NET WORTH
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "মোট সম্পদ (Total Assets)" else "Total Assets",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatOrMask(metrics.totalAssetsPaisa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "ব্যবসায়িক স্টক মূল্য (Stock Value)" else "Business Stock Value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatOrMask(metrics.businessStockValuePaisa),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "মোট পাওনা (Others Owe Me)" else "Total Receivable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatOrMask(metrics.othersOweMePaisa),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "মোট দেনা (I Owe Others)" else "Total Payable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatOrMask(metrics.iOweOthersPaisa),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "প্রকৃত নিট সম্পদ (Net Worth)" else "Net Worth",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "সম্পদ বিয়োগ দেনা/দায়" else "Assets - Liabilities",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatOrMask(metrics.netWorthPaisa),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.netWorthPaisa >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // ==========================================
        // CARD 3: THIS MONTH INCOME, EXPENSE & OPERATING SURPLUS
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "চলতি মাসের হিসাব (This Month)" else "This Month's Cash Flow",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Income
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "স্বীকৃত আয়" else "Income", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = formatOrMask(metrics.thisMonthIncomePaisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        // Expense
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "স্বীকৃত ব্যয়" else "Expense", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = formatOrMask(metrics.thisMonthExpensePaisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }

                        // Surplus
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBangla) "উদ্বৃত্ত / ঘাটতি" else "Surplus",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formatOrMask(metrics.monthlySurplusPaisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.monthlySurplusPaisa >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // QUICK ACTIONS GRID
        // ==========================================
        item {
            Text(
                text = if (isBangla) "দ্রুত কাজ (Quick Actions)" else "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Money In
                    QuickActionButton(
                        icon = Icons.Default.CallReceived,
                        label = if (isBangla) "টাকা জমা (In)" else "Money In",
                        containerColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showAddIncomeDialog.value = true }
                    )
                    // Money Out
                    QuickActionButton(
                        icon = Icons.Default.CallMade,
                        label = if (isBangla) "টাকা প্রদান (Out)" else "Money Out",
                        containerColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showAddExpenseDialog.value = true }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Debt / Pawna
                    QuickActionButton(
                        icon = Icons.Default.MonetizationOn,
                        label = if (isBangla) "দেনা-পাওনা" else "Debt / Loan",
                        containerColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showAddDebtDialog.value = true }
                    )
                    // Asset / Installment
                    QuickActionButton(
                        icon = Icons.Default.Savings,
                        label = if (isBangla) "সম্পদ / কিস্তি" else "Asset / Installment",
                        containerColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showAddAssetDialog.value = true }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Business Stock
                    QuickActionButton(
                        icon = Icons.Default.Inventory,
                        label = if (isBangla) "ব্যবসায়িক স্টক" else "Business Stock",
                        containerColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showAddStockDialog.value = true }
                    )
                    // Transfer
                    QuickActionButton(
                        icon = Icons.Default.SwapHoriz,
                        label = if (isBangla) "তহবিল স্থানান্তর" else "Transfer",
                        containerColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showTransferDialog.value = true }
                    )
                }

                // Owner Capital & Withdrawal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.showOwnerCapitalDialog.value = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "+ মালিকের মূলধন" else "+ Owner Capital", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.showOwnerWithdrawalDialog.value = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "- ব্যক্তিগত উত্তোলন" else "- Owner Withdrawal", fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // UPCOMING COMMITMENTS & OVERDUE DEBTS CARDS
        // ==========================================
        if (metrics.upcomingInstallmentsPaisa > 0 || metrics.overdueDebtsPaisa > 0) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (metrics.upcomingInstallmentsPaisa > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBangla) "আসন্ন কিস্তির দায়" else "Upcoming Installments",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                                Text(
                                    text = CurrencyFormatter.formatBdt(metrics.upcomingInstallmentsPaisa, isBangla),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (metrics.overdueDebtsPaisa > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBangla) "মেয়াদোত্তীর্ণ দেনা" else "Overdue Debts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = CurrencyFormatter.formatBdt(metrics.overdueDebtsPaisa, isBangla),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // RECENT TRANSACTIONS
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "সাম্প্রতিক লেনদেন" else "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToTab(AppNavTab.LEDGER) }) {
                    Text(if (isBangla) "সব দেখুন >" else "View All >")
                }
            }
        }

        if (displayTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBangla) "কোনো সাম্প্রতিক লেনদেন নেই" else "No recent transactions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayTransactions, key = { it.id }) { tx ->
                TransactionCardItem(
                    tx = tx,
                    isBangla = isBangla,
                    onDelete = { viewModel.requestMoveToTrash(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // CASH BREAKDOWN DIALOG (Cash 1 & Cash 2)
    // ==========================================
    if (showCashBreakdownDialog) {
        val cash1Paisa = metrics.personalCash1Paisa
        val cash2Paisa = metrics.personalCash2Paisa
        val totalPersonalCashPaisa = cash1Paisa + cash2Paisa

        AlertDialog(
            onDismissRequest = { showCashBreakdownDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "নগদ টাকা বিবরণী (Cash Breakdown)" else "Cash Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isBalanceRevealed = !isBalanceRevealed },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isBalanceRevealed) "Hide Amounts" else "Show Amounts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "পার্সোনাল ক্যাশ ১" else "Personal Cash 1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatOrMask(cash1Paisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "পার্সোনাল ক্যাশ ২" else "Personal Cash 2",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatOrMask(cash2Paisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (selectedArea == FinanceArea.BUSINESS || selectedArea == FinanceArea.ALL) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBangla) "ব্যবসায়িক ক্যাশ" else "Business Cash",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatOrMask(metrics.businessCashPaisa),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedArea == FinanceArea.PERSONAL) {
                                if (isBangla) "মোট পার্সোনাল ক্যাশ" else "Total Personal Cash"
                            } else {
                                if (isBangla) "মোট ক্যাশ" else "Total Cash"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val totalCashToShow = when (selectedArea) {
                            FinanceArea.PERSONAL -> totalPersonalCashPaisa
                            FinanceArea.BUSINESS -> metrics.businessCashPaisa
                            FinanceArea.ALL -> metrics.cashBalancePaisa
                        }
                        Text(
                            text = formatOrMask(totalCashToShow),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCashBreakdownDialog = false }) {
                    Text(if (isBangla) "ঠিক আছে" else "Close")
                }
            }
        )
    }

    // ==========================================
    // BANK BREAKDOWN DIALOG (Bank 1 & Bank 2)
    // ==========================================
    if (showBankBreakdownDialog) {
        val bank1Paisa = metrics.personalBank1Paisa
        val bank2Paisa = metrics.personalBank2Paisa
        val totalPersonalBankPaisa = bank1Paisa + bank2Paisa

        AlertDialog(
            onDismissRequest = { showBankBreakdownDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ব্যাংক ব্যালেন্স বিবরণী (Bank Breakdown)" else "Bank Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isBalanceRevealed = !isBalanceRevealed },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isBalanceRevealed) "Hide Amounts" else "Show Amounts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "পার্সোনাল ব্যাংক ১" else "Personal Bank 1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatOrMask(bank1Paisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "পার্সোনাল ব্যাংক ২" else "Personal Bank 2",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatOrMask(bank2Paisa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    if (selectedArea == FinanceArea.BUSINESS || selectedArea == FinanceArea.ALL) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBangla) "ব্যবসায়িক ব্যাংক" else "Business Bank",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatOrMask(metrics.businessBankPaisa),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedArea == FinanceArea.PERSONAL) {
                                if (isBangla) "মোট পার্সোনাল ব্যাংক" else "Total Personal Bank"
                            } else {
                                if (isBangla) "মোট ব্যাংক ব্যালেন্স" else "Total Bank"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val totalBankToShow = when (selectedArea) {
                            FinanceArea.PERSONAL -> totalPersonalBankPaisa
                            FinanceArea.BUSINESS -> metrics.businessBankPaisa
                            FinanceArea.ALL -> metrics.bankBalancePaisa
                        }
                        Text(
                            text = formatOrMask(totalBankToShow),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }

                    // Transfer between Bank 1 & 2 shortcut button
                    OutlinedButton(
                        onClick = {
                            showBankBreakdownDialog = false
                            viewModel.showTransferDialog.value = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isBangla) "তহবিল স্থানান্তর (Transfer)" else "Transfer Between Accounts",
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBankBreakdownDialog = false }) {
                    Text(if (isBangla) "ঠিক আছে" else "Close")
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(52.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun TransactionCardItem(
    tx: TransactionEntity,
    isBangla: Boolean,
    onDelete: () -> Unit
) {
    val dtf = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")
    val dateStr = Instant.ofEpochMilli(tx.dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)

    val isIncome = tx.type == TransactionType.INCOME.name || tx.type == TransactionType.STOCK_SALE.name
    val isTransfer = tx.type == TransactionType.TRANSFER.name
    val isCapital = tx.type == TransactionType.OWNER_CAPITAL.name
    val isWithdrawal = tx.type == TransactionType.OWNER_WITHDRAWAL.name

    val amountColor = when {
        isIncome || isCapital -> Color(0xFF10B981)
        isTransfer -> Color(0xFF0284C7)
        else -> Color(0xFFEF4444)
    }

    val prefix = when {
        isIncome || isCapital -> "+"
        isTransfer -> ""
        else -> "-"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(amountColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome || isCapital) Icons.Default.CallReceived else if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.CallMade,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (tx.sourceDetails.isNotBlank()) tx.sourceDetails else tx.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Area Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (tx.area == "BUSINESS") Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (tx.area == "BUSINESS") (if (isBangla) "ব্যবসা" else "Biz") else (if (isBangla) "ব্যক্তিগত" else "Personal"),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.area == "BUSINESS") Color(0xFFD97706) else Color(0xFF2563EB)
                            )
                        }
                    }
                    Text(
                        text = "$dateStr ${if (tx.paidTo.isNotBlank()) "• ${tx.paidTo}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${CurrencyFormatter.formatBdt(tx.amountPaisa, isBangla)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
