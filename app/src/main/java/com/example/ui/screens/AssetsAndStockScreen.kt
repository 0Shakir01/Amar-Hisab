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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyFormatter
import com.example.ui.FinanceViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AssetStockTab {
    ASSETS,
    STOCK
}

@Composable
fun AssetsAndStockScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val assets by viewModel.filteredAssets.collectAsState()
    val stockItems by viewModel.filteredStock.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()

    var activeSubTab by remember { mutableStateOf(AssetStockTab.ASSETS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBangla) "সম্পদ ও স্টক ব্যবস্থাপনা" else "Assets & Business Stock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBangla) "স্থায়ী সম্পদ, কিস্তি ও পণ্যের মজুদ" else "Fixed assets, installments & inventory",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (activeSubTab == AssetStockTab.ASSETS) {
                        viewModel.showAddAssetDialog.value = true
                    } else {
                        viewModel.showAddStockDialog.value = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == AssetStockTab.ASSETS) Color(0xFF8B5CF6) else Color(0xFFD97706)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (activeSubTab == AssetStockTab.ASSETS) (if (isBangla) "নতুন সম্পদ" else "Add Asset")
                           else (if (isBangla) "স্টক ক্রয়" else "Buy Stock"),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub-Tab Switcher: Assets vs Business Stock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                AssetStockTab.ASSETS to (if (isBangla) "স্থায়ী সম্পদ ও কিস্তি (Assets)" else "Fixed Assets"),
                AssetStockTab.STOCK to (if (isBangla) "ব্যবসায়িক স্টক (Business Stock)" else "Business Stock")
            ).forEach { (tab, label) ->
                val isSelected = activeSubTab == tab
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) (if (tab == AssetStockTab.ASSETS) Color(0xFF8B5CF6) else Color(0xFFD97706)) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = tab }
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
                        val totalVal = if (tab == AssetStockTab.ASSETS) metrics.fixedAssetsValuePaisa else metrics.businessStockValuePaisa
                        Text(
                            text = CurrencyFormatter.formatBdt(totalVal, isBangla),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == AssetStockTab.ASSETS) {
            // Assets Tab View
            if (assets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "কোনো স্থায়ী সম্পদ সংরক্ষিত নেই। 'নতুন সম্পদ' বাটনে ক্লিক করে যোগ করুন।" else "No assets recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assets, key = { it.id }) { asset ->
                        AssetCard(
                            asset = asset,
                            isBangla = isBangla,
                            onPayInstallment = { viewModel.selectedAssetForInstallment.value = asset },
                            onDelete = { viewModel.requestMoveToTrash(asset) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } else {
            // Business Stock Tab View
            if (stockItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "কোনো স্টক নেই। 'স্টক ক্রয়' বাটনে ক্লিক করে পণ্য স্টক রেকর্ড করুন।" else "No stock items recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stockItems, key = { it.id }) { stock ->
                        StockCard(
                            stock = stock,
                            isBangla = isBangla,
                            onSell = { viewModel.selectedStockForSale.value = stock },
                            onDelete = { viewModel.requestMoveToTrash(stock) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AssetCard(
    asset: AssetEntity,
    isBangla: Boolean,
    onPayInstallment: () -> Unit,
    onDelete: () -> Unit
) {
    val dtf = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val dateStr = Instant.ofEpochMilli(asset.purchaseDateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)
    val nextDueStr = asset.nextPaymentDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)
    }

    val progress = if (asset.totalPurchaseValuePaisa > 0) (asset.paidAmountPaisa.toFloat() / asset.totalPurchaseValuePaisa.toFloat()).coerceIn(0f, 1f) else 1f
    val isFullyPaid = asset.status == "FULLY_PAID" || asset.remainingPaisa == 0L

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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(asset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (asset.area == "BUSINESS") Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (asset.area == "BUSINESS") (if (isBangla) "ব্যবসা" else "Biz") else (if (isBangla) "ব্যক্তিগত" else "Personal"),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (asset.area == "BUSINESS") Color(0xFFD97706) else Color(0xFF2563EB)
                                )
                            }
                        }
                        Text(asset.assetType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFullyPaid) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF8B5CF6).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isFullyPaid) (if (isBangla) "সম্পূর্ণ পরিশোধিত" else "Fully Paid") else (if (isBangla) "কিস্তি চলমান" else "Active Installment"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullyPaid) Color(0xFF059669) else Color(0xFF7C3AED)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (isBangla) "মোট মূল্য:" else "Total Cost:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(asset.totalPurchaseValuePaisa, isBangla), fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(if (isBangla) "পরিশোধিত:" else "Paid:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(asset.paidAmountPaisa, isBangla), fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isBangla) "অবশিষ্ট বাকি:" else "Remaining:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(asset.remainingPaisa, isBangla), fontWeight = FontWeight.Bold, color = if (isFullyPaid) Color(0xFF059669) else Color(0xFF7C3AED))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF8B5CF6),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isBangla) "ক্রয়: " else "Bought: "} $dateStr ${nextDueStr?.let { "• ${if (isBangla) "পরবর্তী কিস্তি: " else "Due: "} $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isFullyPaid) {
                        Button(
                            onClick = onPayInstallment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isBangla) "কিস্তি প্রদান" else "Pay Installment", fontSize = 12.sp)
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

@Composable
fun StockCard(
    stock: StockItemEntity,
    isBangla: Boolean,
    onSell: () -> Unit,
    onDelete: () -> Unit
) {
    val dtf = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val dateStr = Instant.ofEpochMilli(stock.dateMillis).atZone(ZoneId.of("Asia/Dhaka")).format(dtf)

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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stock.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isBangla) "ব্যবসা স্টক" else "Stock",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                        Text(
                            text = "${if (isBangla) "একক ক্রয়মূল্য: " else "Unit Cost: "}${CurrencyFormatter.formatBdt(stock.purchaseCostPerUnitPaisa, isBangla)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stock.remainingQuantity > 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (isBangla) "মজুদ: " else "Stock: "} ${stock.remainingQuantity} টি",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.remainingQuantity > 0) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (isBangla) "মোট ক্রয়:" else "Purchased:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${stock.purchasedQuantity} টি", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(if (isBangla) "বিক্রয়কৃত:" else "Sold:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${stock.soldQuantity} টি", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isBangla) "বর্তমান স্টক সম্পদ:" else "Stock Value:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatBdt(stock.currentStockValuePaisa, isBangla), fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isBangla) "তারিখ: " else "Date: "} $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (stock.remainingQuantity > 0) {
                        Button(
                            onClick = onSell,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "বিক্রয় করুন" else "Sell", fontSize = 12.sp)
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
