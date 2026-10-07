package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.assistant.ParsedTransactionResult
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CurrencyFormatter
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.FinancialGreenPrimary
import com.example.ui.theme.FinancialNavyPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionAssistantDialog(
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onParseText: suspend (String) -> ParsedTransactionResult,
    onConfirmSave: (
        type: TransactionType,
        amountPaisa: Long,
        categoryId: Long?,
        accountId: Long,
        dateMillis: Long,
        note: String
    ) -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedTransactionResult?>(null) }

    // Editable review fields
    var reviewType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var reviewAmountInput by remember { mutableStateOf("") }
    var reviewCategoryId by remember { mutableStateOf<Long?>(null) }
    var reviewAccountId by remember { mutableStateOf<Long>(accounts.firstOrNull()?.id ?: 1L) }
    var reviewDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var reviewNote by remember { mutableStateOf("") }
    var reviewError by remember { mutableStateOf<String?>(null) }

    val examplePrompts = listOf(
        "আজ দুপুরে ২৫০ টাকা খাবারে খরচ",
        "bKash থেকে ৫০০ টাকা আয়",
        "গতকাল বাস ভাড়া ৫০ টাকা",
        "salary পেলাম ৩০,০০০ টাকা"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FinancialGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FinancialGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "লেনদেন সহকারী (Bangla AI)" else "Transaction Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If not yet parsed, show input & examples
                if (parsedResult == null) {
                    Text(
                        text = if (isBangla) "বাংলা বা বাংলিশে আপনার লেনদেনের কথা লিখুন:"
                        else "Describe your transaction in Bangla or Banglish:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                if (isBangla) "যেমন: আজ দুপুরে ২৫০ টাকা খাবারে খরচ"
                                else "e.g. bKash theke 500 taka income"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        singleLine = false
                    )

                    // Quick Example Chips
                    Text(
                        text = if (isBangla) "উদাহরণসমূহ (ট্যাপ করে চেষ্টা করুন):" else "Quick Examples (Tap to try):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        examplePrompts.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { inputText = prompt }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isProcessing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = FinancialGreenPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBangla) "লেনদেন বিশ্লেষণ করা হচ্ছে..." else "Analyzing transaction...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    // MANDATORY REVIEW SCREEN
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = FinancialNavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "যাচাইকরণ স্ক্রিন (Review Screen): সংরক্ষণের পূর্বে তথ্যগুলো মিলিয়ে নিন"
                                else "Review Screen: Please verify before saving",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Warning for uncertain fields
                    val currentRes = parsedResult!!
                    if (currentRes.isUncertain || currentRes.uncertainFields.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBangla) "কিছু তথ্য স্পষ্ট নয়, অনুগ্রহ করে সংশোধন করুন:"
                                        else "Some fields need your attention:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF92400E),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                currentRes.uncertainFields.forEach { reason ->
                                    Text(
                                        text = "• $reason",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB45309)),
                                        modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Review Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Type Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(2.dp)
                            ) {
                                listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { t ->
                                    val isSel = reviewType == t
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) if (t == TransactionType.INCOME) IncomeGreen else ExpenseRed else Color.Transparent)
                                            .clickable { reviewType = t }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (t == TransactionType.INCOME) AppStrings.get("type_income", language) else AppStrings.get("type_expense", language),
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            // Amount Field
                            OutlinedTextField(
                                value = reviewAmountInput,
                                onValueChange = {
                                    reviewAmountInput = it
                                    reviewError = null
                                },
                                label = { Text(AppStrings.get("amount", language) + " (৳)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Category Selector
                            val availCats = categories.filter { it.type == reviewType.name }
                            Text(
                                text = AppStrings.get("select_category", language) + ":",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availCats.take(6).forEach { cat ->
                                    val isSel = reviewCategoryId == cat.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) CategoryIconHelper.parseColor(cat.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { reviewCategoryId = cat.id }
                                    ) {
                                        Text(
                                            text = if (isBangla) cat.nameBn else cat.nameEn,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Account Selector
                            Text(
                                text = AppStrings.get("select_account", language) + ":",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                accounts.filter { it.isActive }.forEach { acc ->
                                    val isSel = reviewAccountId == acc.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) CategoryIconHelper.getAccountColor(acc.type) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { reviewAccountId = acc.id }
                                    ) {
                                        Text(
                                            text = acc.name,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Note Field
                            OutlinedTextField(
                                value = reviewNote,
                                onValueChange = { reviewNote = it },
                                label = { Text(AppStrings.get("note", language)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Date Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FinancialGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = CurrencyFormatter.formatDate(reviewDateMillis, isBangla = isBangla),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    if (reviewError != null) {
                        Text(
                            text = reviewError ?: "",
                            color = ExpenseRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (parsedResult == null) {
                Button(
                    onClick = {
                        if (inputText.isBlank() || isProcessing) return@Button
                        isProcessing = true
                        coroutineScope.launch {
                            val res = onParseText(inputText)
                            parsedResult = res
                            reviewType = res.type
                            reviewAmountInput = if (res.amountPaisa > 0) (res.amountPaisa / 100.0).toString() else ""
                            reviewCategoryId = res.categoryId
                            reviewAccountId = res.accountId
                            reviewDateMillis = res.dateMillis
                            reviewNote = res.note
                            isProcessing = false
                        }
                    },
                    enabled = !isProcessing && inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialGreenPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "বিশ্লেষণ করুন" else "Parse Text")
                }
            } else {
                Button(
                    onClick = {
                        val paisa = CurrencyFormatter.parseBdtToPaisa(reviewAmountInput) ?: 0L
                        if (paisa <= 0) {
                            reviewError = AppStrings.get("error_amount_zero", language)
                            return@Button
                        }
                        if (reviewCategoryId == null) {
                            reviewError = AppStrings.get("error_category_required", language)
                            return@Button
                        }
                        onConfirmSave(
                            reviewType,
                            paisa,
                            reviewCategoryId,
                            reviewAccountId,
                            reviewDateMillis,
                            reviewNote
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialGreenPrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "নিশ্চিত ও সংরক্ষণ" else "Confirm & Save")
                }
            }
        },
        dismissButton = {
            if (parsedResult != null) {
                OutlinedButton(onClick = { parsedResult = null }) {
                    Text(if (isBangla) "পুনরায় লিখুন" else "Try Again")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        }
    )
}
