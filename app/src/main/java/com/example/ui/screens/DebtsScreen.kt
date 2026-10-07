package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.FinanceArea
import com.example.ui.FinanceViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DebtsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val debts by viewModel.debts.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()

    val currentTabFromVm by viewModel.selectedDebtTab.collectAsState()
    var selectedTab by remember { mutableStateOf(DebtType.OTHERS_OWE_ME) }
    var selectedArea by remember { mutableStateOf(FinanceArea.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(currentTabFromVm) {
        selectedTab = currentTabFromVm
    }

    val displayDebts = debts.filter { debt ->
        val matchType = debt.debtType == selectedTab.key
        val matchArea = selectedArea == FinanceArea.ALL || debt.area.equals(selectedArea.key, ignoreCase = true)
        val matchQuery = searchQuery.isBlank() ||
            debt.personName.contains(searchQuery, ignoreCase = true) ||
            debt.phone.contains(searchQuery) ||
            debt.note.contains(searchQuery, ignoreCase = true)
        matchType && matchArea && matchQuery && !debt.isDeleted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title & Add Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "দেনা-পাওনা হিসাব" else "Debt & Money Owed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBangla) "ধার দেওয়া ও ধার নেওয়া রেকর্ড" else "Track receivables and payables",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { viewModel.showAddDebtDialog.value = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Debt", tint = Color(0xFFEA580C), modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons specifically tailored for Receivable (Others Owe Me) vs Payable (I Owe Others)
        if (selectedTab == DebtType.OTHERS_OWE_ME) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.showAddNewLendingDialog.value = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "নতুন ধার দেওয়া" else "New Lending", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.showAddOldDebtDialog.value = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4338CA)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4338CA))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "পুরোনো পাওনা / শুরুর পাওনা" else "Old Debt / Opening", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.showAddDebtDialog.value = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "নতুন ঋণ গ্রহণ" else "New Borrowing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.showAddOldPayableDialog.value = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6D28D9)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF6D28D9))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "পুরোনো দেনা / শুরুর দেনা" else "Old Debt / Opening", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Direction Toggle Tabs: Others Owe Me vs I Owe Others
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                DebtType.OTHERS_OWE_ME to (if (isBangla) "অন্যের কাছে পাওনা (Receivable)" else "Others Owe Me"),
                DebtType.I_OWE_OTHERS to (if (isBangla) "আমার কাছে দেনা (Payable)" else "I Owe Others")
            )

            tabs.forEach { (tabType, label) ->
                val isSelected = selectedTab == tabType
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) (if (tabType == DebtType.OTHERS_OWE_ME) Color(0xFF10B981) else Color(0xFFEF4444)) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tabType }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val sum = if (tabType == DebtType.OTHERS_OWE_ME) metrics.othersOweMePaisa else metrics.iOweOthersPaisa
                        Text(
                            text = CurrencyFormatter.formatBdt(sum, isBangla),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Area Filter Chips (All / Personal / Business)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                FinanceArea.ALL to (if (isBangla) "সকল" else "All"),
                FinanceArea.PERSONAL to (if (isBangla) "ব্যক্তিগত" else "Personal"),
                FinanceArea.BUSINESS to (if (isBangla) "ব্যবসা" else "Business")
            ).forEach { (area, label) ->
                FilterChip(
                    selected = selectedArea == area,
                    onClick = { selectedArea = area },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedArea == area) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isBangla) "ব্যক্তির নাম বা ফোন নম্বর দিয়ে খুঁজুন..." else "Search person or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List
        if (displayDebts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBangla) "কোনো রেকর্ড পাওয়া যায়নি" else "No debts in this section",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayDebts, key = { it.id }) { debt ->
                    DebtCard(
                        debt = debt,
                        isBangla = isBangla,
                        onRepay = { viewModel.selectedDebtForRepayment.value = debt },
                        onDelete = { viewModel.requestMoveToTrash(debt) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DebtCard(
    debt: DebtEntity,
    isBangla: Boolean,
    onRepay: () -> Unit,
    onDelete: () -> Unit
) {
    val dtf = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val dateStr = Instant.ofEpochMilli(debt.dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)
    val dueDateStr = debt.dueDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)
    }

    val isPawna = debt.debtType == DebtType.OTHERS_OWE_ME.key
    val progress = if (debt.amountPaisa > 0) (debt.settledAmountPaisa.toFloat() / debt.amountPaisa.toFloat()).coerceIn(0f, 1f) else 0f
    val isFullyPaid = debt.status == DebtStatus.FULLY_SETTLED.key || debt.remainingAmountPaisa == 0L

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPawna) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isPawna) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = debt.personName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            // Area Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (debt.area == "BUSINESS") Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (debt.area == "BUSINESS") (if (isBangla) "ব্যবসা" else "Biz") else (if (isBangla) "ব্যক্তিগত" else "Personal"),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (debt.area == "BUSINESS") Color(0xFFD97706) else Color(0xFF2563EB)
                                )
                            }
                            if (debt.isOpening) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isBangla) "শুরুর পাওনা/দেনা" else "Opening",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4338CA)
                                    )
                                }
                            }
                        }
                        if (debt.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(debt.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isFullyPaid -> Color(0xFF10B981).copy(alpha = 0.15f)
                        debt.status == DebtStatus.PARTIALLY_PAID.key -> Color(0xFFD97706).copy(alpha = 0.15f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            isFullyPaid -> if (isBangla) "সম্পূর্ণ পরিশোধিত" else "Fully Settled"
                            debt.status == DebtStatus.PARTIALLY_PAID.key -> if (isBangla) "আংশিক পরিশোধ" else "Partially Paid"
                            else -> if (isBangla) "বাকি রয়েছে" else "Pending"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isFullyPaid -> Color(0xFF059669)
                            debt.status == DebtStatus.PARTIALLY_PAID.key -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isBangla) "মূল ঋণ:" else "Total Amount:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(debt.amountPaisa, isBangla), fontWeight = FontWeight.Bold)
                }

                Column {
                    Text(if (isBangla) "পরিশোধিত:" else "Settled:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(debt.settledAmountPaisa, isBangla), fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isBangla) "বর্তমান বাকি:" else "Current Remaining Balance:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(debt.remainingAmountPaisa, isBangla), fontWeight = FontWeight.Bold, color = if (isPawna) Color(0xFF10B981) else Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (isPawna) Color(0xFF10B981) else Color(0xFFEA580C),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isBangla) "তারিখ: " else "Date: "} $dateStr ${dueDateStr?.let { "• ${if (isBangla) "মেয়াদ: " else "Due: "} $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isFullyPaid) {
                        Button(
                            onClick = onRepay,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPawna) Color(0xFF10B981) else Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isPawna) (if (isBangla) "কিস্তি গ্রহণ (Repayment)" else "Repayment") else (if (isBangla) "কিস্তি পরিশোধ (Pay)" else "Pay"), fontSize = 12.sp)
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
