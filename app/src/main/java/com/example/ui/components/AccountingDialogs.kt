package com.example.ui.components

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AssetTypeSuggestions
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DebtType
import com.example.data.model.ExpenseSuggestions
import com.example.data.model.FinanceArea
import com.example.data.model.IncomeSuggestions
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.ImportPreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

// ==========================================
// 1. ADD INCOME DIALOG (FREE-TEXT & AREA ENFORCED)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIncomeDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, dateMillis: Long, accountId: Long, incomeDetails: String, area: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var incomeDetails by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("PERSONAL") } // PERSONAL or BUSINESS
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateStr = remember(dateMillis) {
        Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CallReceived, contentDescription = null, tint = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "টাকা গ্রহণ (Money In)" else "Money In (Income)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "অর্জিত বা প্রাপ্ত অর্থ রেকর্ড করুন" else "Record incoming funds",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Area Selector: Personal vs Business
                Text(
                    text = if (isBangla) "আর্থিক খাত (খাত নির্বাচন করুন):" else "Select Area:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERSONAL" to (if (isBangla) "ব্যক্তিগত (Personal)" else "Personal"),
                           "BUSINESS" to (if (isBangla) "ব্যবসা (Business)" else "Business")).forEach { (key, label) ->
                        val isSelected = selectedArea == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedArea = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "টাকার পরিমাণ (৳)" else "Amount (BDT)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Free-text Income Type / Details
                OutlinedTextField(
                    value = incomeDetails,
                    onValueChange = { incomeDetails = it },
                    label = { Text(if (isBangla) "আয়ের বিবরণ / খাত (যেকোনো কিছু লিখুন)" else "Income Details (Free Text)") },
                    placeholder = { Text(if (isBangla) "যেমন: বেতন, শাড়ি বিক্রি, কমিশন..." else "e.g. Salary, Saree sale...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick Suggestion Chips
                Text(
                    text = if (isBangla) "দ্রুত পরামর্শ (ক্লিক করুন):" else "Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IncomeSuggestions.common.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clickable { incomeDetails = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "জমা হিসাব (ক্যাশ / ব্যাংক)" else "Deposit Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${accWithBal.account.name} [${accWithBal.account.area}]")
                                        Text(
                                            CurrencyFormatter.formatBdt(accWithBal.calculatedBalancePaisa, isBangla),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Button
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = dateMillis
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance()
                                selected.set(y, m, d)
                                dateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${if (isBangla) "তারিখ: " else "Date: "} $dateStr")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক নোট (Note)" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক টাকার পরিমাণ দিন" else "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val desc = if (incomeDetails.isBlank()) (if (isBangla) "আয়" else "Income") else incomeDetails.trim()
                        onSave(amount, dateMillis, selectedAccountId, desc, selectedArea, note.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "টাকা জমা করুন" else "Save Income",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ADD EXPENSE DIALOG (FREE-TEXT & AREA ENFORCED)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, dateMillis: Long, accountId: Long, expenseDetails: String, area: String, paidTo: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var expenseDetails by remember { mutableStateOf("") }
    var paidTo by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("PERSONAL") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateStr = remember(dateMillis) {
        Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CallMade, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "টাকা পরিশোধ (Money Out)" else "Money Out (Expense)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "ব্যয় বা খরচ রেকর্ড করুন" else "Record money spent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Area Selector: Personal vs Business
                Text(
                    text = if (isBangla) "খরচের খাত (খাত নির্বাচন করুন):" else "Select Area:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERSONAL" to (if (isBangla) "ব্যক্তিগত (Personal)" else "Personal"),
                           "BUSINESS" to (if (isBangla) "ব্যবসা (Business)" else "Business")).forEach { (key, label) ->
                        val isSelected = selectedArea == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedArea = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "টাকার পরিমাণ (৳)" else "Amount (BDT)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Free-text Expense Type / Details
                OutlinedTextField(
                    value = expenseDetails,
                    onValueChange = { expenseDetails = it },
                    label = { Text(if (isBangla) "খরচের বিবরণ (যেকোনো কিছু লিখুন)" else "Expense Details (Free Text)") },
                    placeholder = { Text(if (isBangla) "যেমন: বাড়ি ভাড়া, বাজার, কারখানা খরচ..." else "e.g. Rent, Grocery...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick Suggestion Chips
                Text(
                    text = if (isBangla) "দ্রুত পরামর্শ (ক্লিক করুন):" else "Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseSuggestions.common.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clickable { expenseDetails = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Paid To / Place (Optional)
                OutlinedTextField(
                    value = paidTo,
                    onValueChange = { paidTo = it },
                    label = { Text(if (isBangla) "কাকে প্রদান করা হলো (ঐচ্ছিক)" else "Paid To / Vendor (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "প্রদান হিসাব (ক্যাশ / ব্যাংক)" else "Payment Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${accWithBal.account.name} [${accWithBal.account.area}]")
                                        Text(
                                            CurrencyFormatter.formatBdt(accWithBal.calculatedBalancePaisa, isBangla),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Button
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = dateMillis
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance()
                                selected.set(y, m, d)
                                dateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${if (isBangla) "তারিখ: " else "Date: "} $dateStr")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক নোট (Note)" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক টাকার পরিমাণ দিন" else "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val desc = if (expenseDetails.isBlank()) (if (isBangla) "ব্যয়" else "Expense") else expenseDetails.trim()
                        onSave(amount, dateMillis, selectedAccountId, desc, selectedArea, paidTo.trim(), note.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "টাকা পরিশোধ রেকর্ড করুন" else "Save Expense",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. OPENING BALANCE SETUP DIALOG
// ==========================================
@Composable
fun OpeningBalanceSetupDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (area: String, cashAmountPaisa: Long, bankAmountPaisa: Long, dateMillis: Long, note: String) -> Unit
) {
    var selectedArea by remember { mutableStateOf("PERSONAL") }
    var cashText by remember { mutableStateOf("") }
    var bankText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    val dateStr = remember(dateMillis) {
        Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF0284C7))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "প্রারম্ভিক ব্যালেন্স (Opening Balance)" else "Opening Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "হিসাব শুরুর নগদ ও ব্যাংক তহবিল" else "Starting Cash and Bank Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Area Selector
                Text(
                    text = if (isBangla) "খাত নির্বাচন করুন:" else "Select Area:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERSONAL" to (if (isBangla) "ব্যক্তিগত (Personal)" else "Personal"),
                           "BUSINESS" to (if (isBangla) "ব্যবসা (Business)" else "Business")).forEach { (key, label) ->
                        val isSelected = selectedArea == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedArea = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cash
                OutlinedTextField(
                    value = cashText,
                    onValueChange = { cashText = it },
                    label = { Text(if (isBangla) "নগদ টাকা (Cash Balance ৳)" else "Cash Balance (BDT)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bank Inputs: For Personal area, separate Bank 1 and Bank 2!
                if (selectedArea == "PERSONAL") {
                    var bank1Text by remember { mutableStateOf("") }
                    var bank2Text by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = bank1Text,
                        onValueChange = { bank1Text = it },
                        label = { Text(if (isBangla) "ব্যাংক ১ ব্যালেন্স (Bank 1 ৳)" else "Bank 1 Balance (BDT)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bank2Text,
                        onValueChange = { bank2Text = it },
                        label = { Text(if (isBangla) "ব্যাংক ২ ব্যালেন্স (Bank 2 ৳)" else "Bank 2 Balance (BDT)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Picker
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = dateMillis
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val selected = Calendar.getInstance()
                                    selected.set(y, m, d)
                                    dateMillis = selected.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${if (isBangla) "শুরুর তারিখ: " else "Opening Date: "} $dateStr")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ (Note)" else "Optional Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cashPaisa = (cashText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                            val b1Paisa = (bank1Text.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                            val b2Paisa = (bank2Text.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                            // For Personal, pass Bank 1 balance as primary bank, and handle Bank 2
                            onSave(selectedArea, cashPaisa, b1Paisa + b2Paisa, dateMillis, note.trim())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBangla) "প্রারম্ভিক তহবিল সংরক্ষণ করুন" else "Save Starting Balance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Business Area: Single Business Bank
                    OutlinedTextField(
                        value = bankText,
                        onValueChange = { bankText = it },
                        label = { Text(if (isBangla) "ব্যবসা ব্যাংক ব্যালেন্স (Business Bank ৳)" else "Business Bank Balance (BDT)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Picker
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = dateMillis
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val selected = Calendar.getInstance()
                                    selected.set(y, m, d)
                                    dateMillis = selected.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${if (isBangla) "শুরুর তারিখ: " else "Opening Date: "} $dateStr")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ (Note)" else "Optional Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cashPaisa = (cashText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                            val bankPaisa = (bankText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                            onSave(selectedArea, cashPaisa, bankPaisa, dateMillis, note.trim())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBangla) "প্রারম্ভিক তহবিল সংরক্ষণ করুন" else "Save Starting Balance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. ADD ASSET DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAssetDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, assetType: String, area: String, totalPurchaseValuePaisa: Long, paidAmountPaisa: Long, currentValuePaisa: Long, purchaseDateMillis: Long, accountId: Long, installmentPaisa: Long, nextPaymentDateMillis: Long?, note: String, isOpening: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf("") }
    var isOpening by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf("PERSONAL") }
    var totalValueText by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var currentValueText by remember { mutableStateOf("") }
    var installmentAmountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var purchaseDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var nextDueDateMillis by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF8B5CF6))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOpening) (if (isBangla) "পুরোনো / প্রারম্ভিক সম্পদ" else "Existing / Opening Asset") else (if (isBangla) "নতুন সম্পদ যোগ করুন" else "New Asset Purchase"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOpening) (if (isBangla) "অ্যাপ শুরুর আগের বিদ্যমান সম্পদ (ক্যাশ কমবে না)" else "Owned before app start (Cash unchanged)") else (if (isBangla) "স্থায়ী সম্পদ, বিনিয়োগ ও প্লট/শেয়ার" else "Fixed assets, plots, equipment"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Entry Mode Switcher: New Current Purchase vs Existing Opening Asset
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        false to (if (isBangla) "নতুন ক্রয়" else "New Purchase"),
                        true to (if (isBangla) "পুরোনো / প্রারম্ভিক সম্পদ" else "Opening Asset")
                    ).forEach { (isOp, label) ->
                        val isSelected = isOpening == isOp
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) (if (isOp) Color(0xFF6366F1) else Color(0xFF8B5CF6)) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isOpening = isOp }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isOpening) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBangla)
                                "★ পুরোনো / প্রারম্ভিক সম্পদ — বর্তমান ক্যাশ বা ব্যাংক ব্যালেন্সে কোনো প্রভাব ফেলে না, শুধু সম্পদ ও মোট সম্পদ বৃদ্ধি করে।"
                            else
                                "★ Existing / Opening Asset — does not affect current Cash or Bank balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Area Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERSONAL" to (if (isBangla) "ব্যক্তিগত (Personal)" else "Personal"),
                           "BUSINESS" to (if (isBangla) "ব্যবসা (Business)" else "Business")).forEach { (key, label) ->
                        val isSelected = selectedArea == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedArea = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "সম্পদের নাম" else "Asset Name") },
                    placeholder = { Text(if (isBangla) "যেমন: মোবাইল, সেলাই মেশিন, প্লট..." else "e.g. Laptop, Plot...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = assetType,
                    onValueChange = { assetType = it },
                    label = { Text(if (isBangla) "সম্পদের ধরন (টাইপ)" else "Asset Type") },
                    placeholder = { Text(if (isBangla) "যেমন: ইলেকট্রনিক্স, জমি, সরঞ্জাম..." else "e.g. Plot, Machinery...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssetTypeSuggestions.common.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clickable { assetType = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = totalValueText,
                    onValueChange = { totalValueText = it },
                    label = { Text(if (isBangla) "মোট ক্রয়মূল্য (৳)" else "Total Purchase Value (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = paidAmountText,
                    onValueChange = { paidAmountText = it },
                    label = { Text(if (isBangla) "পরিশোধিত অর্থ (৳)" else "Amount Paid (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = installmentAmountText,
                    onValueChange = { installmentAmountText = it },
                    label = { Text(if (isBangla) "প্রতি কিস্তির পরিমাণ (প্রযোজ্য হলে ৳)" else "Installment Amount (Optional ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!isOpening) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Account used
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                    ) {
                        val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                        OutlinedTextField(
                            value = currentAcc?.name ?: (if (isBangla) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Account"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "পরিশোধে ব্যবহৃত হিসাব (ক্যাশ/ব্যাংক)" else "Account Used") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            accounts.forEach { accWithBal ->
                                DropdownMenuItem(
                                    text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                    onClick = {
                                        selectedAccountId = accWithBal.account.id
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক নোট (Note)" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val totalVal = (totalValueText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        val paidVal = (paidAmountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        val currentVal = (currentValueText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: paidVal
                        val instVal = (installmentAmountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L

                        if (name.isBlank() || totalVal <= 0L) {
                            Toast.makeText(context, if (isBangla) "সম্পদের নাম ও মোট মূল্য দিন" else "Please enter asset name and price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val finalType = if (assetType.isBlank()) "Asset" else assetType.trim()
                        onSave(name.trim(), finalType, selectedArea, totalVal, paidVal, currentVal, purchaseDateMillis, if (isOpening) 1L else selectedAccountId, instVal, nextDueDateMillis, note.trim(), isOpening)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOpening) Color(0xFF6366F1) else Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isOpening) {
                            if (isBangla) "প্রারম্ভিক সম্পদ সংরক্ষণ করুন" else "Confirm Opening Asset"
                        } else {
                            if (isBangla) "সম্পদ সংরক্ষণ করুন" else "Save Asset"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. PAY ASSET INSTALLMENT DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayAssetInstallmentDialog(
    asset: AssetEntity,
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, accountId: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (asset.installmentPaisa > 0) (asset.installmentPaisa / 100.0).toString() else "") }
    var selectedAccountId by remember { mutableStateOf(asset.accountId) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "কিস্তি পরিশোধ: ${asset.name}" else "Pay Installment: ${asset.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (isBangla) "বাকি আছে: " else "Remaining: "}${CurrencyFormatter.formatBdt(asset.remainingPaisa, isBangla)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "কিস্তির পরিমাণ (৳)" else "Payment Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "হিসাব নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "প্রদান হিসাব" else "Payment Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ দিন" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(amount, selectedAccountId, note.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "কিস্তি জমা নিশ্চিত করুন" else "Confirm Installment",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. ADD BUSINESS STOCK DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (productName: String, area: String, purchaseCostPerUnitPaisa: Long, purchasedQuantity: Double, accountId: Long, note: String, isOpening: Boolean) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var costPerUnitText by remember { mutableStateOf("") }
    var isOpening by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull { it.account.area == "BUSINESS" }?.account?.id ?: accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val totalCost = remember(quantityText, costPerUnitText) {
        val q = quantityText.toDoubleOrNull() ?: 0.0
        val c = costPerUnitText.toDoubleOrNull() ?: 0.0
        q * c
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFFD97706))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOpening) (if (isBangla) "বিদ্যমান / প্রারম্ভিক স্টক" else "Existing / Opening Stock") else (if (isBangla) "নতুন ব্যবসায়িক স্টক ক্রয়" else "New Stock Purchase"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOpening) (if (isBangla) "অ্যাপ শুরুর আগের পণ্য (ক্যাশ বা ব্যাংক কমবে না)" else "Old stock before app start (Cash unchanged)") else (if (isBangla) "পণ্য স্টক সম্পদ বৃদ্ধি করে, সাধারণ খরচ নয়" else "Stock is an Asset, paid from account"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Entry Mode Switcher: New Stock Purchase vs Existing Opening Stock
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        false to (if (isBangla) "নতুন ক্রয়" else "New Purchase"),
                        true to (if (isBangla) "বিদ্যমান / প্রারম্ভিক স্টক" else "Opening Stock")
                    ).forEach { (isOp, label) ->
                        val isSelected = isOpening == isOp
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isOpening = isOp }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(if (isBangla) "পণ্যের নাম" else "Product Name") },
                    placeholder = { Text(if (isBangla) "যেমন: জামদানি শাড়ি, থ্রি-পিস..." else "e.g. Saree fabric, Shirts...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (isBangla) (if (isOpening) "বিদ্যমান পরিমাণ (সংখ্যা/পিস)" else "ক্রয়কৃত পরিমাণ (সংখ্যা/পিস)") else (if (isOpening) "Existing Quantity" else "Purchased Quantity")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = costPerUnitText,
                    onValueChange = { costPerUnitText = it },
                    label = { Text(if (isBangla) "একক ক্রয়মূল্য (৳ প্রতি পিস)" else "Cost Per Unit (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (totalCost > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD97706).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${if (isBangla) "মোট স্টক মূল্য: " else "Total Stock Value: "}৳ ${"%.2f".format(totalCost)}",
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                if (!isOpening) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                    ) {
                        val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                        OutlinedTextField(
                            value = currentAcc?.name ?: (if (isBangla) "হিসাব নির্বাচন করুন" else "Select Account"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "পরিশোধে ব্যবহৃত হিসাব" else "Payment Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            accounts.forEach { accWithBal ->
                                DropdownMenuItem(
                                    text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                    onClick = {
                                        selectedAccountId = accWithBal.account.id
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val qty = quantityText.trim().toDoubleOrNull() ?: 0.0
                        val cost = (costPerUnitText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (productName.isBlank() || qty <= 0.0) {
                            Toast.makeText(context, if (isBangla) "পণ্যের নাম ও পরিমাণ দিন" else "Please enter product name and quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(productName.trim(), "BUSINESS", cost, qty, if (isOpening) 1L else selectedAccountId, note.trim(), isOpening)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isOpening) {
                            if (isBangla) "প্রারম্ভিক স্টক সংরক্ষণ করুন" else "Save Opening Stock"
                        } else {
                            if (isBangla) "স্টক ক্রয় সংরক্ষণ করুন" else "Save Stock Purchase"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 7. RECORD STOCK SALE DIALOG (WITH PROFIT CALCULATION)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordStockSaleDialog(
    stock: StockItemEntity,
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (soldQuantity: Double, salePricePerUnitPaisa: Long, accountId: Long, note: String) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }
    var salePriceText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(stock.accountId) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
    val totalRevenue = qty * salePrice
    val totalCost = qty * (stock.purchaseCostPerUnitPaisa / 100.0)
    val profit = totalRevenue - totalCost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "পণ্য বিক্রয়: ${stock.productName}" else "Sell: ${stock.productName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (isBangla) "মজুদ আছে: " else "In Stock: "}${stock.remainingQuantity} টি (একক খরচ: ৳${stock.purchaseCostPerUnitPaisa / 100})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (isBangla) "বিক্রয়কৃত সংখ্যা (পিস)" else "Quantity Sold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = salePriceText,
                    onValueChange = { salePriceText = it },
                    label = { Text(if (isBangla) "একক বিক্রয়মূল্য (৳ প্রতি পিস)" else "Sale Price Per Unit (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (totalRevenue > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (profit >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${if (isBangla) "মোট বিক্রয়মূল্য: " else "Total Revenue: "}৳ ${"%.2f".format(totalRevenue)}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (isBangla) "ব্যবসায়িক মুনাফা: " else "Business Profit: "}৳ ${"%.2f".format(profit)}",
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "হিসাব নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "বিক্রয় অর্থ জমা হিসাব" else "Deposit Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val soldQty = quantityText.trim().toDoubleOrNull() ?: 0.0
                        val salePricePaisa = (salePriceText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (soldQty <= 0.0 || salePricePaisa <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ ও বিক্রয়মূল্য দিন" else "Please enter quantity and price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (soldQty > stock.remainingQuantity) {
                            Toast.makeText(context, if (isBangla) "পর্যাপ্ত স্টক নেই (মজুদ: ${stock.remainingQuantity})" else "Insufficient stock", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(soldQty, salePricePaisa, selectedAccountId, note.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "বিক্রয় সম্পন্ন করুন" else "Confirm Sale",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 8. OWNER CAPITAL & WITHDRAWAL DIALOGS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCapitalDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, businessAccountId: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull { it.account.area == "BUSINESS" }?.account?.id ?: accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (isBangla) "মালিকের মূলধন বিনিয়োগ (Owner Capital)" else "Owner Capital",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBangla) "ব্যক্তিগত থেকে ব্যবসায় মূলধন (ব্যবসায়িক আয় নয়)" else "Personal to Business (Not business income)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "মূলধনের পরিমাণ (৳)" else "Capital Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = accountDropdownExpanded, onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "হিসাব নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "ব্যবসার জমা হিসাব" else "Business Deposit Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = accountDropdownExpanded, onDismissRequest = { accountDropdownExpanded = false }) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ দিন" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(amount, selectedAccountId, note.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "মূলধন সংরক্ষণ করুন" else "Save Owner Capital", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerWithdrawalDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountPaisa: Long, businessAccountId: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull { it.account.area == "BUSINESS" }?.account?.id ?: accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (isBangla) "মালিকের ব্যক্তিগত উত্তোলন (Owner Withdrawal)" else "Owner Withdrawal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBangla) "ব্যবসা থেকে ব্যক্তিগত উত্তোলন (ব্যবসায়িক ব্যয় নয়)" else "Business to Personal (Not business expense)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "উত্তোলনের পরিমাণ (৳)" else "Withdrawal Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = accountDropdownExpanded, onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: (if (isBangla) "হিসাব নির্বাচন করুন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "ব্যবসার যে হিসাব থেকে উত্তোলন" else "Business Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = accountDropdownExpanded, onDismissRequest = { accountDropdownExpanded = false }) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ দিন" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(amount, selectedAccountId, note.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "উত্তোলন নিশ্চিত করুন" else "Confirm Withdrawal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 9. TRANSFER MONEY DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMoneyDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (sourceAccountId: Long, destinationAccountId: Long, amountPaisa: Long, dateMillis: Long, area: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var sourceAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var destinationAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.account?.id ?: accounts.firstOrNull()?.account?.id ?: 2L) }
    var selectedArea by remember { mutableStateOf("PERSONAL") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }
    var destDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF0284C7))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = if (isBangla) "তহবিল স্থানান্তর (Transfer)" else "Transfer Money", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = if (isBangla) "ক্যাশ থেকে ব্যাংক বা অ্যাকাউন্ট স্থানান্তর" else "Cash to Bank or Account transfer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "স্থানান্তরের পরিমাণ (৳)" else "Transfer Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Source Account
                ExposedDropdownMenuBox(expanded = sourceDropdownExpanded, onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }) {
                    val currentSource = accounts.find { it.account.id == sourceAccountId }?.account
                    OutlinedTextField(
                        value = currentSource?.name ?: "Source",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "যে হিসাব থেকে যাবে (উৎস)" else "From Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = sourceDropdownExpanded, onDismissRequest = { sourceDropdownExpanded = false }) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    sourceAccountId = accWithBal.account.id
                                    sourceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination Account
                ExposedDropdownMenuBox(expanded = destDropdownExpanded, onExpandedChange = { destDropdownExpanded = !destDropdownExpanded }) {
                    val currentDest = accounts.find { it.account.id == destinationAccountId }?.account
                    OutlinedTextField(
                        value = currentDest?.name ?: "Destination",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "যে হিসাবে জমা হবে (গন্তব্য)" else "To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = destDropdownExpanded, onDismissRequest = { destDropdownExpanded = false }) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    destinationAccountId = accWithBal.account.id
                                    destDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ দিন" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (sourceAccountId == destinationAccountId) {
                            Toast.makeText(context, if (isBangla) "উৎস ও গন্তব্য হিসাব ভিন্ন হতে হবে" else "Source and destination must be different", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(sourceAccountId, destinationAccountId, amount, dateMillis, selectedArea, note.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "স্থানান্তর সম্পন্ন করুন" else "Complete Transfer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 10. ADD DEBT DIALOG (DHAR / PAWNA / DENA / OPENING)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    initialDebtType: DebtType = DebtType.OTHERS_OWE_ME,
    initialIsOpening: Boolean = false,
    onSave: (personName: String, debtType: DebtType, amountPaisa: Long, dateMillis: Long, area: String, phone: String, dueDateMillis: Long?, accountId: Long, note: String, isOpening: Boolean, originalDateMillis: Long?) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedDebtType by remember { mutableStateOf(initialDebtType) }
    var isOpening by remember { mutableStateOf(initialIsOpening) }
    var selectedArea by remember { mutableStateOf("PERSONAL") }
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var originalDateMillis by remember { mutableStateOf<Long?>(null) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dueDateStr = remember(dueDateMillis) {
        dueDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }
    val origDateStr = remember(originalDateMillis) {
        originalDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFEA580C))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val dialogTitle = if (isOpening) {
                                if (selectedDebtType == DebtType.OTHERS_OWE_ME) {
                                    if (isBangla) "পুরোনো পাওনা / শুরুর পাওনা" else "Old Debt / Opening Receivable"
                                } else {
                                    if (isBangla) "পুরোনো দেনা / শুরুর দেনা" else "Old Debt / Opening Payable"
                                }
                            } else {
                                if (selectedDebtType == DebtType.OTHERS_OWE_ME) {
                                    if (isBangla) "নতুন ধার দেওয়া (Lending)" else "New Lending"
                                } else {
                                    if (isBangla) "নতুন ঋণ গ্রহণ (Borrowing)" else "New Borrowing"
                                }
                            }
                            Text(text = dialogTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isOpening) {
                                    if (isBangla) "অ্যাপ শুরুর আগের ঐতিহাসিক দেনা-পাওনা" else "Historical debt before start date"
                                } else {
                                    if (isBangla) "বর্তমান রিয়েল নগদ/ব্যাংক লেনদেন" else "Live cash/bank transaction"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Entry Mode Switcher: New Live Lending/Borrowing vs Old Debt / Opening Balance
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        false to (if (isBangla) "নতুন লেনদেন" else "New Live Entry"),
                        true to (if (isBangla) "পুরোনো / প্রারম্ভিক" else "Old Debt / Opening")
                    ).forEach { (isOp, label) ->
                        val isSelected = isOpening == isOp
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) (if (isOp) Color(0xFF6366F1) else Color(0xFFEA580C)) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { isOpening = isOp }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isOpening) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBangla)
                                "★ পুরোনো পাওনা / শুরুর পাওনা — বর্তমান ক্যাশ বা ব্যাংক ব্যালেন্সে কোনো প্রভাব ফেলে না।"
                            else
                                "★ Old Debt / Opening Receivable — does not affect current Cash or Bank balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Direction: Others Owe Me vs I Owe Others
                Text(text = if (isBangla) "লেনদেনের দিক:" else "Direction:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        DebtType.OTHERS_OWE_ME to (if (isBangla) "পাওনা (Others Owe Me)" else "Others Owe Me"),
                        DebtType.I_OWE_OTHERS to (if (isBangla) "দেনা (I Owe Others)" else "I Owe Others")
                    ).forEach { (dt, label) ->
                        val isSelected = selectedDebtType == dt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) (if (dt == DebtType.OTHERS_OWE_ME) Color(0xFF10B981) else Color(0xFFEF4444)) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { selectedDebtType = dt }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Area: Personal vs Business
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PERSONAL" to (if (isBangla) "ব্যক্তিগত" else "Personal"),
                           "BUSINESS" to (if (isBangla) "ব্যবসা" else "Business")).forEach { (key, label) ->
                        val isSelected = selectedArea == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { selectedArea = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text(if (isBangla) "ব্যক্তির নাম (Person Name)" else "Person Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর (ঐচ্ছিক)" else "Phone Number (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isOpening) (if (isBangla) "বর্তমান অবশিষ্ট পরিমাণ (৳)" else "Current Remaining Amount (BDT)") else (if (isBangla) "টাকার পরিমাণ (৳)" else "Amount (BDT)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!isOpening) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(expanded = accountDropdownExpanded, onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }) {
                        val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                        OutlinedTextField(
                            value = currentAcc?.name ?: "Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "টাকা প্রদান/গ্রহণের অ্যাকাউন্ট" else "Account (Cash/Bank 1/Bank 2)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = accountDropdownExpanded, onDismissRequest = { accountDropdownExpanded = false }) {
                            accounts.forEach { accWithBal ->
                                DropdownMenuItem(
                                    text = { Text("${accWithBal.account.name} [${accWithBal.account.area}] (${CurrencyFormatter.formatBdt(accWithBal.calculatedBalancePaisa, isBangla)})") },
                                    onClick = {
                                        selectedAccountId = accWithBal.account.id
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (isOpening) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Optional original date
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            originalDateMillis?.let { cal.timeInMillis = it }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val selected = Calendar.getInstance()
                                    selected.set(y, m, d)
                                    originalDateMillis = selected.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(origDateStr?.let { "${if (isBangla) "মূল ঋণের তারিখ: " else "Original Date: "} $it" } ?: (if (isBangla) "মূল ঋণের তারিখ (ঐচ্ছিক)" else "Original Date (Optional)"))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        dueDateMillis?.let { cal.timeInMillis = it }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance()
                                selected.set(y, m, d)
                                dueDateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dueDateStr?.let { "${if (isBangla) "পরিশোধের শেষ তারিখ: " else "Due: "} $it" } ?: (if (isBangla) "পরিশোধের সম্ভাব্য তারিখ নির্ধারণ করুন (ঐচ্ছিক)" else "Set Due Date (Optional)"))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (personName.isBlank() || amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "নাম ও টাকার পরিমাণ দিন" else "Please enter name and amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(
                            personName.trim(),
                            selectedDebtType,
                            amount,
                            dateMillis,
                            selectedArea,
                            phone.trim(),
                            dueDateMillis,
                            if (isOpening) 1L else selectedAccountId,
                            note.trim(),
                            isOpening,
                            originalDateMillis
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOpening) Color(0xFF6366F1) else Color(0xFFEA580C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isOpening) {
                            if (isBangla) "প্রারম্ভিক ব্যালেন্স নিশ্চিত করুন" else "Confirm Opening Balance"
                        } else {
                            if (isBangla) "দেনা/পাওনা নিশ্চিত করুন" else "Confirm Debt Entry"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 11. DEBT REPAYMENT DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtRepaymentDialog(
    debt: DebtEntity,
    accounts: List<AccountWithBalance>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (repaymentAmountPaisa: Long, accountId: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf((debt.remainingAmountPaisa / 100.0).toString()) }
    var selectedAccountId by remember { mutableStateOf(debt.accountId) }
    var note by remember { mutableStateOf("") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isPawna = debt.debtType == DebtType.OTHERS_OWE_ME.key

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (isPawna) (if (isBangla) "পাওনা আদায়: ${debt.personName}" else "Collect Due: ${debt.personName}")
                                   else (if (isBangla) "দেনা পরিশোধ: ${debt.personName}" else "Pay Debt: ${debt.personName}"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (isBangla) "মোট অবশিষ্ট: " else "Remaining: "} ${CurrencyFormatter.formatBdt(debt.remainingAmountPaisa, isBangla)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "পরিশোধিত অর্থ (৳)" else "Repayment Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = accountDropdownExpanded, onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }) {
                    val currentAcc = accounts.find { it.account.id == selectedAccountId }?.account
                    OutlinedTextField(
                        value = currentAcc?.name ?: "Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isPawna) (if (isBangla) "জমা হিসাব" else "Deposit Account") else (if (isBangla) "প্রদান হিসাব" else "Payment Account")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = accountDropdownExpanded, onDismissRequest = { accountDropdownExpanded = false }) {
                        accounts.forEach { accWithBal ->
                            DropdownMenuItem(
                                text = { Text("${accWithBal.account.name} [${accWithBal.account.area}]") },
                                onClick = {
                                    selectedAccountId = accWithBal.account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ঐচ্ছিক বিবরণ" else "Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = (amountText.trim().toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                        if (amount <= 0L) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ দিন" else "Please enter valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSave(amount, selectedAccountId, note.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPawna) Color(0xFF10B981) else Color(0xFFEA580C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "পরিশোধ নিশ্চিত করুন" else "Confirm Repayment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 12. SOFT DELETE & TRASH CONFIRMATION DIALOGS
// ==========================================
@Composable
fun MoveToTrashConfirmDialog(
    item: Any,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (item) {
        is TransactionEntity -> if (isBangla) "লেনদেনটি ট্র্যাশে পাঠাবেন?" else "Move transaction to Trash?"
        is DebtEntity -> if (isBangla) "দেনা/পাওনা রেকর্ড ট্র্যাশে পাঠাবেন?" else "Move debt record to Trash?"
        is AssetEntity -> if (isBangla) "সম্পদটি ট্র্যাশে পাঠাবেন?" else "Move asset to Trash?"
        is StockItemEntity -> if (isBangla) "পণ্য স্টকটি ট্র্যাশে পাঠাবেন?" else "Move stock to Trash?"
        else -> if (isBangla) "আইটেমটি ট্র্যাশে পাঠাবেন?" else "Move to Trash?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                if (isBangla)
                    "এটি ট্র্যাশে / রিসাইকেল বিনে জমা থাকবে। আপনি যেকোনো সময় এটি পুনরুদ্ধার (Restore) করতে পারবেন।"
                else
                    "This item will be safely kept in the Recycle Bin. You can restore it anytime."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text(if (isBangla) "ট্র্যাশে পাঠান" else "Move to Trash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun TwoStepPermanentDeleteDialog(
    item: Any,
    step: Int, // 1 or 2
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirmStep1: () -> Unit,
    onConfirmStep2: () -> Unit
) {
    if (step == 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "স্থায়ীভাবে মুছে ফেলা (ধাপ ১/২)" else "Permanent Delete (Step 1/2)")
                }
            },
            text = {
                Text(
                    if (isBangla)
                        "সতর্কতা: এটি স্থায়ীভাবে ডাটাবেস থেকে মুছে যাবে। এই কাজ আর ফিরিয়ে আনা যাবে না। আপনি কি এগিয়ে যেতে চান?"
                    else
                        "Warning: This will permanently remove the record from storage. This cannot be undone. Proceed to final confirmation?"
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmStep1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isBangla) "পরবর্তী নিশ্চিতকরণ >" else "Proceed to Confirm >")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFF991B1B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "চূড়ান্ত নিশ্চিতকরণ (ধাপ ২/২)" else "Final Confirmation (Step 2/2)")
                }
            },
            text = {
                Text(
                    if (isBangla)
                        "স্থায়ীভাবে ডিলিট করতে নিশ্চিত করুন। লোকাল ডাটাবেস ও ক্লাউড সিঙ্ক থেকে এটি সম্পূর্ণরূপে অপসারিত হবে।"
                    else
                        "Confirm final permanent deletion. This record will be permanently purged."
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmStep2,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                ) {
                    Text(if (isBangla) "স্থায়ীভাবে ডিলিট করুন" else "Permanently Delete Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        )
    }
}

// ==========================================
// 13. IMPORT BACKUP PREVIEW DIALOG
// ==========================================
@Composable
fun ImportBackupPreviewDialog(
    preview: ImportPreview,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dateStr = remember(preview.exportedAt) {
        Instant.ofEpochMilli(preview.exportedAt).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBangla) "ব্যাকআপ ফাইল যাচাইকরণ সম্পন্ন" else "Backup File Verified")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isBangla) "ফাইলের তথ্য পর্যালোচনা করুন:" else "Review file contents before restore:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("• ${if (isBangla) "তৈরির সময়: " else "Created: "} $dateStr")
                Text("• ${if (isBangla) "অ্যাকাউন্ট সংখ্যা: " else "Accounts: "} ${preview.accountCount} টি")
                Text("• ${if (isBangla) "লেনদেন সংখ্যা: " else "Transactions: "} ${preview.transactionCount} টি")
                Text("• ${if (isBangla) "দেনা/পাওনা সংখ্যা: " else "Debts: "} ${preview.debtCount} টি")
                Text("• ${if (isBangla) "সম্পদ সংখ্যা: " else "Assets: "} ${preview.assetCount} টি")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBangla) "✓ রিস্টোরের পূর্বে একটি স্বয়ংক্রিয় সেফটি স্ন্যাপশট ব্যাকআপ তৈরি হবে।"
                           else "✓ An automatic safety snapshot backup will be created before restoring.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text(if (isBangla) "রিস্টোর নিশ্চিত করুন" else "Confirm Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}
