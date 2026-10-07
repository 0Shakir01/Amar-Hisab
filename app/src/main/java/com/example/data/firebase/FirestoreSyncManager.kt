package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.InstallmentEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.StockItemEntity
import com.example.data.local.entity.TransactionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class SyncState {
    OFFLINE,
    SYNCING,
    SYNCED,
    SYNC_ERROR
}

data class CloudSummary(
    val accountCount: Int = 0,
    val transactionCount: Int = 0,
    val debtCount: Int = 0,
    val assetCount: Int = 0,
    val stockCount: Int = 0,
    val installmentCount: Int = 0,
    val investmentCount: Int = 0,
    val lastUpdatedMillis: Long = 0L
)

data class CloudDataPackage(
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val assets: List<AssetEntity> = emptyList(),
    val stock: List<StockItemEntity> = emptyList(),
    val installments: List<InstallmentEntity> = emptyList(),
    val investments: List<InvestmentEntity> = emptyList()
)

data class FirebaseTestResult(
    val isSuccess: Boolean,
    val message: String,
    val details: String? = null
)

class FirestoreSyncManager(private val context: Context) {

    private val TAG = "FirestoreSyncManager"

    private val _syncState = MutableStateFlow(SyncState.OFFLINE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val dbId = try {
                    context.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    null
                }
                val db = if (!dbId.isNullOrBlank()) {
                    FirebaseFirestore.getInstance(dbId)
                } else {
                    FirebaseFirestore.getInstance()
                }
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
                db
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not yet initialized: ${e.message}")
            null
        }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isFirebaseConnected(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && firestore != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun verifyRealFirestoreConnection(uid: String): FirebaseTestResult = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            _syncState.value = SyncState.OFFLINE
            return@withContext FirebaseTestResult(
                isSuccess = false,
                message = "Device Offline",
                details = "No active internet connection. Room SQLite offline mode is active."
            )
        }
        val db = firestore ?: return@withContext FirebaseTestResult(
            isSuccess = false,
            message = "Firebase Not Initialized",
            details = "google-services.json or FirebaseApp could not initialize."
        )
        if (uid.isEmpty() || uid == "local_user") {
            _syncState.value = SyncState.OFFLINE
            return@withContext FirebaseTestResult(
                isSuccess = false,
                message = "Authentication Required",
                details = "A real authenticated user is required. Please sign in with an enabled Firebase provider."
            )
        }

        try {
            _syncState.value = SyncState.SYNCING
            val profileDoc = db.collection("users").document(uid).collection("settings").document("profile")
            val now = System.currentTimeMillis()
            val verifyData = hashMapOf(
                "ownerUid" to uid,
                "createdAt" to now,
                "updatedAt" to now,
                "isDeleted" to false,
                "syncStatus" to "SYNCED",
                "lastVerifiedAt" to now
            )
            // Real write to Firestore
            profileDoc.set(verifyData, SetOptions.merge()).await()

            // Real read from Firestore
            val readSnap = profileDoc.get().await()
            if (readSnap.exists() && readSnap.getString("ownerUid") == uid) {
                _syncState.value = SyncState.SYNCED
                _lastSyncTimestamp.value = now
                FirebaseTestResult(
                    isSuccess = true,
                    message = "Firebase Connected & Verified",
                    details = "Real read and write succeeded on /users/$uid/settings/profile in cloud Firestore."
                )
            } else {
                _syncState.value = SyncState.SYNC_ERROR
                FirebaseTestResult(
                    isSuccess = false,
                    message = "Verification Incomplete",
                    details = "Write call completed but read snapshot did not return expected user document."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyRealFirestoreConnection error: ${e.message}", e)
            _syncState.value = SyncState.SYNC_ERROR
            val rawMsg = e.message ?: "Unknown Firestore error"
            val hint = when {
                rawMsg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Firestore Security Rules rejected write access. Please publish rules allowing /users/{uid}/{document=**}."
                rawMsg.contains("UNAUTHENTICATED", ignoreCase = true) ->
                    "User authentication expired or token is invalid."
                rawMsg.contains("UNAVAILABLE", ignoreCase = true) ->
                    "Firestore service is currently unavailable or network is blocked."
                else -> rawMsg
            }
            FirebaseTestResult(
                isSuccess = false,
                message = "Firestore Test Failed",
                details = hint
            )
        }
    }

    suspend fun getCloudSummary(uid: String): CloudSummary = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext CloudSummary()
        try {
            val userDoc = db.collection("users").document(uid)
            val accountsSnap = userDoc.collection("accounts").get().await()
            val txSnap = userDoc.collection("transactions").get().await()
            val debtsSnap = userDoc.collection("debts").get().await()
            val assetsSnap = userDoc.collection("assets").get().await()
            val stockSnap = userDoc.collection("stock").get().await()
            val installmentsSnap = userDoc.collection("installments").get().await()
            val invSnap = userDoc.collection("investments").get().await()

            var latestTime = 0L
            listOf(txSnap, assetsSnap, stockSnap).forEach { snap ->
                snap.documents.forEach { doc ->
                    val time = doc.getLong("updatedAt") ?: 0L
                    if (time > latestTime) latestTime = time
                }
            }

            CloudSummary(
                accountCount = accountsSnap.size(),
                transactionCount = txSnap.size(),
                debtCount = debtsSnap.size(),
                assetCount = assetsSnap.size(),
                stockCount = stockSnap.size(),
                installmentCount = installmentsSnap.size(),
                investmentCount = invSnap.size(),
                lastUpdatedMillis = latestTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching cloud summary: ${e.message}")
            CloudSummary()
        }
    }

    suspend fun uploadAllLocalData(
        uid: String,
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>,
        debts: List<DebtEntity>,
        assets: List<AssetEntity>,
        stock: List<StockItemEntity>,
        installments: List<InstallmentEntity>,
        investments: List<InvestmentEntity> = emptyList()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not available"))
        _syncState.value = SyncState.SYNCING
        try {
            val userDoc = db.collection("users").document(uid)

            // Settings/profile
            val profileNow = System.currentTimeMillis()
            userDoc.collection("settings").document("profile").set(
                hashMapOf(
                    "ownerUid" to uid,
                    "createdAt" to profileNow,
                    "updatedAt" to profileNow,
                    "isDeleted" to false,
                    "syncStatus" to "SYNCED"
                ),
                SetOptions.merge()
            ).await()

            // Accounts
            for (acc in accounts) {
                userDoc.collection("accounts").document(acc.id.toString()).set(
                    hashMapOf(
                        "id" to acc.id,
                        "ownerUid" to uid,
                        "name" to acc.name,
                        "type" to acc.type,
                        "area" to acc.area,
                        "openingBalancePaisa" to acc.openingBalancePaisa,
                        "isActive" to acc.isActive,
                        "colorHex" to acc.colorHex,
                        "createdAt" to acc.createdAt,
                        "updatedAt" to acc.updatedAt,
                        "isDeleted" to acc.isDeleted,
                        "deletedAtMillis" to acc.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Transactions
            for (tx in transactions) {
                userDoc.collection("transactions").document(tx.id.toString()).set(
                    hashMapOf(
                        "id" to tx.id,
                        "ownerUid" to uid,
                        "type" to tx.type,
                        "amountPaisa" to tx.amountPaisa,
                        "accountId" to tx.accountId,
                        "destinationAccountId" to tx.destinationAccountId,
                        "dateMillis" to tx.dateMillis,
                        "area" to tx.area,
                        "category" to tx.category,
                        "sourceDetails" to tx.sourceDetails,
                        "paidTo" to tx.paidTo,
                        "note" to tx.note,
                        "createdAt" to tx.createdAt,
                        "updatedAt" to tx.updatedAt,
                        "isDeleted" to tx.isDeleted,
                        "deletedAtMillis" to tx.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Debts
            for (debt in debts) {
                userDoc.collection("debts").document(debt.id.toString()).set(
                    hashMapOf(
                        "id" to debt.id,
                        "ownerUid" to uid,
                        "personName" to debt.personName,
                        "phone" to debt.phone,
                        "debtType" to debt.debtType,
                        "area" to debt.area,
                        "amountPaisa" to debt.amountPaisa,
                        "dateMillis" to debt.dateMillis,
                        "dueDateMillis" to debt.dueDateMillis,
                        "accountId" to debt.accountId,
                        "settledAmountPaisa" to debt.settledAmountPaisa,
                        "status" to debt.status,
                        "note" to debt.note,
                        "createdAt" to debt.createdAt,
                        "updatedAt" to debt.updatedAt,
                        "isDeleted" to debt.isDeleted,
                        "deletedAtMillis" to debt.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Assets
            for (asset in assets) {
                userDoc.collection("assets").document(asset.id.toString()).set(
                    hashMapOf(
                        "id" to asset.id,
                        "ownerUid" to uid,
                        "name" to asset.name,
                        "assetType" to asset.assetType,
                        "area" to asset.area,
                        "totalPurchaseValuePaisa" to asset.totalPurchaseValuePaisa,
                        "paidAmountPaisa" to asset.paidAmountPaisa,
                        "currentValuePaisa" to asset.currentValuePaisa,
                        "purchaseDateMillis" to asset.purchaseDateMillis,
                        "accountId" to asset.accountId,
                        "installmentPaisa" to asset.installmentPaisa,
                        "nextPaymentDateMillis" to asset.nextPaymentDateMillis,
                        "status" to asset.status,
                        "note" to asset.note,
                        "createdAt" to asset.createdAt,
                        "updatedAt" to asset.updatedAt,
                        "isDeleted" to asset.isDeleted,
                        "deletedAtMillis" to asset.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Stock
            for (st in stock) {
                userDoc.collection("stock").document(st.id.toString()).set(
                    hashMapOf(
                        "id" to st.id,
                        "ownerUid" to uid,
                        "productName" to st.productName,
                        "area" to st.area,
                        "purchaseCostPerUnitPaisa" to st.purchaseCostPerUnitPaisa,
                        "purchasedQuantity" to st.purchasedQuantity,
                        "soldQuantity" to st.soldQuantity,
                        "accountId" to st.accountId,
                        "dateMillis" to st.dateMillis,
                        "note" to st.note,
                        "createdAt" to st.createdAt,
                        "updatedAt" to st.updatedAt,
                        "isDeleted" to st.isDeleted,
                        "deletedAtMillis" to st.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Installments
            for (inst in installments) {
                userDoc.collection("installments").document(inst.id.toString()).set(
                    hashMapOf(
                        "id" to inst.id,
                        "ownerUid" to uid,
                        "assetId" to inst.assetId,
                        "amountPaisa" to inst.amountPaisa,
                        "accountId" to inst.accountId,
                        "dateMillis" to inst.dateMillis,
                        "note" to inst.note,
                        "createdAt" to inst.createdAt,
                        "updatedAt" to inst.updatedAt,
                        "isDeleted" to inst.isDeleted,
                        "deletedAtMillis" to inst.deletedAtMillis,
                        "syncStatus" to "SYNCED"
                    ),
                    SetOptions.merge()
                ).await()
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
            Result.failure(e)
        }
    }

    suspend fun downloadCloudData(uid: String): Result<CloudDataPackage> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not available"))
        _syncState.value = SyncState.SYNCING
        try {
            val userDoc = db.collection("users").document(uid)

            val accDocs = userDoc.collection("accounts").get().await()
            val accounts = accDocs.documents.map { doc ->
                AccountEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    name = doc.getString("name") ?: "",
                    type = doc.getString("type") ?: "BANK",
                    area = doc.getString("area") ?: "PERSONAL",
                    openingBalancePaisa = doc.getLong("openingBalancePaisa") ?: 0L,
                    isActive = doc.getBoolean("isActive") ?: true,
                    colorHex = doc.getString("colorHex") ?: "#059669",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            val txDocs = userDoc.collection("transactions").get().await()
            val transactions = txDocs.documents.map { doc ->
                TransactionEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    type = doc.getString("type") ?: "EXPENSE",
                    amountPaisa = doc.getLong("amountPaisa") ?: 0L,
                    accountId = doc.getLong("accountId") ?: 1L,
                    destinationAccountId = doc.getLong("destinationAccountId"),
                    dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                    area = doc.getString("area") ?: "PERSONAL",
                    category = doc.getString("category") ?: "",
                    sourceDetails = doc.getString("sourceDetails") ?: "",
                    paidTo = doc.getString("paidTo") ?: "",
                    note = doc.getString("note") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            val debtDocs = userDoc.collection("debts").get().await()
            val debts = debtDocs.documents.map { doc ->
                DebtEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    personName = doc.getString("personName") ?: "",
                    phone = doc.getString("phone") ?: "",
                    debtType = doc.getString("debtType") ?: "OTHERS_OWE_ME",
                    area = doc.getString("area") ?: "PERSONAL",
                    amountPaisa = doc.getLong("amountPaisa") ?: 0L,
                    dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                    dueDateMillis = doc.getLong("dueDateMillis"),
                    accountId = doc.getLong("accountId") ?: 1L,
                    settledAmountPaisa = doc.getLong("settledAmountPaisa") ?: 0L,
                    status = doc.getString("status") ?: "PENDING",
                    note = doc.getString("note") ?: "",
                    isOpening = doc.getBoolean("isOpening") ?: false,
                    originalDateMillis = doc.getLong("originalDateMillis"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            val assetDocs = userDoc.collection("assets").get().await()
            val assets = assetDocs.documents.map { doc ->
                AssetEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    name = doc.getString("name") ?: "",
                    assetType = doc.getString("assetType") ?: "Other",
                    area = doc.getString("area") ?: "PERSONAL",
                    totalPurchaseValuePaisa = doc.getLong("totalPurchaseValuePaisa") ?: 0L,
                    paidAmountPaisa = doc.getLong("paidAmountPaisa") ?: 0L,
                    currentValuePaisa = doc.getLong("currentValuePaisa") ?: 0L,
                    purchaseDateMillis = doc.getLong("purchaseDateMillis") ?: System.currentTimeMillis(),
                    accountId = doc.getLong("accountId") ?: 1L,
                    installmentPaisa = doc.getLong("installmentPaisa") ?: 0L,
                    nextPaymentDateMillis = doc.getLong("nextPaymentDateMillis"),
                    status = doc.getString("status") ?: "ACTIVE",
                    note = doc.getString("note") ?: "",
                    isOpening = doc.getBoolean("isOpening") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            val stockDocs = userDoc.collection("stock").get().await()
            val stock = stockDocs.documents.map { doc ->
                StockItemEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    productName = doc.getString("productName") ?: "",
                    area = doc.getString("area") ?: "BUSINESS",
                    purchaseCostPerUnitPaisa = doc.getLong("purchaseCostPerUnitPaisa") ?: 0L,
                    purchasedQuantity = doc.getDouble("purchasedQuantity") ?: 0.0,
                    soldQuantity = doc.getDouble("soldQuantity") ?: 0.0,
                    accountId = doc.getLong("accountId") ?: 1L,
                    dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                    note = doc.getString("note") ?: "",
                    isOpening = doc.getBoolean("isOpening") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            val installmentDocs = userDoc.collection("installments").get().await()
            val installments = installmentDocs.documents.map { doc ->
                InstallmentEntity(
                    id = doc.getLong("id") ?: 0L,
                    userId = uid,
                    assetId = doc.getLong("assetId") ?: 0L,
                    amountPaisa = doc.getLong("amountPaisa") ?: 0L,
                    accountId = doc.getLong("accountId") ?: 1L,
                    dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                    note = doc.getString("note") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletedAtMillis = doc.getLong("deletedAtMillis")
                )
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
            Result.success(
                CloudDataPackage(
                    accounts = accounts,
                    transactions = transactions,
                    debts = debts,
                    assets = assets,
                    stock = stock,
                    installments = installments
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
            Result.failure(e)
        }
    }

    suspend fun syncSingleAccount(uid: String, account: AccountEntity) = withContext(Dispatchers.IO) {
        if (uid.isBlank() || uid == "local_user") return@withContext
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to account.id,
                "ownerUid" to uid,
                "name" to account.name,
                "type" to account.type,
                "area" to account.area,
                "openingBalancePaisa" to account.openingBalancePaisa,
                "isActive" to account.isActive,
                "colorHex" to account.colorHex,
                "createdAt" to account.createdAt,
                "updatedAt" to account.updatedAt,
                "isDeleted" to account.isDeleted,
                "deletedAtMillis" to account.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("accounts").document(account.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Account sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleTransaction(uid: String, transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        if (uid.isBlank() || uid == "local_user") return@withContext
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to transaction.id,
                "ownerUid" to uid,
                "type" to transaction.type,
                "amountPaisa" to transaction.amountPaisa,
                "accountId" to transaction.accountId,
                "destinationAccountId" to transaction.destinationAccountId,
                "dateMillis" to transaction.dateMillis,
                "area" to transaction.area,
                "category" to transaction.category,
                "sourceDetails" to transaction.sourceDetails,
                "paidTo" to transaction.paidTo,
                "note" to transaction.note,
                "createdAt" to transaction.createdAt,
                "updatedAt" to transaction.updatedAt,
                "isDeleted" to transaction.isDeleted,
                "deletedAtMillis" to transaction.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("transactions").document(transaction.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Transaction sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleDebt(uid: String, debt: DebtEntity) = withContext(Dispatchers.IO) {
        if (uid.isBlank() || uid == "local_user") return@withContext
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to debt.id,
                "ownerUid" to uid,
                "personName" to debt.personName,
                "phone" to debt.phone,
                "debtType" to debt.debtType,
                "area" to debt.area,
                "amountPaisa" to debt.amountPaisa,
                "dateMillis" to debt.dateMillis,
                "dueDateMillis" to debt.dueDateMillis,
                "accountId" to debt.accountId,
                "settledAmountPaisa" to debt.settledAmountPaisa,
                "status" to debt.status,
                "note" to debt.note,
                "isOpening" to debt.isOpening,
                "originalDateMillis" to debt.originalDateMillis,
                "createdAt" to debt.createdAt,
                "updatedAt" to debt.updatedAt,
                "isDeleted" to debt.isDeleted,
                "deletedAtMillis" to debt.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("debts").document(debt.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Debt sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleAsset(uid: String, asset: AssetEntity) = withContext(Dispatchers.IO) {
        if (uid.isBlank() || uid == "local_user") return@withContext
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to asset.id,
                "ownerUid" to uid,
                "name" to asset.name,
                "assetType" to asset.assetType,
                "area" to asset.area,
                "totalPurchaseValuePaisa" to asset.totalPurchaseValuePaisa,
                "paidAmountPaisa" to asset.paidAmountPaisa,
                "currentValuePaisa" to asset.currentValuePaisa,
                "purchaseDateMillis" to asset.purchaseDateMillis,
                "accountId" to asset.accountId,
                "installmentPaisa" to asset.installmentPaisa,
                "nextPaymentDateMillis" to asset.nextPaymentDateMillis,
                "status" to asset.status,
                "note" to asset.note,
                "isOpening" to asset.isOpening,
                "createdAt" to asset.createdAt,
                "updatedAt" to asset.updatedAt,
                "isDeleted" to asset.isDeleted,
                "deletedAtMillis" to asset.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("assets").document(asset.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Asset sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleStock(uid: String, stock: StockItemEntity) = withContext(Dispatchers.IO) {
        if (uid.isBlank() || uid == "local_user") return@withContext
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to stock.id,
                "ownerUid" to uid,
                "productName" to stock.productName,
                "area" to stock.area,
                "purchaseCostPerUnitPaisa" to stock.purchaseCostPerUnitPaisa,
                "purchasedQuantity" to stock.purchasedQuantity,
                "soldQuantity" to stock.soldQuantity,
                "accountId" to stock.accountId,
                "dateMillis" to stock.dateMillis,
                "note" to stock.note,
                "isOpening" to stock.isOpening,
                "createdAt" to stock.createdAt,
                "updatedAt" to stock.updatedAt,
                "isDeleted" to stock.isDeleted,
                "deletedAtMillis" to stock.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("stock").document(stock.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Stock sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleInstallment(uid: String, installment: InstallmentEntity) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to installment.id,
                "ownerUid" to uid,
                "assetId" to installment.assetId,
                "amountPaisa" to installment.amountPaisa,
                "accountId" to installment.accountId,
                "dateMillis" to installment.dateMillis,
                "note" to installment.note,
                "createdAt" to installment.createdAt,
                "updatedAt" to installment.updatedAt,
                "isDeleted" to installment.isDeleted,
                "deletedAtMillis" to installment.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("installments").document(installment.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Installment sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }

    suspend fun syncSingleInvestment(uid: String, investment: InvestmentEntity) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val docData = hashMapOf(
                "id" to investment.id,
                "ownerUid" to uid,
                "projectName" to investment.projectName,
                "investmentType" to investment.investmentType,
                "totalAgreedPaisa" to investment.totalAgreedPaisa,
                "paidAmountPaisa" to investment.paidAmountPaisa,
                "installmentPaisa" to investment.installmentPaisa,
                "nextPaymentDateMillis" to investment.nextPaymentDateMillis,
                "accountId" to investment.accountId,
                "note" to investment.note,
                "createdAt" to investment.createdAt,
                "updatedAt" to investment.updatedAt,
                "isDeleted" to investment.isDeleted,
                "deletedAtMillis" to investment.deletedAtMillis,
                "syncStatus" to "SYNCED"
            )
            db.collection("users").document(uid)
                .collection("investments").document(investment.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
        } catch (e: Exception) {
            Log.e(TAG, "Investment sync failed: ${e.message}")
            _syncState.value = SyncState.SYNC_ERROR
        }
    }
}
