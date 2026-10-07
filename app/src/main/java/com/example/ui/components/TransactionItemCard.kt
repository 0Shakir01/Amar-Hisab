package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CurrencyFormatter
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    sourceAccount: AccountEntity?,
    destinationAccount: AccountEntity?,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isBangla = language == AppLanguage.BANGLA

    val type = try {
        TransactionType.valueOf(transaction.type)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    val amountColor = when (type) {
        TransactionType.INCOME, TransactionType.OWNER_CAPITAL, TransactionType.STOCK_SALE, TransactionType.DEBT_REPAYMENT, TransactionType.DEBT_BORROW -> IncomeGreen
        TransactionType.EXPENSE, TransactionType.OWNER_WITHDRAWAL, TransactionType.STOCK_PURCHASE, TransactionType.ASSET_PURCHASE, TransactionType.ASSET_INSTALLMENT, TransactionType.DEBT_LENDING, TransactionType.DEBT_PAYMENT -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }

    val iconVector = when (type) {
        TransactionType.TRANSFER -> Icons.AutoMirrored.Filled.CompareArrows
        else -> CategoryIconHelper.getCategoryIcon(category?.iconName ?: "category")
    }

    val iconBgColor = when (type) {
        TransactionType.TRANSFER -> TransferBlue.copy(alpha = 0.15f)
        else -> CategoryIconHelper.parseColor(category?.colorHex ?: "#10B981").copy(alpha = 0.15f)
    }

    val iconTintColor = when (type) {
        TransactionType.TRANSFER -> TransferBlue
        else -> CategoryIconHelper.parseColor(category?.colorHex ?: "#10B981")
    }

    val titleText = when (type) {
        TransactionType.TRANSFER -> {
            val fromName = sourceAccount?.name ?: "Account"
            val toName = destinationAccount?.name ?: "Account"
            "$fromName ➔ $toName"
        }
        else -> {
            if (transaction.sourceDetails.isNotBlank()) {
                transaction.sourceDetails
            } else if (isBangla) {
                category?.nameBn ?: category?.nameEn ?: transaction.category.ifBlank { AppStrings.get("other", language) }
            } else {
                category?.nameEn ?: category?.nameBn ?: transaction.category.ifBlank { AppStrings.get("other", language) }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = titleText,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Recurring",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Account badge and date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (type != TransactionType.TRANSFER && sourceAccount != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CategoryIconHelper.getAccountColor(sourceAccount.type).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = sourceAccount.name,
                                color = CategoryIconHelper.getAccountColor(sourceAccount.type),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = CurrencyFormatter.formatDate(transaction.dateMillis, isBangla = isBangla),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                val formattedAmount = CurrencyFormatter.formatBdt(
                    paisa = transaction.amountPaisa,
                    isBangla = isBangla,
                    includeSign = false
                )
                val signPrefix = when (type) {
                    TransactionType.INCOME, TransactionType.OWNER_CAPITAL, TransactionType.STOCK_SALE, TransactionType.DEBT_REPAYMENT, TransactionType.DEBT_BORROW -> "+"
                    TransactionType.EXPENSE, TransactionType.OWNER_WITHDRAWAL, TransactionType.STOCK_PURCHASE, TransactionType.ASSET_PURCHASE, TransactionType.ASSET_INSTALLMENT, TransactionType.DEBT_LENDING, TransactionType.DEBT_PAYMENT -> "-"
                    TransactionType.TRANSFER -> "⇄ "
                }

                Text(
                    text = "$signPrefix$formattedAmount",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )

                // Menu Trigger
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(AppStrings.get("edit", language)) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(AppStrings.get("duplicate", language)) },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(AppStrings.get("delete", language), color = ExpenseRed) },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ExpenseRed)
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
