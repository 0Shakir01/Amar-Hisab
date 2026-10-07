package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.FinanceArea
import com.example.data.model.TransactionType
import com.example.ui.DateRangeFilter
import com.example.ui.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val transactions by viewModel.filteredTransactions.collectAsState()
    val dateRange by viewModel.selectedDateRange.collectAsState()
    val selectedArea by viewModel.selectedAreaFilter.collectAsState()
    val selectedType by viewModel.selectedLedgerType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = if (isBangla) "লেনদেন লেজার (Ledger)" else "Transaction Ledger",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text(if (isBangla) "বিবরণ, খাত বা নোট খুঁজুন..." else "Search description, vendor, note...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Row: Area & Date Range
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Area Filter Chips
            listOf(
                FinanceArea.ALL to (if (isBangla) "সকল খাত" else "All Areas"),
                FinanceArea.PERSONAL to (if (isBangla) "ব্যক্তিগত" else "Personal"),
                FinanceArea.BUSINESS to (if (isBangla) "ব্যবসা" else "Business")
            ).forEach { (area, label) ->
                FilterChip(
                    selected = selectedArea == area,
                    onClick = { viewModel.setAreaFilter(area) },
                    label = { Text(label) }
                )
            }

            // Date Range Filter Chips
            DateRangeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = dateRange == filter,
                    onClick = { viewModel.selectedDateRange.value = filter },
                    label = { Text(if (isBangla) filter.labelBn else filter.labelEn) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Filter Row 2: Type Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val typeFilters = listOf(
                null to (if (isBangla) "সকল ধরন" else "All Types"),
                TransactionType.INCOME.name to (if (isBangla) "টাকা জমা (In)" else "Money In"),
                TransactionType.EXPENSE.name to (if (isBangla) "টাকা খরচ (Out)" else "Money Out"),
                TransactionType.TRANSFER.name to (if (isBangla) "স্থানান্তর" else "Transfer"),
                TransactionType.STOCK_SALE.name to (if (isBangla) "স্টক বিক্রয়" else "Stock Sale"),
                TransactionType.OWNER_CAPITAL.name to (if (isBangla) "মালিকের মূলধন" else "Capital"),
                TransactionType.OWNER_WITHDRAWAL.name to (if (isBangla) "ব্যক্তিগত উত্তোলন" else "Withdrawal")
            )

            typeFilters.forEach { (typeKey, label) ->
                FilterChip(
                    selected = selectedType == typeKey,
                    onClick = { viewModel.selectedLedgerType.value = typeKey },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Count Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (isBangla) "মোট ফলাফল: " else "Found: "} ${transactions.size} ${if (isBangla) "টি লেনদেন" else "entries"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedAccountId != null || selectedType != null || searchQuery.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        viewModel.selectedAccountId.value = null
                        viewModel.selectedLedgerType.value = null
                        viewModel.searchQuery.value = ""
                    }
                ) {
                    Text(
                        text = if (isBangla) "ফিল্টার রিসেট" else "Reset Filters",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ledger List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBangla) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions match your filter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionCardItem(
                        tx = tx,
                        isBangla = isBangla,
                        onDelete = { viewModel.requestMoveToTrash(tx) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
