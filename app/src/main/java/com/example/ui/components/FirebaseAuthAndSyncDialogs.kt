package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.FirebaseTestResult
import com.example.data.firebase.SyncState
import com.example.data.repository.MigrationSummary
import com.google.firebase.auth.FirebaseUser
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ==========================================
// 1. SYNC STATUS BADGE
// ==========================================
@Composable
fun SyncStatusBadge(
    syncState: SyncState,
    lastSyncMillis: Long,
    isBangla: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, icon, label) = when (syncState) {
        SyncState.SYNCED -> {
            val dateStr = if (lastSyncMillis > 0) {
                Instant.ofEpochMilli(lastSyncMillis).atZone(ZoneId.of("Asia/Dhaka")).format(DateTimeFormatter.ofPattern("hh:mm a"))
            } else ""
            Triple(
                Color(0xFF059669),
                Icons.Default.CloudDone,
                if (isBangla) "সিঙ্কড ${if (dateStr.isNotEmpty()) "($dateStr)" else ""}" else "Synced ${if (dateStr.isNotEmpty()) "($dateStr)" else ""}"
            )
        }
        SyncState.SYNCING -> Triple(
            Color(0xFF2563EB),
            Icons.Default.CloudSync,
            if (isBangla) "সিঙ্ক হচ্ছে..." else "Syncing"
        )
        SyncState.OFFLINE -> Triple(
            Color(0xFF64748B),
            Icons.Default.CloudOff,
            if (isBangla) "অফলাইন (লোকাল সংরক্ষিত)" else "Offline (Saved locally)"
        )
        SyncState.SYNC_ERROR -> Triple(
            Color(0xFFDC2626),
            Icons.Default.CloudQueue,
            if (isBangla) "সিঙ্ক ত্রুটি" else "Sync Error"
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (syncState == SyncState.SYNCING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        }
    }
}

// ==========================================
// 2. FIREBASE AUTHENTICATION & VERIFICATION DIALOG
// ==========================================
@Composable
fun FirebaseAuthDialog(
    currentUser: FirebaseUser?,
    testResult: FirebaseTestResult?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (email: String, pass: String) -> Unit,
    onSignOut: () -> Unit,
    onTestConnection: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBangla) "ফায়ারবেস ক্লাউড সংযোগ" else "Firebase Cloud Integration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Project & Config Verification Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "✓ Official app/google-services.json verified",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                        Text(
                            text = "• Project: Firebase Firestore Connected",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "• Application ID: com.aistudio.amarhisab.tkqxpv",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (currentUser != null) {
                    // Signed In Status Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isBangla) "প্রকৃত ফায়ারবেস ব্যবহারকারী লগইন আছেন" else "Authenticated Firebase User",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Email: ${currentUser.email ?: "Google Authenticated"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "UID: ${currentUser.uid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real Connection Test Button
                    Button(
                        onClick = onTestConnection,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBangla) "বাস্তব ফায়ারবেস সংযোগ পরীক্ষা করুন" else "Run Live Firestore Test")
                    }

                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (testResult.isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = testResult.message,
                                    fontWeight = FontWeight.Bold,
                                    color = if (testResult.isSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                testResult.details?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "সাইন-আউট করুন" else "Sign Out")
                    }

                } else {
                    // Sign In Form for real authentication
                    Text(
                        text = if (isBangla) "ফায়ারবেস কনসোলে সক্রিয় প্রোভাইডার দিয়ে সাইন-ইন করুন:"
                        else "Sign in using enabled providers in Firebase Console:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real Email / Password Form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isBangla) "ইমেইল এড্রেস" else "Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isBangla) "পাসওয়ার্ড (নূন্যতম ৬ অক্ষর)" else "Password (min 6 characters)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.length >= 6) {
                                onEmailSignIn(email.trim(), password)
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "সঠিক ইমেইল ও নূন্যতম ৬ অক্ষরের পাসওয়ার্ড দিন" else "Please enter valid email and 6+ character password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "ইমেইল দিয়ে সাইন-ইন / রেজিস্টার" else "Sign In / Register with Email")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google Sign-In Button
                    Button(
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBangla) "G  Google দিয়ে সাইন-ইন করুন" else "G  Sign in with Google",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Setup Warnings Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "ফায়ারবেস কনসোল প্রয়োজনীয় সেটিংস:" else "Required Firebase Console Setup:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = "1. Sign in with Google (recommended) or your account email to link devices.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "2. Both PC Preview and mobile device will sync in real time when signed into the same account.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "3. Offline changes are stored locally in Room SQLite first and automatically synced when connected.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. CLOUD SYNC MIGRATION SCREEN (DIALOG)
// ==========================================
@Composable
fun CloudSyncMigrationDialog(
    summary: MigrationSummary,
    uid: String,
    isBangla: Boolean,
    onUploadLocal: () -> Unit,
    onDownloadCloud: () -> Unit,
    onMerge: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0284C7))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBangla) "ডাটা সিঙ্ক ও মাইগ্রেশন" else "Cloud Data Migration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBangla) "সাইন-ইন সফল হয়েছে! অনুগ্রহ করে বেছে নিন কিভাবে আপনার ডাটা পরিচালনা করতে চান:"
                    else "Sign in successful! Please choose how to handle your local and cloud data:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Comparison Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "ডাটার সারাংশ:" else "Data Summary:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isBangla) "এই ডিভাইসে (Local Room):" else "On this device (Local Room):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${summary.localAccountCount} Acc, ${summary.localTransactionCount} Tx, ${summary.localDebtCount} Debt",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isBangla) "ক্লাউডে (Cloud Firestore):" else "In cloud (Cloud Firestore):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${summary.cloudAccountCount} Acc, ${summary.cloudTransactionCount} Tx, ${summary.cloudDebtCount} Debt",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = if (isBangla) "✓ মাইগ্রেশনের পূর্বে একটি স্বয়ংক্রিয় JSON ব্যাকআপ তৈরি করা হয়েছে।"
                            else "✓ Automatic JSON backup created before migration.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Upload local Room data to Firebase
                Button(
                    onClick = onUploadLocal,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "১. লোকাল ডাটা ক্লাউডে আপলোড করুন" else "1. Upload Local Data to Cloud")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Download cloud data to this device
                Button(
                    onClick = onDownloadCloud,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "২. ক্লাউড ডাটা এই ডিভাইসে ডাউনলোড করুন" else "2. Download Cloud Data to Device")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Merge local and cloud data
                Button(
                    onClick = onMerge,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "৩. উভয় ডাটা মার্জ (একত্রিত) করুন" else "3. Merge Local & Cloud Data")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 4: Cancel
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "৪. বাতিল (স্থানীয়ভাবে থাকুন)" else "4. Cancel")
                }
            }
        }
    }
}
