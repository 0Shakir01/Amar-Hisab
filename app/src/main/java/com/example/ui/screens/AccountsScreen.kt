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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.AccountType
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.data.repository.AccountWithBalance
import com.example.ui.FinanceViewModel

@Composable
fun AccountsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsState()

    var editingAccount by remember { mutableStateOf<AccountWithBalance?>(null) }
    var editNameText by remember { mutableStateOf("") }
    var editBalanceText by remember { mutableStateOf("") }
    var showCreateAccountDialog by remember { mutableStateOf(false) }

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
                        text = if (isBangla) "অ্যাকাউন্ট ও ব্যালেন্স" else "Accounts & Balances",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "যেখানে আপনার টাকা জমা আছে (নগদ, ব্যাংক ইত্যাদি)" else "Manage cash, bank & wallet accounts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.showOpeningBalanceSetup.value = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Setup Balances", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { showCreateAccountDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "নতুন" else "New")
                    }
                }
            }
        }

        items(accountsWithBalances) { accWithBal ->
            val acc = accWithBal.account
            val isCash = acc.type == AccountType.CASH.name
            val icon = if (isCash) Icons.Default.Payments else Icons.Default.AccountBalance
            val iconColor = if (isCash) Color(0xFF059669) else Color(0xFF1E40AF)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = iconColor)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(acc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${if (isBangla) "প্রারম্ভিক ব্যালেন্স:" else "Opening:"} ${CurrencyFormatter.formatBdt(acc.openingBalancePaisa, useBanglaDigits = isBangla)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.formatBdt(accWithBal.calculatedBalancePaisa, useBanglaDigits = isBangla),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (accWithBal.calculatedBalancePaisa >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                        Row {
                            IconButton(onClick = {
                                editingAccount = accWithBal
                                editNameText = acc.name
                                editBalanceText = CurrencyFormatter.formatBdt(acc.openingBalancePaisa, useBanglaDigits = false, showSymbol = false)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Account", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            if (accountsWithBalances.size > 2) {
                                IconButton(onClick = { viewModel.requestMoveToTrash(acc) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Account Dialog (Rename & Opening Balance)
    editingAccount?.let { accWithBal ->
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            title = { Text(if (isBangla) "অ্যাকাউন্ট সম্পাদনা ও ব্যালেন্স" else "Edit Account & Balance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        label = { Text(if (isBangla) "অ্যাকাউন্টের নাম (যেমন: DBBL, ব্র্যাক ব্যাংক)" else "Account Name (e.g. DBBL, BRAC)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBalanceText,
                        onValueChange = { editBalanceText = it },
                        label = { Text(if (isBangla) "প্রারম্ভিক ব্যালেন্স (৳)" else "Opening Balance (BDT)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val paisa = CurrencyFormatter.parseBdtToPaisa(editBalanceText) ?: 0L
                    viewModel.updateAccount(
                        accWithBal.account.id,
                        editNameText.ifBlank { accWithBal.account.name },
                        paisa
                    )
                    editingAccount = null
                }) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingAccount = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Create Account Dialog
    if (showCreateAccountDialog) {
        var newAccName by remember { mutableStateOf("") }
        var newAccType by remember { mutableStateOf(AccountType.BANK.name) }
        var newAccOpeningText by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showCreateAccountDialog = false },
            title = { Text(if (isBangla) "নতুন অ্যাকাউন্ট যোগ করুন" else "Add New Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAccName,
                        onValueChange = { newAccName = it },
                        label = { Text(if (isBangla) "অ্যাকাউন্টের নাম" else "Account Name") },
                        placeholder = { Text(if (isBangla) "যেমন: বিকাশ, ডাচ-বাংলা" else "e.g. bKash, DBBL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAccOpeningText,
                        onValueChange = { newAccOpeningText = it },
                        label = { Text(if (isBangla) "প্রারম্ভিক ব্যালেন্স (৳)" else "Opening Balance (BDT)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newAccName.isNotBlank()) {
                        val paisa = CurrencyFormatter.parseBdtToPaisa(newAccOpeningText) ?: 0L
                        viewModel.addAccount(newAccName.trim(), newAccType, paisa, "#10B981")
                        showCreateAccountDialog = false
                    }
                }) {
                    Text(if (isBangla) "তৈরি করুন" else "Create")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateAccountDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
