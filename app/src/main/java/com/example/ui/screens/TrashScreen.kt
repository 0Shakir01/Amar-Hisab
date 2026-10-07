package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.ui.FinanceViewModel

@Composable
fun TrashScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val deletedTx by viewModel.deletedTransactions.collectAsState()
    val deletedDebts by viewModel.deletedDebts.collectAsState()
    val deletedInv by viewModel.deletedInvestments.collectAsState()
    val deletedAccounts by viewModel.deletedAccounts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Transactions, 1: Debts, 2: Investments, 3: Accounts

    val hasAnyTrash = deletedTx.isNotEmpty() || deletedDebts.isNotEmpty() || deletedInv.isNotEmpty() || deletedAccounts.isNotEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = if (isBangla) "রিসাইকেল বিন / ট্র্যাশ" else "Trash / Recently Deleted",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "মুছে ফেলা রেকর্ড পুনরুদ্ধার বা স্থায়ী অপসারণ" else "Restore or permanently delete items (2-step confirmation)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasAnyTrash) {
                    OutlinedButton(
                        onClick = { viewModel.startEmptyTrash() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "ট্র্যাশ খালি করুন" else "Empty Trash")
                    }
                }
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${if (isBangla) "লেনদেন" else "Tx"} (${deletedTx.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${if (isBangla) "দেনা/পাওনা" else "Debts"} (${deletedDebts.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("${if (isBangla) "বিনিয়োগ" else "Invest"} (${deletedInv.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("${if (isBangla) "অ্যাকাউন্ট" else "Acc"} (${deletedAccounts.size})") }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                if (deletedTx.isEmpty()) {
                    item { EmptyTrashNotice(isBangla) }
                } else {
                    items(deletedTx) { tx ->
                        TrashItemCard(
                            title = "${tx.type}: ${if (tx.category.isNotBlank()) tx.category else tx.sourceDetails}",
                            subtitle = CurrencyFormatter.formatBdt(tx.amountPaisa, useBanglaDigits = isBangla),
                            isBangla = isBangla,
                            onRestore = { viewModel.restoreItem(tx) },
                            onPermanentDelete = { viewModel.startPermanentDelete(tx) }
                        )
                    }
                }
            }
            1 -> {
                if (deletedDebts.isEmpty()) {
                    item { EmptyTrashNotice(isBangla) }
                } else {
                    items(deletedDebts) { d ->
                        TrashItemCard(
                            title = "${d.personName} (${if (d.debtType == "OTHERS_OWE_ME") "পাওনা" else "দেনা"})",
                            subtitle = CurrencyFormatter.formatBdt(d.remainingAmountPaisa, useBanglaDigits = isBangla),
                            isBangla = isBangla,
                            onRestore = { viewModel.restoreItem(d) },
                            onPermanentDelete = { viewModel.startPermanentDelete(d) }
                        )
                    }
                }
            }
            2 -> {
                if (deletedInv.isEmpty()) {
                    item { EmptyTrashNotice(isBangla) }
                } else {
                    items(deletedInv) { inv ->
                        TrashItemCard(
                            title = inv.projectName,
                            subtitle = "${if (isBangla) "পরিশোধিত:" else "Paid:"} ${CurrencyFormatter.formatBdt(inv.paidAmountPaisa, useBanglaDigits = isBangla)}",
                            isBangla = isBangla,
                            onRestore = { viewModel.restoreItem(inv) },
                            onPermanentDelete = { viewModel.startPermanentDelete(inv) }
                        )
                    }
                }
            }
            3 -> {
                if (deletedAccounts.isEmpty()) {
                    item { EmptyTrashNotice(isBangla) }
                } else {
                    items(deletedAccounts) { acc ->
                        TrashItemCard(
                            title = acc.name,
                            subtitle = "${if (isBangla) "প্রারম্ভিক:" else "Opening:"} ${CurrencyFormatter.formatBdt(acc.openingBalancePaisa, useBanglaDigits = isBangla)}",
                            isBangla = isBangla,
                            onRestore = { viewModel.restoreItem(acc) },
                            onPermanentDelete = { viewModel.startPermanentDelete(acc) }
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

@Composable
private fun EmptyTrashNotice(isBangla: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isBangla) "এই সেকশনে কোন ট্র্যাশ আইটেম নেই" else "No items in trash for this section",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrashItemCard(
    title: String,
    subtitle: String,
    isBangla: Boolean,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onRestore,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "পুনরুদ্ধার" else "Restore")
                }
                IconButton(onClick = onPermanentDelete) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Permanent Delete", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}
