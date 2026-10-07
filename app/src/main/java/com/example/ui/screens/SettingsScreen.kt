package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.FinanceViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isBangla = language == AppLanguage.BANGLA
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showFirestoreRulesDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pasteJsonText by remember { mutableStateOf("") }
    var exportedJsonText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBangla) "সেটিংস ও ব্যাকআপ" else "Settings & Backup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isBangla) "অ্যাপ কনফিগারেশন, ডাটা নিরাপত্তা ও ক্লাউড সিঙ্ক" else "App configuration, data safety and cloud sync",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Language Toggle Card
        item {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isBangla) "অ্যাপের ভাষা" else "App Language", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isBangla) "বাংলা (সক্রিয়)" else "English (Active)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isBangla) "Switch to English" else "বাংলায় পরিবর্তন")
                    }
                }
            }
        }

        // 2. Opening Balance Setup Card
        item {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF059669))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isBangla) "প্রারম্ভিক ব্যালেন্স সেটআপ" else "Opening Balance Setup", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isBangla) "হিসাব শুরুর নগদ ও ব্যাংক ব্যালেন্স" else "Configure starting balances",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.showOpeningBalanceSetup.value = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isBangla) "নির্ধারণ করুন" else "Configure")
                    }
                }
            }
        }

        // 3. Cloud Firestore & Security Rules
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFFF59E0B))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(if (isBangla) "ক্লাউড ফায়ারস্টোর ডাটাবেস" else "Cloud Firestore Database", fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isBangla) "ডাটা স্থায়ীভাবে সুরক্ষিত ও সিঙ্কড" else "Permanent storage & multi-device sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBangla) "ডাটাবেসে কোন স্বয়ংক্রিয় মুছে ফেলা (TTL) বা রিসেট লজিক নেই। সমস্ত রেকর্ড স্থায়ী এবং সুরক্ষায় আবদ্ধ।"
                        else "No automatic deletion, TTL, or cleanup logic exists. Financial records are permanent and protected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showAuthDialog.value = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "ক্লাউড সিঙ্ক অ্যাকাউন্ট" else "Sync Account")
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerSync() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isBangla) "এখনই সিঙ্ক করুন" else "Sync Now")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showFirestoreRulesDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "ফায়ারস্টোর সিকিউরিটি রুলস দেখুন" else "View Firestore Security Rules")
                    }
                }
            }
        }

        // 4. Export & Import Backup
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isBangla) "ব্যাকআপ ও রিস্টোর (Export / Import)" else "Backup & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBangla) "সম্পূর্ণ ডাটাবেস JSON বা CSV ফরম্যাটে এক্সপোর্ট করুন এবং যে কোন সময় প্রিভিউ দেখে রিস্টোর করুন।"
                        else "Export database to JSON/CSV or restore from backup with validation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.getExportJson()
                                    exportedJsonText = json
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("AmarHisab Backup", json))
                                    Toast.makeText(context, if (isBangla) "JSON ব্যাকআপ ক্লিপবোর্ডে কপি হয়েছে" else "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "JSON ব্যাকআপ" else "Export JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val csv = viewModel.getExportCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("AmarHisab Ledger CSV", csv))
                                    Toast.makeText(context, if (isBangla) "CSV লেজার ক্লিপবোর্ডে কপি হয়েছে" else "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "CSV এক্সপোর্ট" else "Export CSV")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "ব্যাকআপ ফাইল ইমপোর্ট ও রিস্টোর" else "Import & Restore Backup")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Firestore Rules Dialog
    if (showFirestoreRulesDialog) {
        val rulesText = remember { viewModel.repository.getFirestoreSecurityRules() }
        AlertDialog(
            onDismissRequest = { showFirestoreRulesDialog = false },
            title = { Text(if (isBangla) "ফায়ারস্টোর সিকিউরিটি রুলস" else "Firestore Security Rules") },
            text = {
                Column {
                    Text(
                        if (isBangla) "নিয়ম: শুধুমাত্র প্রমাণীকৃত মালিক নিজের ডাটা দেখতে ও লিখতে পারবেন। পাসওয়ার্ড বা গোপন তথ্য সংরক্ষণ নিষিদ্ধ।"
                        else "Rules: Only the authenticated owner can access their records. Sensitive bank credentials prohibited.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rulesText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Firestore Rules", rulesText))
                    Toast.makeText(context, if (isBangla) "রুলস কপি করা হয়েছে" else "Rules copied", Toast.LENGTH_SHORT).show()
                    showFirestoreRulesDialog = false
                }) {
                    Text(if (isBangla) "রুলস কপি করুন" else "Copy Rules")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFirestoreRulesDialog = false }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(if (isBangla) "ব্যাকআপ JSON প্রবেশ করান" else "Paste Backup JSON") },
            text = {
                Column {
                    Text(
                        if (isBangla) "পূর্ববর্তী JSON ব্যাকআপ এখানে পেস্ট করুন। সিস্টেমে প্রয়োগের পূর্বে প্রিভিউ দেখানো হবে:"
                        else "Paste previous JSON backup content below. A preview will be shown before restoring:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pasteJsonText,
                        onValueChange = { pasteJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\n  \"app\": \"Amar Hisab\",\n  ...\n}") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pasteJsonText.isNotBlank()) {
                        viewModel.previewBackupJson(pasteJsonText)
                        showImportDialog = false
                    }
                }) {
                    Text(if (isBangla) "প্রিভিউ দেখুন" else "Preview & Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
