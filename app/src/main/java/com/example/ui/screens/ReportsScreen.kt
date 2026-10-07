package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DebtType
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeCategory
import com.example.data.model.TransactionType
import com.example.ui.DateRangeFilter
import com.example.ui.FinanceViewModel
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val investments by viewModel.investments.collectAsState()
    val dateRange by viewModel.selectedDateRange.collectAsState()
    val scope = rememberCoroutineScope()

    // Expense breakdown by the 6 standard categories
    val expenseCategoryTotals = ExpenseCategory.entries.map { cat ->
        val sum = transactions
            .filter { it.type == TransactionType.EXPENSE.name && it.category.equals(cat.key, ignoreCase = true) }
            .sumOf { it.amountPaisa }
        cat to sum
    }
    val totalExpenseInPeriod = expenseCategoryTotals.sumOf { it.second }.coerceAtLeast(1L)

    // Income breakdown by the 4 standard categories
    val incomeCategoryTotals = IncomeCategory.entries.map { cat ->
        val sum = transactions
            .filter { it.type == TransactionType.INCOME.name && it.category.equals(cat.key, ignoreCase = true) }
            .sumOf { it.amountPaisa }
        cat to sum
    }
    val totalIncomeInPeriod = incomeCategoryTotals.sumOf { it.second }.coerceAtLeast(1L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBangla) "আর্থিক রিপোর্ট ও বিশ্লেষণ" else "Financial Reports",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "আপনার সামগ্রিক আর্থিক অবস্থার সঠিক চিত্র" else "Overview of accounts, categories, debts & assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Date Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == dateRange,
                        onClick = { viewModel.selectedDateRange.value = filter },
                        label = { Text(if (isBangla) filter.labelBn else filter.labelEn) }
                    )
                }
            }
        }

        // 1. Account-wise balance report
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "১. অ্যাকাউন্টভিত্তিক ব্যালেন্স (Account Balances)" else "1. Account-wise Balances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    accounts.forEach { accWithBal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(accWithBal.account.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.formatBdt(accWithBal.calculatedBalancePaisa, useBanglaDigits = isBangla),
                                fontWeight = FontWeight.Bold,
                                color = if (accWithBal.calculatedBalancePaisa >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "মোট ব্যবহারযোগ্য অর্থ" else "Total Available Cash & Bank", fontWeight = FontWeight.Bold)
                        Text(
                            CurrencyFormatter.formatBdt(metrics.availableBalancePaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Expense Category Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "২. ব্যয়ের ক্যাটাগরি বিশ্লেষণ (Expense Breakdown)" else "2. Expense Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    expenseCategoryTotals.forEach { (cat, sum) ->
                        val fraction = (sum.toFloat() / totalExpenseInPeriod.toFloat()).coerceIn(0f, 1f)
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (isBangla) cat.titleBn else cat.titleEn, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.formatBdt(sum, useBanglaDigits = isBangla), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                color = cat.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        // 3. Debts (Others Owe Me vs I Owe Others)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "৩. দেনা ও পাওনা পরিস্থিতি (Debts Summary)" else "3. Debts & Loans Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "অন্যের কাছে পাওনা (Receivables):" else "Others Owe Me (Receivables):")
                        Text(
                            CurrencyFormatter.formatBdt(metrics.othersOweMePaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF97316)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "আমি দেনাদার (Payables):" else "I Owe Others (Payables):")
                        Text(
                            CurrencyFormatter.formatBdt(metrics.iOweOthersPaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "নিট ঋণ ব্যালেন্স:" else "Net Debt Position:")
                        val netDebt = metrics.othersOweMePaisa - metrics.iOweOthersPaisa
                        Text(
                            CurrencyFormatter.formatBdt(netDebt, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.Bold,
                            color = if (netDebt >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // 4. Investment Summary (Paid & Remaining)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "৪. বিনিয়োগ ও সম্পদ খতিয়ান (Investments)" else "4. Investments & Assets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "মোট সম্পদ ও মূলধনী বিনিয়োগ:" else "Total Capital Assets & Investments:")
                        Text(
                            CurrencyFormatter.formatBdt(metrics.fixedAssetsValuePaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "ভবিষ্যৎ বাকি কিস্তির পরিমাণ:" else "Remaining Future Installments:")
                        Text(
                            CurrencyFormatter.formatBdt(metrics.upcomingInstallmentsPaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBangla) "নিট ওয়ার্থ (Net Worth):" else "Net Worth (Assets - Liabilities):", fontWeight = FontWeight.Bold)
                        Text(
                            CurrencyFormatter.formatBdt(metrics.netWorthPaisa, useBanglaDigits = isBangla),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
