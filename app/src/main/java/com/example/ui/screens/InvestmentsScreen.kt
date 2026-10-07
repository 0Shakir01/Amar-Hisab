package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.InvestmentEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.data.model.InvestmentType
import com.example.ui.FinanceViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InvestmentsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val investments by viewModel.investments.collectAsState()

    val totalPaid = investments.sumOf { it.paidAmountPaisa }
    val totalRemaining = investments.sumOf { it.remainingPaisa }

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
                        text = if (isBangla) "বিনিয়োগ হিসাব (Investments)" else "Investments",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBangla) "প্লট, হোটেল শেয়ার ও দীর্ঘমেয়াদী সম্পদ" else "Plot installments, hotel shares & capital assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { viewModel.showAddAssetDialog.value = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "যোগ করুন" else "Add")
                }
            }
        }

        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isBangla) "পরিশোধিত বিনিয়োগ সম্পদ (Asset)" else "Total Paid Asset Value", style = MaterialTheme.typography.labelSmall)
                        Text(
                            CurrencyFormatter.formatBdt(totalPaid, useBanglaDigits = isBangla),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isBangla) "বাকি কিস্তি (Remaining)" else "Remaining Installments", style = MaterialTheme.typography.labelSmall)
                        Text(
                            CurrencyFormatter.formatBdt(totalRemaining, useBanglaDigits = isBangla),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        if (investments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBangla) "এখনও কোন বিনিয়োগ যুক্ত করা হয়নি" else "No investments added yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(investments) { inv ->
                InvestmentCard(
                    investment = inv,
                    isBangla = isBangla,
                    onPayInstallment = { viewModel.selectedInvestmentForPayment.value = inv },
                    onDelete = { viewModel.requestMoveToTrash(inv) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InvestmentCard(
    investment: InvestmentEntity,
    isBangla: Boolean,
    onPayInstallment: () -> Unit,
    onDelete: () -> Unit
) {
    val invType = InvestmentType.fromKey(investment.investmentType)
    val nextDateStr = remember(investment.nextPaymentDateMillis) {
        investment.nextPaymentDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = investment.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isBangla) invType.titleBn else invType.titleEn,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (isBangla) "পরিশোধিত (Asset)" else "Paid So Far", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(investment.paidAmountPaisa, useBanglaDigits = isBangla), fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
                if (investment.totalAgreedPaisa > 0) {
                    Column {
                        Text(if (isBangla) "নির্ধারিত মূল্য" else "Agreed Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.formatBdt(investment.totalAgreedPaisa, useBanglaDigits = isBangla), fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isBangla) "বাকি কিস্তি" else "Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.formatBdt(investment.remainingPaisa, useBanglaDigits = isBangla), fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                    }
                }
            }

            if (investment.installmentPaisa > 0 || nextDateStr != null || investment.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (investment.installmentPaisa > 0) {
                    Text(
                        text = "${if (isBangla) "প্রতি কিস্তি:" else "Installment:"} ${CurrencyFormatter.formatBdt(investment.installmentPaisa, useBanglaDigits = isBangla)}" +
                                if (nextDateStr != null) " | ${if (isBangla) "পরবর্তী তারিখ:" else "Next Date:"} $nextDateStr" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (investment.note.isNotBlank()) {
                    Text(
                        text = investment.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Move to Trash", tint = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = onPayInstallment,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isBangla) "কিস্তি পরিশোধ রেকর্ড" else "Pay Installment", color = Color.White)
                }
            }
        }
    }
}
